package me.marvuun.logic

import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.database.daos.Player
import me.marvuun.database.daos.PlayerSite
import me.marvuun.database.daos.Resource
import me.marvuun.database.daos.Site
import me.marvuun.database.tables.PlayerSites
import me.marvuun.database.tables.Players
import me.marvuun.database.tables.Resources
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun GuildSlashCommandEvent<NoArgs>.startJourney() {
  val response = transaction {

    if (Player.findById(author.id.value) == null) {

      Player.new {
        userId = EntityID(author.id.value, Players)
      }


      PlayerSite.new {
        userId = EntityID(author.id.value, PlayerSites)
      }
      "You started your journey!"
    } else "You already started your journey!"
  }
  respond {
    title = response
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.printSites() {
  val sites = getSites()

  respond {
    title = "You have discovered the following sites:\n"
    sites.forEach { (key, value) ->
      field {
        name = "${key.name.lowercase()}s: ${value.size}"
      }
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.getLocation() {
  val player = getPlayer()

  if (player.currentLocation == null) {
    respond {
      title = "You are in the middle of nowhere."
    }
    return
  }

  val site = transaction { Site.findById(player.currentLocation!!) }!!
  val totalResources = transaction { site.totalResources }
  val currentResources = transaction { site.currentResources }

  respond {
    title = "You are currently at a ${site.type.name.lowercase()}.\n"
    description = "The number represents how many times you can gather the resource here."
    footer {
      text = "Hint: Use /gather to start gathering."
    }
    totalResources.forEach { (short, amount) ->
      val resource = transaction { Resource.find { Resources.short eq short }.first()}
      field {
        name = resource.name.value
        value = "${currentResources[resource.short]}/$amount"
      }
    }
  }
}