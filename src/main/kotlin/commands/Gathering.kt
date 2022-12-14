package com.theStuffGatherer.commands

import com.theStuffGatherer.databass.DAOs.Player
import com.theStuffGatherer.databass.DAOs.Skills
import com.theStuffGatherer.enums.ActivityTypes
import com.theStuffGatherer.databass.tables.PlayersTable
import com.theStuffGatherer.databass.tables.SkillsTable
import com.theStuffGatherer.util.millisecondsToDuration
import com.theStuffGatherer.util.minutesToDuration
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import org.jetbrains.exposed.sql.transactions.transaction

fun gathering() = commands("Gathering") {
  slash("mine") {
    execute {
      transaction {
        val player = Player.find { PlayersTable.userId eq author.id.value }.first()
        val skills = Skills.find { SkillsTable.userId eq player.id }.first()
        skills.miningSkill = skills.addSkillExp("Mining", 15)
      }
      respond("You mined!")
    }
  }
  slash("chop") {
    execute {
      transaction {
        val player = Player.find { PlayersTable.userId eq author.id.value }.first()
        val skills = Skills.find { SkillsTable.userId eq player.id }.first()
        skills.choppingSkill = skills.addSkillExp("Chopping", 15)
      }
      respond("You chopped!")
    }
  }
  slash("fish") {
    execute {
      var isBusy = false
      var message = ""
      respond {
        transaction {
          val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
          if (player.currentActivity != ActivityTypes.NOTHING) {
            isBusy = true
            message =
              "You are still ${player.currentActivity.name.lowercase()}. Time remaining: ${millisecondsToDuration(player.activityStartTime + player.activityDuration - System.currentTimeMillis())}"
            return@transaction
          }
          val skills = Skills.find { SkillsTable.userId eq player.id }.first()
          skills.fishingSkill = skills.addSkillExp("Fishing", 15)
        }
        title = if (isBusy) message else "You fished!"

      }

    }
  }
  slash("explore", "Go and explore the world.") {
    execute(IntegerArg("minutes")) {
      respond {
        if (args.first <= 0) {
          title = "You can't explore for ${args.first} minutes!"
          return@respond
        }
        transaction {
          val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
          player.activityStartTime = System.currentTimeMillis()
          player.activityDuration = args.first * 60000L
          player.currentActivity = ActivityTypes.EXPLORING
        }
        title = "You went exploring for ${minutesToDuration(args.first)}!"
      }
    }
  }

}