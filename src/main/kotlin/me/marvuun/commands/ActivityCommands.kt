package me.marvuun.commands

import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.marvuun.logic.*


fun activityCommands() = commands("Activity") {
  slash("explore", "Go and explore the wild for new sites!") {
    execute(IntegerArg("minutes")) {
      isRegisteredPlayer() ?: return@execute
      startExploration()
    }
  }
  slash("travel", "Travel to a specific place.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      openTravelMenu()
    }
  }
  slash("activity", "Tells you, what you are doing right now.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      getActivity()
    }
  }
  slash("craft", "Start to craft items with your resources.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      openCraftingMenu()
    }
  }
}