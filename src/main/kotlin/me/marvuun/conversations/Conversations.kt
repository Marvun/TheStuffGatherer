package me.marvuun.conversations

import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.conversations.conversation
import me.marvuun.database.daos.Site
import me.marvuun.enums.ActivityTypes
import me.marvuun.enums.SiteTypes
import me.marvuun.logic.*
import me.marvuun.util.millisecondsToDuration
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

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
