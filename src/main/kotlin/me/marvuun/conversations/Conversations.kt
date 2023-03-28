package me.marvuun.conversations

import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.conversations.conversation
import me.marvuun.database.daos.location.Site
import me.marvuun.database.tables.locations.Sites
import me.marvuun.logic.getPlayer
import me.marvuun.logic.getPlayerSites
import org.jetbrains.exposed.sql.transactions.transaction


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

