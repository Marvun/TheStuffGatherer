package me.marvuun.conversations

import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.conversations.conversation
import me.marvuun.database.daos.Resource
import me.marvuun.database.daos.Site
import me.marvuun.enums.ActivityTypes
import me.marvuun.logic.*
import me.marvuun.util.millisecondsToDuration
import me.marvuun.util.promptUntilAsEmbed
import org.jetbrains.exposed.sql.transactions.transaction

fun GuildSlashCommandEvent<NoArgs>.travelConversation() = conversation("cancel", 30) {
  val player = getPlayer()
  val sites = getSites()

  if (sites.isEmpty()) {
    respond {
      title = "There is nothing you can travel to."
    }
    return@conversation
  }

  if (checkIfBusy(player)) {
    respond {
      title = "You are currently busy. You are ${player.currentActivityType!!.name.lowercase()}."
    }
    return@conversation
  }
  val selection = promptSelect {
    content {
      title = "Where do you want to travel?"
    }
    sites.forEach {
      option(it.key.getDisplayName(), it.key.name, "${it.value.size} left")
    }
  }.first()

  val site = transaction {

    val siteUUID = getSiteUUIDFromSelection(selection, sites)

    Site.findById(siteUUID)!!

  }

  if (player.currentLocation != site.siteId.value) {
    transaction {
      player.currentActivity = "Travelling to a ${site.type.getDisplayName()}."
      player.currentActivityType = ActivityTypes.TRAVELING
      player.activityDuration = site.travelTime.toLong()
      player.activityStartTime = System.currentTimeMillis()
      player.destination = site.siteId.value
    }
    respond {
      title =
        "You will now travel ${millisecondsToDuration(site.travelTime.toLong())} to the ${site.type.name.lowercase()}."
    }
  } else {
    respond {
      title = "You already are at your current ${site.type.name.lowercase()} and therefore won't travel now."
      description =
        "If you want to visit a different ${site.type.name.lowercase()}, you either have to gather all the resources or abandon it with `/abandon`."
    }
  }


}

fun GuildSlashCommandEvent<NoArgs>.gatherConversation() = conversation("cancel", 30) {
  val player = getPlayer()
  val site = getSite()
  val tempCurrentResources = site.currentResources.toMutableMap()
  var isValid = true
  val selectedResources = mutableMapOf<String, Int>()

  while (isValid) {

    val selection = promptSelect {
      content {
        title = "Choose what you want to gather."
      }

      tempCurrentResources.forEach{ (short, amount) ->
        val resource = Resource.getResourceFromShort(short)
        option(resource.name.value, short, "$amount/${site.currentResources[short]}")
      }

    }.first()

    val amount = promptUntilAsEmbed(AnyArg, "Please enter a valid amount.", { it.toIntOrNull() != null && it.toInt() > 0 && it.toInt() <= tempCurrentResources[selection]!! }) {
      title = "How often do you want to gather `${Resource.getResourceFromShort(selection).name.value}`?"
      description = "The maximum is in this case ${tempCurrentResources[selection]}."
    }

    if (selectedResources[selection] == null)
      selectedResources[selection] = amount.toInt()
    else
      selectedResources[selection] = selectedResources[selection]!! + amount.toInt()

    tempCurrentResources[selection] = tempCurrentResources[selection]!! - amount.toInt()

    if (tempCurrentResources[selection] == 0)
      tempCurrentResources.remove(selection)

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
    val gatheringTime = Resource.getResourceFromShort(short).gatherDuration
    duration += gatheringTime * amount
  }

  transaction {
    site.currentResources = tempCurrentResources
    player.activityStartTime = System.currentTimeMillis()
    player.currentActivityType = ActivityTypes.GATHERING
    player.currentActivity = "Gathering resources at the ${site.type.name.lowercase()}."
    player.activityDuration = duration
    player.currentlyGathering = selectedResources
  }

  val descriptionText = if (tempCurrentResources.isEmpty()) {
    transaction {
      player.currentLocation = null
      val playerSite = getPlayerSites()
      playerSite.deleteColumnWithUUID(site.siteId.value)
      site.delete()
    }
    "Since you will be gathering all the resources, that were left, this site will count as depleted."
  }
  else
    null

  respond {
    title = "You will be gathering for ${millisecondsToDuration(duration)} at the ${site.type.name.lowercase()}."
    description = descriptionText
  }
}