package me.marvuun.logic

import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.ComponentInteractionBehavior
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import me.jakejmattson.discordkt.Args1
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.database.daos.Inventory
import me.marvuun.database.daos.Level
import me.marvuun.database.daos.Player
import me.marvuun.database.daos.PlayerSite
import me.marvuun.database.daos.activities.CurrentPlayerActivity
import me.marvuun.database.daos.location.*
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.tables.Inventories
import me.marvuun.database.tables.locations.Cities
import me.marvuun.database.tables.locations.Homes
import me.marvuun.database.tables.locations.Sites
import me.marvuun.enums.ActivityTypes
import me.marvuun.enums.RarityTypes
import me.marvuun.enums.ResourceCategories
import me.marvuun.enums.SiteTypes
import me.marvuun.util.checkUser
import me.marvuun.util.millisecondsToDuration
import me.marvuun.util.minutesToDuration
import me.marvuun.util.toIntRange
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.sqrt

private var commandInvoker: User? = null
suspend fun GuildSlashCommandEvent<Args1<Int>>.startExploration() {

  val player = getPlayer(author)

  checkIfBusy(interaction!!) ?: return

  if (args.first <= 0) {
    interaction!!.respondPublic {
      embed {
        title = "You can't explore for ${args.first} minutes!"
      }
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

  interaction!!.respondPublic {
    embed {
      title = "You went exploring for ${minutesToDuration(args.first)}!"
    }
  }
}

suspend fun openGatheringMenu(ci: ActionInteraction) {
  commandInvoker = ci.user

  val site = getSite(ci.user)
  val currentResources = site.currentResources.toMutableMap()
  if (ci is ComponentInteractionBehavior)
    ci.updatePublicMessage {
      embed {
        title = "Choose what you want to gather."
      }
      actionRow {
        stringSelect("gatherSelectResource") {
          this.allowedValues = 1..currentResources.size
          currentResources.forEach { (short, amount) ->
            val resource = getResourceFromShort(short)
            option(resource.name.value, short) {
              description = "$amount/${site.currentResources[short]}"
            }
          }
        }
      }
    }
}

suspend fun askGatheringAmount(ci: ComponentInteraction, resourceShorts: MutableList<String>) {
  if (!checkUser(ci, commandInvoker!!)) return
  val site = getSite(ci.user)
  val currentResources = site.currentResources.toMutableMap()

  ci.modal("How often do you want to gather?", "askGatheringAmount") {
    resourceShorts.forEach {
      actionRow {
        val resource = getResourceFromShort(it)
        textInput(TextInputStyle.Short, "gatheringAmountTextInput_${resource.short}", resource.name.value) {
          placeholder = "Maximum for ${resource.name.value} is: ${currentResources[it]}"
        }
      }
    }
  }
}

suspend fun startGathering(msi: ModalSubmitInteraction, resources: MutableMap<String, Int>) {
  if (!checkUser(msi, commandInvoker!!)) return
  val player = getPlayer(msi.user)
  val site = getSite(msi.user)
  var duration = 0L
  val currentResources = site.currentResources.toMutableMap()
  resources.forEach { (short, amount) ->
    currentResources[short] = currentResources[short]!! - amount
    if (currentResources[short]!! == 0) currentResources.remove(short)
    val gatheringTime = (getResourceFromShort(short) as RawResource).gatherDuration
    duration += gatheringTime * amount
  }

  val startTime = System.currentTimeMillis()
  transaction {
    site.currentResources = currentResources
    player.activityStartTime = startTime
    player.currentActivityType = ActivityTypes.GATHERING
    player.currentActivity = "Gathering resources at the ${site.type.name.lowercase()}."
    player.activityDuration = duration
    player.currentlyMaking = resources.mapValues { it.value.toIntRange() }
  }

  val descriptionText = if (currentResources.isEmpty()) {
    transaction {
      player.currentLocation = null

      val playerSite = getPlayerSites(msi.user)
      playerSite.deleteColumnWithUUID(site.siteId.value)

      val occupiedCoordinates = player.occupiedCoordinates
      occupiedCoordinates.remove(mutableListOf(site.xCoordinate, site.yCoordinate))
      player.occupiedCoordinates = occupiedCoordinates

      site.delete()
    }
    "Since you will be gathering all the resources, that were left, this site will be depleted."
  } else null

  val guild = msi.message!!.getGuild()
  transaction {
    CurrentPlayerActivity.new {
      guildId = guild.id.value
      channelId = msi.channel.id.value
      userId = msi.user.id.value
      activityEnd = startTime + duration
    }
  }

  msi.updatePublicMessage {
    embed {
      title = "You will be gathering for ${millisecondsToDuration(duration)} at the ${site.type.name.lowercase()}."
      description = descriptionText
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.openTravelMenu() {
  commandInvoker = author
  val player = getPlayer(author)
  val sites = getSites(author)

  checkIfBusy(interaction!!) ?: return

  interaction!!.respondPublic {
    embed {
      title = "Where do you want to travel?"
    }
    actionRow {
      stringSelect("travelMenu") {

        transaction {
          if (player.currentLocation != getHome(author).homeId.value)
            option("Home", "home", fun SelectOptionBuilder.() {
              description = "Travel to your home."
            })
          if (!City.find { Cities.userId eq player.userId.value and (Cities.id neq player.currentLocation) }.empty())
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
  if (!checkUser(ci, commandInvoker!!)) return
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
      ci.updatePublicMessage {
        components = mutableListOf()
        embed {
          title = "You will now travel ${millisecondsToDuration(player.activityDuration)} to your home."
        }
      }
    }

    "sites" -> {
      ci.updatePublicMessage {
        embed {
          title = "To what kind of site do you want to travel?"
        }
        actionRow {
          stringSelect("travelSiteMenu") {
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
      ci.updatePublicMessage {
        embed {
          title = "To which city do you want to travel?"
        }
        actionRow {
          stringSelect("travelCityMenu") {
            transaction {
              City.find { Cities.userId eq player.userId.value and (Cities.id neq player.currentLocation) }.forEach {
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
  if (!checkUser(ci, commandInvoker!!)) return
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
    ci.updatePublicMessage {
      components = mutableListOf()
      embed {
        title =
          "You will now travel ${millisecondsToDuration(site.travelTime.toLong())} to the ${site.type.name.lowercase()}."
      }
    }

  } else {
    ci.updatePublicMessage {
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
  if (!checkUser(ci, commandInvoker!!)) return
  val player = getPlayer(ci.user)
  val guild = ci.message.getGuild()
  val city =
    transaction { City.find { Cities.name eq selectedCityName and (Cities.userId eq ci.user.id.value) }.first() }
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
  ci.updatePublicMessage {
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
          itemId = if (resource.type == ResourceCategories.FUEL) resource.short.replace("raw", "fuel") else resource.short
          this.amount = amount
          type = resource.type
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

suspend fun checkIfBusy(interaction: ActionInteraction): Unit? {

  val player = getPlayer(interaction.user)
  val busy = transaction { player.currentActivityType } != null

  return if (busy) {
    interaction.respondPublic {
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
    val randomNum2 = if (amount == null) (0..100).random() else 100

    if (randomNum2 > 67) {
      val rarityType = rarity ?: RarityTypes.values().find { randomNum in it.range }!!
      val siteType = SiteTypes.values().random()

      transaction {
        val resources = generateResources(rarityType, siteType, user)
        val coordinates = generateCoordinates(rarityType)
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
          travelTime =
            (sqrt(xCoordinate.toDouble() * xCoordinate.toDouble() + yCoordinate.toDouble() * yCoordinate.toDouble()) * 5000).toInt()
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
