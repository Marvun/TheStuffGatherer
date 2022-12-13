package com.theStuffGatherer


import com.theStuffGatherer.DAOs.Player
import com.theStuffGatherer.DAOs.Skills
import com.theStuffGatherer.logic.calculateProgress
import com.theStuffGatherer.tables.PlayersTable
import com.theStuffGatherer.tables.SkillsTable
import com.theStuffGatherer.util.millisecondsToDuration
import com.theStuffGatherer.util.minutesToDuration
import dev.kord.common.annotation.KordPreview
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.dsl.bot
import org.jetbrains.exposed.sql.transactions.transaction

@OptIn(KordPreview::class)
fun main() {
  Database.init()

  val token = "MTA1MTQ2MDkxMDg2NDE1ODc3MA.GvsU-K.mAuZjSo2lgwD2rKurDZaZvK1_Enh9npVKtlzlE"

  bot(token) {}
}

fun demo() = commands("Demo") {
  slash("Hello", "A 'Hello World' command.") {
    execute {
      respond("Hello World!")
    }
  }
}

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
              "Level: ${skill.level} \nProgress: \n ${calculateProgress(skill.exp.toDouble()/skill.maxExp*100)} (${skill.exp}/${skill.maxExp})[${(skill.exp.toDouble()/skill.maxExp)*100}%]"
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
      var isExploring = false
      var message = ""
      transaction {
        val player = Player.find { PlayersTable.userId eq author.id.value }.first()
        if (player.checkIfExploring()) {
          isExploring = true
          val currentTime = System.currentTimeMillis()
          message = "You are still exploring. Time remaining: ${millisecondsToDuration( player.exploreStartTime + player.exploreDuration - currentTime)}"
          return@transaction
        }
        val skills = Skills.find { SkillsTable.userId eq player.id }.first()
        skills.fishingSkill = skills.addSkillExp("Fishing", 15)
      }
      if (isExploring) {
        respond(message)
        return@execute
      }
      respond("You fished!")
    }
  }
  slash("explore", "Go and explore the world.") {
    execute(IntegerArg("minutes")) {
      val first = args
      transaction {
        val player = Player.find { PlayersTable.userId eq author.id.value }.first()
        player.exploreStartTime = System.currentTimeMillis()
        player.exploreDuration = first.first * 60000L
      }
      respond("You went exploring for ${minutesToDuration(first.first)}!")
    }
  }
}