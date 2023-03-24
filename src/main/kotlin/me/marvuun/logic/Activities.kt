package me.marvuun.logic

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.updateEphemeralMessage
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import me.jakejmattson.discordkt.Args1
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.conversations.gatherConversation
import me.marvuun.database.daos.*
import me.marvuun.database.daos.activities.CurrentPlayerActivity
import me.marvuun.database.daos.location.*
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.tables.locations.Cities
import me.marvuun.database.tables.locations.Homes
import me.marvuun.database.tables.Inventories
import me.marvuun.database.tables.locations.Sites
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
import kotlin.math.sqrt

suspend fun GuildSlashCommandEvent<Args1<Int>>.startExploration() {

  val player = getPlayer(author)

  checkIfBusy() ?: return

  if (args.first <= 0) {
    respond {
      title = "You can't explore for ${args.first} minutes!"
    }
    return
  }

  transaction {
    val startTime = System.currentTimeMillis()
    player.currentActivityType = ActivityTypes.EXPLORING
    player.currentActivity = "Exploring the wild for new sites."
    player.activityStartTime = startTime
    player.activityDuration = args.first * 60000L

    CurrentPlayerActivity.new {
      guildId = guild.id.value
      channelId = channel.id.value
      userId = author.id.value
      activityEnd = startTime + args.first * 60000L
    }
  }

  respond {
    title = "You went exploring for ${minutesToDuration(args.first)}!"
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.openTravelMenu() {
  val player = getPlayer(author)
  val sites = getSites(author)

  checkIfBusy() ?: return

  interaction!!.respondEphemeral {
    embed {
      title = "Where do you want to travel?"
    }
    actionRow {
      selectMenu("travelMenu") {
        transaction {
          if (player.currentLocation != getHome(author).homeId.value)
            option("Home", "home") {
              description = "Travel to your home."
            }
          if (!City.find { Cities.userId eq player.userId.value and (Cities.id neq player.currentLocation)}.empty())
            option("Cities", "cities") {
              description = "Travel to one of your discovered cities."
            }
          if (sites.isNotEmpty())
            option("Sites", "sites") {
              description = "Travel to one of your sites to gather resources."
            }
        }
      }
    }
  }

}

suspend fun openTravelCategoryMenu(ci: ComponentInteraction, category: String) {
  val home = getHome(ci.user)
  val player = getPlayer(ci.user)
  val sites = getSites(ci.user)
  val guild = ci.message.getGuild()

  when (category) {
    "home" -> {

      val startTime = System.currentTimeMillis()
      transaction {
        updateTravelTimes(home.homeId.value, ci.user)
        player.currentActivity = "Traveling home."
        player.currentActivityType = ActivityTypes.TRAVELING
        player.activityDuration = home.travelTime.toLong()
        player.activityStartTime = startTime
        player.destination = home.homeId.value

        CurrentPlayerActivity.new {
          guildId = guild.id.value
          channelId = ci.channelId.value
          userId = ci.user.id.value
          activityEnd = startTime + home.travelTime.toLong()
        }

        home.travelTime = 0
      }
      ci.updateEphemeralMessage {
        components = mutableListOf()
        embed {
          title = "You will now travel ${millisecondsToDuration(player.activityDuration)} to your home."
        }
      }
    }
    "sites" -> {
      ci.updateEphemeralMessage {
        embed {
          title = "To what kind of site do you want to travel?"
        }
        actionRow {
          selectMenu("travelSiteMenu") {
            sites.forEach {
              option(it.key.getDisplayName(), it.key.name) {
                description = "${it.value.size} left"
              }
            }
          }
        }
      }
    }
    "cities" -> {
      ci.updateEphemeralMessage {
        embed {
          title = "To which city do you want to travel?"
        }
        actionRow {
          selectMenu("travelCityMenu") {
            transaction {
              City.find { Cities.userId eq player.userId.value and (Cities.id neq player.currentLocation)}.forEach {
                option(it.name, it.name) {
                  description = "Travel time: ${millisecondsToDuration(it.travelTime.toLong())}"
                }
              }
            }
          }
        }
      }
    }
  }
}

suspend fun openTravelSiteMenu(ci: ComponentInteraction, selectedSiteType: String) {
  val sites = getSites(ci.user)
  val player = getPlayer(ci.user)
  val guild = ci.message.getGuild()
  val site = transaction {

    val siteUUID = getSiteUUIDFromSelection(selectedSiteType, sites, ci.user)

    Site.findById(siteUUID)!!

  }

  if (player.currentLocation != site.siteId.value) {
    transaction {

      val startTime = System.currentTimeMillis()
      updateTravelTimes(site.siteId.value, ci.user)
      player.currentActivity = "Traveling to a ${site.type.getDisplayName()}."
      player.currentActivityType = ActivityTypes.TRAVELING
      player.activityDuration = site.travelTime.toLong()
      player.activityStartTime = startTime
      player.destination = site.siteId.value

      CurrentPlayerActivity.new {
        guildId = guild.id.value
        channelId = ci.channelId.value
        userId = ci.user.id.value
        activityEnd = startTime + site.travelTime.toLong()
      }
    }
    ci.updateEphemeralMessage {
    components = mutableListOf()
      embed {
        title = "You will now travel ${millisecondsToDuration(site.travelTime.toLong())} to the ${site.type.name.lowercase()}."
      }
    }

  } else {
    ci.updateEphemeralMessage {
      components = mutableListOf()
      embed {
        title = "You already are at your current ${site.type.name.lowercase()} and therefore won't travel now."
        description =
          "If you want to visit a different ${site.type.name.lowercase()}, you either have to gather all the resources or abandon it with `/abandon`."
      }
    }
  }
}

suspend fun openTravelCityMenu(ci: ComponentInteraction, selectedCityName: String) {
  val player = getPlayer(ci.user)
  val guild = ci.message.getGuild()
  val city = transaction {  City.find { Cities.name eq selectedCityName and (Cities.userId eq ci.user.id.value)}.first() }
  transaction {
    val startTime = System.currentTimeMillis()

    updateTravelTimes(city.cityId.value, ci.user)
    player.currentActivity = "Traveling to ${city.name}."
    player.currentActivityType = ActivityTypes.TRAVELING
    player.activityDuration = city.travelTime.toLong()
    player.activityStartTime = startTime
    player.destination = city.cityId.value

    CurrentPlayerActivity.new {
      guildId = guild.id.value
      channelId = ci.channelId.value
      userId = ci.user.id.value
      activityEnd = startTime + city.travelTime.toLong()
    }
  }
  ci.updateEphemeralMessage {
    components = mutableListOf()
    embed {
      title = "You will now travel ${millisecondsToDuration(city.travelTime.toLong())} to ${city.name}."
    }
  }
}

suspend fun finishActivity(user: User, channel: MessageChannel) {

  val player = transaction { Player.findById(user.id.value) }!!

  when (player.currentActivityType!!) {

    ActivityTypes.EXPLORING -> {
      finishExploring(player, user, channel)
    }

    ActivityTypes.TRAVELING -> {
      finishTraveling(player, user, channel)
    }

    ActivityTypes.GATHERING -> {
      finishGathering(player, user, channel)
    }

    ActivityTypes.CRAFTING -> {
      finishCrafting(player, user, channel)
    }
  }
  transaction {
    player.currentActivity = ""
    player.currentActivityType = null
    player.activityDuration = 0
  }

}

suspend fun finishGathering(player: Player, user: User, channel: MessageChannel) {

  val resources = transaction { player.currentlyMaking }
  val transformedResources = mutableListOf<String>()

  resources.forEach { (short, count) ->

    val resource = getResourceFromShort(short) as RawResource
    val amount = count.first * resource.calculateResourceAmount(user)
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
      } else {
        val inventoryEntry = inventoryEntries.first()
        inventoryEntry.amount = inventoryEntry.amount + amount
      }

    }


    levels.addExperience(resource, amount, user, channel)

    transformedResources.add(
      "${amount}x ${resource.name.value}"
    )
  }

  transaction { player.currentlyMaking = mutableMapOf() }

  channel.createMessage {
    content = user.mention
    embed {
      title = "You finished gathering."
      field {
        name = "You got the following resources:\n"
        value = transformedResources.joinToString("\n")
      }
    }
  }
}

