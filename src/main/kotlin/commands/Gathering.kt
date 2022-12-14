package com.theStuffGatherer.commands

import com.theStuffGatherer.DAOs.Player
import com.theStuffGatherer.DAOs.Skills
import com.theStuffGatherer.enums.Activities
import com.theStuffGatherer.tables.PlayersTable
import com.theStuffGatherer.tables.SkillsTable
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
          if (player.currentActivity != Activities.NOTHING) {
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
      val first = args
      transaction {
        val player = Player.find { PlayersTable.userId eq author.id.value }.first()
        player.activityStartTime = System.currentTimeMillis()
        player.activityDuration = first.first * 60000L
        player.currentActivity = Activities.EXPLORING
      }
      respond("You went exploring for ${minutesToDuration(first.first)}!")
    }
  }
  slash("activity", "Tells you your current activity.") {
    execute {
      respond {
        transaction {
          val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
          title = if (player.currentActivity == Activities.NOTHING) "You aren't doing anything right now."
          else {
            val timeLeft = player.activityStartTime + player.activityDuration - System.currentTimeMillis()
            if (timeLeft > 0) {
              "You are currently ${player.currentActivity.name.lowercase()}. Time remaining: ${
                millisecondsToDuration(
                  timeLeft
                )
              }"
            }
          else "You finished ${player.currentActivity.name.lowercase()}. Use `/claim` to see what you got."
          }}
      }
    }
  }

  slash("claim", "Claim the rewards from your activity.") {
    execute {
      respond {
        transaction {
          val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
          if (player.currentActivity == Activities.NOTHING) {
            title = "You can't claim rewards, when you aren't doing anything!"
            return@transaction
          }
          val timeLeft = player.activityStartTime + player.activityDuration - System.currentTimeMillis()
          if (timeLeft > 0) {
            title = "You can't claim yet. You are still ${player.currentActivity.name.lowercase()}. Time remaining: ${millisecondsToDuration(timeLeft)}"
            return@transaction
          }
          player.activityDuration = 0L
          player.currentActivity = Activities.NOTHING
          title = "You claimed your activity rewards!"
        }
      }
    }
  }
}