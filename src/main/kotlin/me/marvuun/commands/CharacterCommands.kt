package me.marvuun.commands

import me.jakejmattson.discordkt.commands.commands
import me.marvuun.logic.*

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
  slash("levels") {
    execute {
      printLevels()
    }
  }
}

