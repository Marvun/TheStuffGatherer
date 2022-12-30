package me.marvuun.logic

import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import me.jakejmattson.discordkt.Args1
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.conversations.gatherConversation
import me.marvuun.conversations.travelConversation
import me.marvuun.database.daos.*
import me.marvuun.database.tables.Homes
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
import kotlin.math.absoluteValue

suspend fun GuildSlashCommandEvent<Args1<Int>>.startExploration() {

  val player = getPlayer()

  checkIfBusy() ?: return

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

suspend fun finishActivity(user: User, channel: MessageChannel) {

  val player = transaction { Player.findById(user.id.value) }!!

  when (player.currentActivityType!!) {

    ActivityTypes.EXPLORING -> {
      finishExploring(player, user , channel)
    }

    ActivityTypes.TRAVELING -> {
      finishTraveling(player, channel)
    }

    ActivityTypes.GATHERING -> {
      finishGathering(player, user, channel)
    }
  }
  transaction {
    player.currentActivity = ""
    player.currentActivityType = null
    player.activityDuration = 0
  }

}

suspend fun finishGathering(player: Player, user: User, channel: MessageChannel) {

  val resources = transaction { player.currentlyGathering }
  val transformedResources = mutableListOf<String>()

  resources.forEach { (short, count) ->

    val resource = Resource.getResourceFromShort(short)
    val amount = count * calculateResourceAmount(resource, user)
    val levels = transaction { Level.findById(user.id.value)!! }

    transaction {

      val inventoryEntries =
        Inventory.find { (Inventories.userId eq user.id.value) and (Inventories.itemId eq resource.short) }

      if (inventoryEntries.empty()) {
        Inventory.new {
          userId = user.id.value
          itemId = resource.short
          this.amount = amount
        }
      }
      else {
        val inventoryEntry = inventoryEntries.first()
        inventoryEntry.amount = inventoryEntry.amount + amount
      }
    }


    levels.addExperience(resource, amount, user, channel)

    transformedResources.add(
      "${amount}x ${resource.name.value}"
    )
  }

  transaction { player.currentlyGathering = mutableMapOf() }

  channel.createEmbed {
    title = "You finished gathering."
    field {
      name = "You got the following resources:\n"
      value = transformedResources.joinToString("\n")
    }
  }
}

suspend fun finishTraveling(player: Player, channel: MessageChannel) {

  channel.createEmbed {
    transaction {
      player.currentLocation = player.destination
      player.destination = null
    }

    val home = transaction { Home.find { Homes.userId eq player.userId.value}.first() }

    title = if (player.currentLocation == home.homeId.value) {
      "You arrived at home."
    } else {
      val site = transaction { Site.findById(player.currentLocation!!)!! }
      "You arrived at the ${site.type.getDisplayName()}."
    }
  }
}

suspend fun finishExploring(player: Player, user: User, channel: MessageChannel) {
  val amount = generateSites(player, user)

  channel.createEmbed {
    title = "You finished exploring.\nYou found $amount sites."
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

suspend fun GuildSlashCommandEvent<*>.checkIfBusy(): Unit? {

  val player = getPlayer()
  val busy = transaction { player.currentActivityType } != null

  return if (busy) {
    respond {
      title = "You are currently busy. You are ${player.currentActivityType!!.name.lowercase()}."
    }
    null
  } else Unit
}

fun GuildSlashCommandEvent<*>.getPlayer() = transaction { Player.findById(this@getPlayer.author.id.value) }!!

suspend fun GuildSlashCommandEvent<*>.isRegisteredPlayer(): Unit? {
  val player = transaction { Player.findById(this@isRegisteredPlayer.author.id.value) }
  return if (player == null) {
    respond {
      title = "Please start your journey with `/start` first."
    }
    null
  }
  else Unit
}

fun GuildSlashCommandEvent<*>.getSites() =
  transaction { Site.find(Sites.userId eq getPlayer().userId.value).groupBy { it.type } }

fun GuildSlashCommandEvent<*>.getPlayerSites() =
  transaction { PlayerSite.findById(this@getPlayerSites.author.id.value)!! }

fun GuildSlashCommandEvent<*>.getLevels() = transaction { Level.findById(this@getLevels.author.id.value)!! }

fun GuildSlashCommandEvent<*>.getSite() = transaction { Site.findById(getPlayer().currentLocation!!)!! }

fun GuildSlashCommandEvent<*>.getHome() = transaction { Home.find { Homes.userId eq this@getHome.author.id.value}.first() }

fun GuildSlashCommandEvent<*>.getInventory() = transaction { Inventory.find {Inventories.userId eq this@getInventory.author.id.value } }

fun generateSites(player: Player, user: User): Int {

  var count = 0

  repeat((player.activityDuration / 60000).toInt()) {

    val randomNum = (1..100).random()
    val randomNum2 = (0..100).random()

    if (randomNum2 > 50) {
      val rarityType = RarityTypes.values().find { randomNum in it.range }!!
      val siteType = SiteTypes.values().random()

      transaction {
        val resources = generateResources(rarityType, siteType, user)

        Site.new {
          siteId = EntityID(UUID.randomUUID(), Sites)
          userId = player.userId.value
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

  return when (SiteTypes.getFromString(selection)) {

    SiteTypes.FOREST -> {
      playerSites.forestId = playerSites.forestId ?: sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.forestId!!
    }

    SiteTypes.MINE -> {
      playerSites.mineId = playerSites.mineId ?: sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.mineId!!
    }

    SiteTypes.LAKE -> {
      playerSites.lakeId = playerSites.lakeId ?: sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.lakeId!!
    }

    SiteTypes.RIVER -> {
      playerSites.riverId = playerSites.riverId ?: sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.riverId!!
    }

    SiteTypes.MEADOW -> {
      playerSites.meadowId = playerSites.meadowId ?: sites[SiteTypes.getFromString(selection)]!!.random().siteId.value
      playerSites.meadowId!!
    }
  }
}

fun calculateNewTravelTimeToHome(home: Home, travelTime: Long) =
  if (home.travelTime == 0)
     travelTime.toInt()
  else
    (home.travelTime - (-travelTime..travelTime).random()).absoluteValue.toInt()
