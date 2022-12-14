package com.theStuffGatherer.commands

import com.theStuffGatherer.DAOs.Player
import com.theStuffGatherer.DAOs.Skills
import com.theStuffGatherer.logic.calculateProgress
import com.theStuffGatherer.tables.PlayersTable
import com.theStuffGatherer.tables.SkillsTable
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
          message = "You started your journey!"
        } else {
          message = "You already started your journey!"
        }

      }
      respond(message)
    }
  }
}