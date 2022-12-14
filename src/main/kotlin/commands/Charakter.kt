package com.theStuffGatherer.commands

import com.theStuffGatherer.databass.DAOs.Player
import com.theStuffGatherer.databass.DAOs.Sites
import com.theStuffGatherer.databass.DAOs.Skills
import com.theStuffGatherer.databass.DAOs.addSiteToDatabase
import com.theStuffGatherer.enums.ActivityTypes
import com.theStuffGatherer.logic.calculateProgress
import com.theStuffGatherer.logic.getRewards
import com.theStuffGatherer.databass.tables.PlayersTable
import com.theStuffGatherer.databass.tables.SitesTable
import com.theStuffGatherer.databass.tables.SkillsTable
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
          title = if (player.currentActivity == ActivityTypes.NOTHING) "You aren't doing anything right now."
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
          if (player.currentActivity == ActivityTypes.NOTHING) {
            title = "You can't claim rewards, when you aren't doing anything!"
            return@transaction
          }
          val timeLeft = player.activityStartTime + player.activityDuration - System.currentTimeMillis()
          if (timeLeft > 0) {
            title = "You can't claim yet. You are still ${player.currentActivity.name.lowercase()}. Time remaining: ${millisecondsToDuration(timeLeft)}"
            return@transaction
          }
          val duration = player.activityDuration
          player.activityDuration = 0L
          player.currentActivity = ActivityTypes.NOTHING
          val rewards = getRewards(duration / 60000L)
          if (rewards.isEmpty()) {
            title = "You found nothing while exploring. Better luck next time!"
            return@transaction
          }
          val sites = Sites.find { SitesTable.userId eq player.id }.first()

          title = "You found the following while exploring:"
          description = "The letters 'D' to 'S' represent the rarity of the site, while S is the rarest."
          for (entry in rewards) {
            sites.addSiteToDatabase(entry)
            val rarities = entry.value.groupingBy { it }.eachCount()
            val formattedRarities = mutableListOf<String>()
            rarities.forEach {
              formattedRarities.add("${it.value}x ${it.key.name}")
            }
            field {
              name = entry.key.name
              value = formattedRarities.joinToString("\n")
            }
          }
        }
      }
    }
  }
}