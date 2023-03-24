package me.marvuun.conversations

import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.conversations.ConversationBuilder
import me.jakejmattson.discordkt.conversations.conversation
import me.marvuun.database.daos.activities.CurrentPlayerActivity
import me.marvuun.database.daos.location.Site
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.tables.locations.Sites
import me.marvuun.enums.ActivityTypes
import me.marvuun.logic.*
import me.marvuun.util.millisecondsToDuration
import me.marvuun.util.promptUntilAsEmbed
import me.marvuun.util.toIntRange
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun GuildSlashCommandEvent<NoArgs>.gatherConversation() = conversation("cancel", 30) {

  val player = getPlayer(author)

  if (player.currentLocation == null || player.currentLocation == getHome(author).homeId.value) {
    respond {
      title = "There aren't any resources to be gathered here."
    }
    return@conversation
  }

  val site = getSite(author)
  var tempCurrentResources = site.currentResources.toMutableMap()
  var isValid = true
  var selectedResources = mutableMapOf<String, Int>()

  while (isValid) {

    val selection = resourceSelectionPrompt(site, tempCurrentResources)

    if (selection == "ALL") {
      selectedResources = site.currentResources.toMutableMap()
      tempCurrentResources = mutableMapOf()
      break
    }

    val amount = promptUntilAsEmbed(AnyArg,
      "Please enter a valid amount.",
      { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= tempCurrentResources[selection]!! }) {
//      title = "How often do you want to gather `${RawResource.getResourceFromShort(selection).name.value}`?"
      description = "The maximum is in this case ${tempCurrentResources[selection]}."
    }

    selectedResources[selection] = (selectedResources[selection] ?: 0) + amount.toInt()

    tempCurrentResources[selection] = tempCurrentResources[selection]!! - amount.toInt()

    tempCurrentResources = tempCurrentResources.filter { it.value != 0 }.toMutableMap()

    if (tempCurrentResources.isEmpty()) break

    isValid = promptButton {
      embed {
        title = "Do you want to gather anything else here?"
      }
      buttons {
        button("Yes", Emojis.whiteCheckMark, true)
        button("No", Emojis.x, false)
      }
    }
  }

  var duration = 0L
  selectedResources.forEach { (short, amount) ->
    val gatheringTime = (getResourceFromShort(short) as RawResource).gatherDuration
    duration += gatheringTime * amount
  }

  val startTime = System.currentTimeMillis()
  transaction {
    site.currentResources = tempCurrentResources
    player.activityStartTime = startTime
    player.currentActivityType = ActivityTypes.GATHERING
    player.currentActivity = "Gathering resources at the ${site.type.name.lowercase()}."
    player.activityDuration = duration
    player.currentlyMaking = selectedResources.mapValues { it.value.toIntRange() }
  }

  val descriptionText = if (tempCurrentResources.isEmpty()) {
    transaction {
      player.currentLocation = null

      val playerSite = getPlayerSites(author)
      playerSite.deleteColumnWithUUID(site.siteId.value)

      val occupiedCoordinates = player.occupiedCoordinates
      occupiedCoordinates.remove(mutableListOf(site.xCoordinate, site.yCoordinate))
      player.occupiedCoordinates = occupiedCoordinates

      site.delete()
    }
    "Since you will be gathering all the resources, that were left, this site will be depleted."
  } else null

  transaction {
    CurrentPlayerActivity.new {
      guildId = guild.id.value
      channelId = channel.id.value
      userId = user.id.value
      activityEnd = startTime + duration
    }
  }

  respond {
    title = "You will be gathering for ${millisecondsToDuration(duration)} at the ${site.type.name.lowercase()}."
    description = descriptionText
  }
}

fun GuildSlashCommandEvent<NoArgs>.abandonSiteConversation() = conversation("cancel", 30) {
  val playerSites = getPlayerSites(author)

  val playerSitesMap = transaction {
    mapOf(
      "Mine" to playerSites.mineId,
      "Lake" to playerSites.lakeId,
      "River" to playerSites.riverId,
      "Forest" to playerSites.forestId,
      "Meadow" to playerSites.meadowId
    ).filter { it.value != null }
  }

  if (playerSitesMap.isEmpty()) {
    respond {
      title = "You don't have any sites you can abandon. Visit some first."
    }
    return@conversation
  }

  val selection = promptSelect {
    content {
      title = "Which site do you want to abandon?"
      description = "Select a site or type `cancel` to cancel the action."
    }
    playerSitesMap.forEach { (name, _) ->
      option(name)
    }
  }.first()

  val uuid = playerSitesMap[selection]!!
  val player = getPlayer(author)

  if (transaction { player.destination == uuid }) {
    respond {
      title = "You can't abandon a site, that you are traveling to."
    }
    return@conversation
  }

  transaction {
    if (player.currentLocation == uuid) {
      player.currentLocation = null
    }

    val site = Site.find { Sites.id eq uuid }.first()
    site.delete()
  }

  playerSites.deleteColumnWithUUID(uuid)

  respond {
    title = "You abandoned your current ${selection.lowercase()}."
  }
}

suspend fun ConversationBuilder.resourceSelectionPrompt(site: Site, tempCurrentResources: MutableMap<String, Int>) =
  promptSelect {
    content {
      title = "Choose what you want to gather."
    }

    if (tempCurrentResources.size > 1) {
      option("All", "ALL", "You will collect all the available resources.")
    }

    tempCurrentResources.forEach { (short, amount) ->
      val resource = getResourceFromShort(short)
      option(resource.name.value, short, "$amount/${site.currentResources[short]}")
    }

  }.first()

