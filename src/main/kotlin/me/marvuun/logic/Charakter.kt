package me.marvuun.logic

import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.database.daos.Player
import me.marvuun.database.daos.PlayerSite
import me.marvuun.database.tables.PlayerSites
import me.marvuun.database.tables.Players
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