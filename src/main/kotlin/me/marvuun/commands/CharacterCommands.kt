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
  slash("inventory", "Shows you, what you've got in your inventory.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      printInventory()
    }
  }
  slash("abandon", "Abandons a site, which still has resources left.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      abandonSite()
    }
  }
  slash("upgrade", "Gives you information on what you can upgrade.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      openUpgradeMenu()
    }
  }
}