suspend fun finishTraveling(player: Player, user: User, channel: MessageChannel) {
  val location = getLocationFromUUID(user.id.value, player.destination!!)

  channel.createMessage {
    transaction {
      player.currentLocation = player.destination
      player.destination = null
    }

    content = user.mention

    embed {
      title = when (location) {
        is Home -> {
          "You arrived at home."
        }
        is City -> {
          "Your arrived at ${location.name}."
        }
        else -> {
          location as Site
          "You arrived at the ${location.type.getDisplayName()}."
        }
      }
    }
  }
}

suspend fun finishExploring(player: Player, user: User, channel: MessageChannel) {
  val amount = generateSites(player, user)

  channel.createMessage {
    content = user.mention
    embed {
      title = "You finished exploring.\nYou found $amount sites."
    }
  }

}

suspend fun GuildSlashCommandEvent<NoArgs>.getActivity() {
  val player = getPlayer(author)
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

  val player = getPlayer(author)
  val busy = transaction { player.currentActivityType } != null

  return if (busy) {
    interaction!!.respondEphemeral {
      embed {
        title = "You are currently busy. You are ${player.currentActivityType!!.name.lowercase()}."
      }
    }
    null
  } else Unit
}

fun getPlayer(user: User) = transaction { Player.findById(user.id.value) }!!

