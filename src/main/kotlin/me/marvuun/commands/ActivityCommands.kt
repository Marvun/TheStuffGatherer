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
      startTravel()
    }
  }
  slash("finish", "Finish your current activity.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      finishActivity()
    }
  }
  slash("activity", "Tells you, what you are doing right now.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      getActivity()
    }
  }
  slash("gather", "Gather the resources at your current location.") {
    execute {
      isRegisteredPlayer() ?: return@execute
      startGathering()
    }
  }
}