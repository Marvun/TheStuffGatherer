package com.theStuffGatherer.commands

import com.theStuffGatherer.DTOs.MineDTO
import com.theStuffGatherer.databass.DAOs.Player
import com.theStuffGatherer.databass.DAOs.Sites
import com.theStuffGatherer.databass.DAOs.sites.Mines
import com.theStuffGatherer.databass.tables.PlayersTable
import com.theStuffGatherer.databass.tables.SitesTable
import com.theStuffGatherer.databass.tables.sites.MinesTable
import com.theStuffGatherer.enums.ActivityTypes
import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.logic.generateTravelTime
import com.theStuffGatherer.util.aOrAn
import com.theStuffGatherer.util.millisecondsToDuration
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.conversations.conversation
import org.jetbrains.exposed.sql.transactions.transaction

fun sites() = commands("Sites") {
  slash("sites") {
    execute {
      val sites: Sites = transaction {
        val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
        Sites.find { SitesTable.userId eq player.id }.first()

      }
      if (sites.sites.values.all { it.isEmpty() }) {
        respond {
          title = "You haven't found any sites yet. Go explore with `/explore`!"
        }
        return@execute
      }
      respondMenu {

        sites.sites.forEach {
          if (it.value.isNotEmpty()) {
            page {
              title = "You found ${it.key}:"
              it.value.forEach { (rarity, count) ->
                field {
                  name = "${rarity.name}:${count}"
                }
              }
            }
          }
        }
        buttons {
          button("Left", Emojis.arrowLeft) {
            previousPage()
          }

          button("Right", Emojis.arrowRight) {
            nextPage()
          }
        }
      }
    }
  }
  slash("selectmine") {
    execute(AnyArg("rarity")) {
      respond {
        transaction {
          val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
          val sites = Sites.find { SitesTable.userId eq player.id }.first()
          val availableMines = sites.mines
          val currentMines = Mines.find { MinesTable.userId eq player.id }.first()
          if (!args.first.matches(Regex("[sSaAbBcCdD]"))) {
            title = "Please enter a valid rarity!"
            return@transaction
          }
          val rarity = RarityTypes.getFromString(args.first)
          if (availableMines[rarity] == null) {
            title = "You don't have any ${args.first.uppercase()} tier mines!"
            return@transaction
          }
          val resources = Mines.generateResources(rarity)
          val newMine = MineDTO("Mine123", rarity, resources, resources, generateTravelTime(rarity))
          val currentMine = currentMines.getCurrentMineByRarity(rarity)
          if (currentMine.rarity != RarityTypes.NONE) {
            title = "You already have ${aOrAn(currentMine.rarity.name)} ${currentMine.rarity.name}-tier mine selected! Either abandon or clear it first!"
            return@transaction
          }
          when (rarity.name) {
            "S" -> currentMines.currentSMine = newMine
            "A" -> currentMines.currentAMine = newMine
            "B" -> currentMines.currentBMine = newMine
            "C" -> currentMines.currentCMine = newMine
            "D" -> currentMines.currentDMine = newMine
          }
          title = "You have selected ${aOrAn(currentMine.rarity.name)} ${newMine.rarity.name}-tier mine!"
        }
      }
    }
  }
  slash("travel") {
    execute {
      val player =
        transaction {
          Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
        }
      if (player.currentActivityType != ActivityTypes.NOTHING) {
        respond {
          title = "You can't travel right now, because you you are ${player.currentActivityType.name.lowercase()}."
        }
        return@execute
      }
      travelConversation().startSlashResponse(discord, author, this)
    }
  }
}

fun travelConversation() = conversation(promptTimeout = 30) {

  val promptMessage = "To what kind of mine do you want to travel? Please enter a valid rarity from `D` to `S`."
  val promptErrorMessage = "Please enter a valid rarity!"
  val rarityString = promptUntil(AnyArg, promptMessage, promptErrorMessage) {
      it.uppercase().matches(Regex("[SABCD]"))
  }.uppercase()
  val rarity = RarityTypes.getFromString(rarityString)
  val currentMine = transaction {
    val player = Player.find { PlayersTable.userId eq user.id.value }.first()
    val currentMines = Mines.find { MinesTable.userId eq player.id }.first()
    when (rarity) {
      RarityTypes.S -> currentMines.currentSMine
      RarityTypes.A -> currentMines.currentAMine
      RarityTypes.B -> currentMines.currentBMine
      RarityTypes.C -> currentMines.currentCMine
      RarityTypes.D -> currentMines.currentDMine
      RarityTypes.NONE -> MineDTO()

    }
  }
  val response = if (currentMine.rarity == RarityTypes.NONE) {
    "You don't have ${aOrAn(currentMine.rarity.name)} ${rarity}-tier mine discovered right now!"
  } else {
    promptButton {
      embed {
        title = "You have selected ${aOrAn(currentMine.rarity.name)} ${rarity}-tier mine!"
        field {
          name = "You would need ${millisecondsToDuration(currentMine.travelTime)} to travel there."
          value = "Do you want to travel?"
        }
      }
      buttons {
        button("Yes", Emojis.whiteCheckMark, "You are now traveling to the mine.")
        button("No", Emojis.x, "You won't travel to the mine.")
      }
    }
  }
  if (response == "You are now traveling to the mine.") {
    transaction {
      val player = Player.find { PlayersTable.userId eq user.id.value }.first()
//      val currentMines = Mines.find { MinesTable.userId eq player.id }.first()
      player.currentActivityType = ActivityTypes.TRAVELING
      player.activityDuration = currentMine.travelTime
      player.activityStartTime = System.currentTimeMillis()
      player.currentActivity = "traveling to ${currentMine.rarity.name}-Tier Mine"
//      when (rarity) {
//        "S", "s" -> currentMines.currentSMine = MineDTO()
//        "A", "a" -> currentMines.currentAMine = MineDTO()
//        "B", "b" -> currentMines.currentBMine = MineDTO()
//        "C", "c" -> currentMines.currentCMine = MineDTO()
//        "D", "d" -> currentMines.currentDMine = MineDTO()
//      }
      player.destination = "${rarity}-Tier Mine"
    }
  }
  respond {
    title = response
  }

}





