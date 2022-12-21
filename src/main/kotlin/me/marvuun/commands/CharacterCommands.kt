package me.marvuun.commands

import me.jakejmattson.discordkt.commands.commands
import me.marvuun.logic.*

fun characterCommands() = commands("Character") {
  slash("start","Start the journey!") {
    execute {
      startJourney()
    }
  }
  slash("sites", "Shows you all your available sites, that you have found."){
    execute {
      isRegisteredPlayer() ?: return@execute
      printSites()
    }
  }
  slash("location" , "Tells you where you are and what can be found at your current location.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      getLocation()
    }
  }
  slash("levels", "Shows you your level progresses.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      printLevels()
    }
  }
}

