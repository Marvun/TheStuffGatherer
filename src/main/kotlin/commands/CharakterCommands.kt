package com.theStuffGatherer.commands

import com.theStuffGatherer.databass.DAOs.*
import com.theStuffGatherer.databass.DAOs.sites.Mines
import com.theStuffGatherer.enums.ActivityTypes
import com.theStuffGatherer.logic.calculateProgress
import com.theStuffGatherer.databass.tables.PlayersTable
import com.theStuffGatherer.databass.tables.SkillsTable
import com.theStuffGatherer.logic.finishActivity
import com.theStuffGatherer.logic.getInventory
import com.theStuffGatherer.logic.getLocation
import com.theStuffGatherer.util.millisecondsToDuration
import me.jakejmattson.discordkt.commands.commands
import org.jetbrains.exposed.sql.transactions.transaction

fun character() = commands("Character") {
  slash("skills", "Shows you the progress of your skills.") {
    execute {
      val player = transaction {
        Player.find {
          PlayersTable.userId eq author.id.value
        }.first()
      }
      val playerSkill = transaction {
        Skills.find { SkillsTable.userId eq player.id }.first()
      }
      respond {
        for (skill in playerSkill.skills) {
          field {
            name = skill.name
            value =
              "Level: ${skill.level} \nProgress: \n ${calculateProgress(skill.exp.toDouble() / skill.maxExp * 100)} (${skill.exp}/${skill.maxExp})[${(skill.exp.toDouble() / skill.maxExp) * 100}%]"
          }
        }
      }
    }
  }
  slash("start", "Start the journey!") {
    execute {
      var message = ""
      transaction {
        if (Player.find { PlayersTable.userId eq author.id.value }.toList().isEmpty()) {
          val player = Player.new {
            userId = author.id.value
          }
          Skills.new {
            userId = player
            fishingSkill = skills[0]
            choppingSkill = skills[1]
            miningSkill = skills[2]
            exploringSkill = skills[3]
          }
          Sites.new {
            userId = player
          }
          Mines.new {
            userId = player
          }
          message = "You started your journey!"
        } else {
          message = "You already started your journey!"
        }

      }
      respond(message)
    }
  }
  slash("activity", "Tells you your current activity.") {
    execute {
      respond {
        transaction {
          val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
          title = if (player.currentActivityType == ActivityTypes.NOTHING) "You aren't doing anything right now."
          else {
            val timeLeft = player.activityStartTime + player.activityDuration - System.currentTimeMillis()
            if (timeLeft > 0) {
              "You are currently ${player.currentActivity}. Time remaining: ${
                millisecondsToDuration(
                  timeLeft
                )
              }"
            }
            else "You finished ${player.currentActivity}. Use `/finish` to finish your activity."
          }}
      }
    }
  }

  slash("finish", "Finish your current activity.") {
    execute {
      finishActivity()

    }
  }
  slash("location") {
    execute {
      getLocation()
    }
  }
  slash("inventory") {
    execute {
      getInventory()
    }
  }
}