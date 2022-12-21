package me.marvuun.commands

import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.marvuun.logic.*


fun activityCommands() = commands("Activity") {
  slash("explore", "Go and explore the wild for new sites!") {
    execute(IntegerArg("minutes")) {
      startExploration()
    }
  }
  slash("travel", "Travel to a specific place.") {
    execute {
      startTravel()
    }
  }
  slash("finish", "Finish your current activity.") {
    execute {
      finishActivity()
    }
  }
  slash("activity", "Tells you, what you are doing right now.") {
    execute {
      getActivity()
    }
  }
  slash("gather", "Gather the resources at your current location.") {
    execute {
      startGathering()
    }
  }
}