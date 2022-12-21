package me.marvuun.logic

import me.jakejmattson.discordkt.Args1
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.conversations.gatherConversation
import me.marvuun.conversations.travelConversation
import me.marvuun.database.daos.*
import me.marvuun.database.tables.Inventories
import me.marvuun.database.tables.Sites
import me.marvuun.enums.ActivityTypes
import me.marvuun.enums.RarityTypes
import me.marvuun.enums.SiteTypes
import me.marvuun.util.millisecondsToDuration
import me.marvuun.util.minutesToDuration
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

suspend fun GuildSlashCommandEvent<Args1<Int>>.startExploration() {

  val player = getPlayer()

  if (checkIfBusy(player)) {
    respond {
      title = "You are currently busy. You are ${player.currentActivityType!!.name.lowercase()}."
    }
    return
  }

  respond {

    if (args.first <= 0) {
      title = "You can't explore for ${args.first} minutes!"
      return@respond
    }

    transaction {
      player.currentActivityType = ActivityTypes.EXPLORING
      player.currentActivity = "Exploring the wild for new sites."
      player.activityStartTime = System.currentTimeMillis()
      player.activityDuration = args.first * 60000L
    }

    title = "You went exploring for ${minutesToDuration(args.first)}!"
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.startTravel() =
  travelConversation().startSlashResponse(discord, author, this)

suspend fun GuildSlashCommandEvent<NoArgs>.finishActivity() {
  val player = getPlayer()

  if (!checkIfBusy(player)) {
    respond {
      title = "You aren't doing anything right now."
    }
    return
  }

  val currentActivityType = player.currentActivityType!!

  if (player.activityStartTime + player.activityDuration > System.currentTimeMillis()) {
    val timeLeft = player.activityStartTime + player.activityDuration - System.currentTimeMillis()
    respond {
      title = "You can't finish your activity yet."
      description = "You are still ${currentActivityType.name.lowercase()} for ${millisecondsToDuration(timeLeft)}."
    }
    return
  }

  when (currentActivityType) {
    ActivityTypes.EXPLORING -> {
      val amount = generateSites()
      respond {
        title = """
          You finished ${currentActivityType.name.lowercase()}.
          You found $amount sites.
          """.trimIndent()
      }

    }

    ActivityTypes.TRAVELING -> {
      transaction {
        player.currentLocation = player.destination
        player.destination = null
      }
      val site = transaction { Site.findById(player.currentLocation!!) }!!
      respond {
        title = "You arrived at the ${site.type.getDisplayName()}."
      }
    }

    ActivityTypes.GATHERING -> {

      val resources = transaction { player.currentlyGathering }


      val transformedResources = mutableListOf<String>()

      resources.forEach { (short, count) ->
        val resource = Resource.getResourceFromShort(short)
        val amount = count * calculateResourceAmount(resource)
        val levels = getLevels()

        transaction {
          val inventoryEntries =
            Inventory.find { (Inventories.id eq author.id.value) and (Inventories.itemId eq resource.short) }

          if (inventoryEntries.empty()) {
            Inventory.new {
              userId = EntityID(author.id.value, Inventories)
              itemId = resource.short
              this.amount = amount
            }
          } else {
            val inventoryEntry = inventoryEntries.first()
            inventoryEntry.amount = inventoryEntry.amount + amount
          }
        }


        levels.context = this
        levels.addExperience(resource, amount)

        transformedResources.add(
          "${amount}x ${resource.name.value}"
        )
      }
      transaction { player.currentlyGathering = mutableMapOf() }
      respond {
        title = "You finished gathering."
        field {
          name = "You got the following resources:\n"
          value = transformedResources.joinToString("\n")
        }
      }

    }
  }
  transaction {
    player.currentActivity = ""
    player.currentActivityType = null
    player.activityDuration = 0
  }


}

suspend fun GuildSlashCommandEvent<NoArgs>.getActivity() {
  val player = getPlayer()
  val activity = player.currentActivityType

  respond {

    if (activity == null) title = "You are currently doing nothing."
    else {

      val timeLeft = player.activityStartTime + player.activityDuration - System.currentTimeMillis()

      title = "You are currently ${activity.name.lowercase()}."

      field {
        name = player.currentActivity
        value = if (timeLeft > 0) "Time remaining: ${millisecondsToDuration(timeLeft)}"
        else "You are done ${activity.name.lowercase()}. You can finish it with `/finish`."
      }
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.startGathering() =
  gatherConversation().startSlashResponse(discord, author, this)

fun checkIfBusy(player: Player) = transaction { player.currentActivityType } != null

fun GuildSlashCommandEvent<*>.getPlayer() = transaction { Player.findById(this@getPlayer.author.id.value)!! }

fun GuildSlashCommandEvent<*>.getSites() =
  transaction { Site.find(Sites.userId eq getPlayer().userId.value).groupBy { it.type } }

fun GuildSlashCommandEvent<*>.getPlayerSites() =
  transaction { PlayerSite.findById(this@getPlayerSites.author.id.value)!! }

fun GuildSlashCommandEvent<*>.getLevels() = transaction { Level.findById(this@getLevels.author.id.value)!! }

fun GuildSlashCommandEvent<*>.getSite() = transaction { Site.findById(getPlayer().currentLocation!!)!! }

fun GuildSlashCommandEvent<NoArgs>.generateSites(): Int {

  val player = getPlayer()
  var count = 0

  repeat((player.activityDuration / 60000).toInt()) {

    val randomNum = (1..100).random()
    val randomNum2 = (0..100).random()

    if (randomNum2 > 50) {
      val rarityType = RarityTypes.values().find { randomNum in it.range }!!
      val siteType = SiteTypes.values().random()

      transaction {
        val resources = generateResources(rarityType, siteType)

        Site.new {
          siteId = EntityID(UUID.randomUUID(), Sites)
          userId = author.id.value
          type = siteType
          rarity = rarityType
          currentResources = resources
          totalResources = resources
          travelTime = generateTravelTime(rarity)
        }
      }
      count++
    }

  }
  return count
}

fun GuildSlashCommandEvent<NoArgs>.getSiteUUIDFromSelection(
  selection: String, sites: Map<SiteTypes, List<Site>>
): UUID {

  val playerSites = getPlayerSites()

  return when (selection) {
    "FOREST" -> if (playerSites.forestId == null) {
      playerSites.forestId = sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.forestId!!
    } else playerSites.forestId!!

    "MINE" -> if (playerSites.mineId == null) {
      playerSites.mineId = sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.mineId!!
    } else playerSites.mineId!!

    "LAKE" -> if (playerSites.lakeId == null) {
      playerSites.lakeId = sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.lakeId!!
    } else playerSites.lakeId!!

    "RIVER" -> if (playerSites.riverId == null) {
      playerSites.riverId = sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.riverId!!
    } else playerSites.riverId!!

    "MEADOW" -> if (playerSites.meadowId == null) {
      playerSites.meadowId = sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.meadowId!!
    } else playerSites.meadowId!!

    else -> sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
  }
}