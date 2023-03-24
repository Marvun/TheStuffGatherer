package me.marvuun.commands

import me.jakejmattson.discordkt.commands.commands
import me.marvuun.database.daos.generateBuyers
import me.marvuun.database.daos.location.City
import me.marvuun.database.tables.locations.Cities
import me.marvuun.logic.showHelp
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.sqrt

fun utilityCommands() = commands("Utility") {
  slash("help", "Shows you the help to all the commands.") {
    execute {
      showHelp()
    }
  }
}