suspend fun GuildSlashCommandEvent<*>.isRegisteredPlayer(): Unit? {
  val player = transaction { Player.findById(this@isRegisteredPlayer.author.id.value) }
  return if (player == null) {
    respond {
      title = "Please start your journey with `/start` first."
    }
    null
  } else Unit
}

fun getSites(user: User) =
  transaction { Site.find(Sites.userId eq getPlayer(user).userId.value).groupBy { it.type } }

fun getPlayerSites(user: User) =
  transaction { PlayerSite.findById(user.id.value)!! }

fun getLevels(user: User) = transaction { Level.findById(user.id.value)!! }

fun getSite(user: User) = transaction { Site.findById(getPlayer(user).currentLocation!!)!! }

fun getHome(user: User) =
  transaction { Home.find { Homes.userId eq user.id.value }.first() }

fun getInventory(user: User) =
  transaction { Inventory.find { Inventories.userId eq user.id.value } }

fun getCities(user: User) =
  transaction { City.find { Cities.userId eq user.id.value } }

fun generateSites(player: Player, user: User, amount: Int? = null, rarity: RarityTypes? = null): Int {

  var count = 0

  repeat(amount ?: (player.activityDuration / 60000).toInt()) {

    val randomNum = (1..100).random()
    val randomNum2 = (0..100).random()

    if (randomNum2 > 0) {
      val rarityType = rarity ?: RarityTypes.values().find { randomNum in it.range }!!
      val siteType = SiteTypes.values().random()

      transaction {
        val resources = generateResources(rarityType, siteType, user)
        val coordinates = generateCoordinates(rarityType, user)
        val occupiedCoordinates = player.occupiedCoordinates
        occupiedCoordinates.add(coordinates)
        player.occupiedCoordinates = occupiedCoordinates

        Site.new {
          xCoordinate = coordinates[0]
          yCoordinate = coordinates[1]
          siteId = EntityID(UUID.randomUUID(), Sites)
          userId = player.userId.value
          type = siteType
          this.rarity = rarityType
          currentResources = resources
          totalResources = resources
          travelTime = (sqrt(xCoordinate.toDouble() * xCoordinate.toDouble() + yCoordinate.toDouble() * yCoordinate.toDouble()) * 10000).toInt()
        }
      }
      count++
    }

  }
  return count
}

fun getSiteUUIDFromSelection(
  selection: String, sites: Map<SiteTypes, List<Site>>, user: User
): UUID {

  val playerSites = getPlayerSites(user)

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
