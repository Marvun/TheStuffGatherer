package me.marvuun.commands

import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.marvuun.logic.*


fun activityCommands() = commands("Activity") {
  slash("explore") {
    execute(IntegerArg("minutes")) {
      startExploration()
    }
  }
  slash("travel") {
    execute {
      startTravel()
    }
  }
  slash("finish") {
    execute {
      finishActivity()
    }
  }
  slash("activity") {
    execute {
      getActivity()
    }
  }
  slash("gather") {
    execute {
      startGathering()
    }
  }
}