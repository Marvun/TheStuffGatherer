package me.marvuun.commands

import me.jakejmattson.discordkt.commands.commands
import me.marvuun.logic.getLocation
import me.marvuun.logic.printSites
import me.marvuun.logic.startJourney

fun characterCommands() = commands("Character") {
  slash("start","Start the journey!") {
    execute {
      startJourney()
    }
  }
  slash("sites"){
    execute {
      printSites()
    }
  }
  slash("location") {
    execute {
      getLocation()
    }
  }
}

