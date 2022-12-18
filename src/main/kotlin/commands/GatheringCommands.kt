package com.theStuffGatherer.commands

import com.theStuffGatherer.databass.DAOs.Player
import com.theStuffGatherer.databass.DAOs.sites.calculateMiningDuration
import com.theStuffGatherer.enums.ActivityTypes
import com.theStuffGatherer.databass.tables.PlayersTable
import com.theStuffGatherer.enums.resourceTypes.OreTypes
import com.theStuffGatherer.logic.getMines
import com.theStuffGatherer.logic.getPlayer
import com.theStuffGatherer.util.millisecondsToDuration
import com.theStuffGatherer.util.minutesToDuration
import com.theStuffGatherer.util.promptUntilAsEmbed
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.conversations.conversation
import org.jetbrains.exposed.sql.transactions.transaction

fun gathering() = commands("Gathering") {
  slash("mine") {
    execute {

      val player = transaction {
        getPlayer(this@execute.author.id)
      }

      if (!player.currentLocation.contains("Mine")) {
        respond {
          title = "You are not at a mine!"

        }
      } else {
        mineConversation().startSlashResponse(discord, author, this)
      }

    }
  }
//  slash("chop") {
//    execute {
//      transaction {
//        val player = Player.find { PlayersTable.userId eq author.id.value }.first()
//        val skills = Skills.find { SkillsTable.userId eq player.id }.first()
//        skills.choppingSkill = skills.addSkillExp("Chopping", 15)
//      }
//      respond("You chopped!")
//    }
//  }
//  slash("fish") {
//    execute {
//      var isBusy = false
//      var message = ""
//      respond {
//        transaction {
//          val player = Player.find { PlayersTable.userId eq this@execute.author.id.value }.first()
//          if (player.currentActivity != ActivityTypes.NOTHING) {
//            isBusy = true
//            message =
//              "You are still ${player.currentActivity.name.lowercase()}. Time remaining: ${millisecondsToDuration(player.activityStartTime + player.activityDuration - System.currentTimeMillis())}"
//            return@transaction
//          }
//          val skills = Skills.find { SkillsTable.userId eq player.id }.first()
//          skills.fishingSkill = skills.addSkillExp("Fishing", 15)
//        }
//        title = if (isBusy) message else "You fished!"
//
//      }
//
//    }
//  }
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
          player.currentActivityType = ActivityTypes.EXPLORING
        }
        title = "You went exploring for ${minutesToDuration(args.first)}!"
      }
    }
  }

}

fun GuildSlashCommandEvent<NoArgs>.mineConversation() = conversation("cancel", promptTimeout = 30) {

  val player = getPlayer(this@mineConversation.author.id)
  val mines = getMines(player)
  val mine = mines.getMine(player.currentLocation)
  var tempCurrentResources = mine.currentResources.toMutableMap()
  var isValid = true
  var selections = mutableMapOf<OreTypes, Int>()

  if (tempCurrentResources.filterValues { it != 0 }.isEmpty()) {
    respond {
      title = "There aren't any resources left at: `${player.currentLocation}`."
    }
    return@conversation
  }

  while (isValid) {

    val result = promptSelect {

      content {
        title = "What do you want to mine?"
        description = "Select an ore or `All`, if you want to mine all available ores."
      }

      option("All", "ALL", "${tempCurrentResources.values.sum()}/${mine.totalResources.values.sum()}")

      tempCurrentResources.forEach { (key, value) ->
        option(key.getDisplayName(), key.short, "Available: $value/${mine.totalResources[key]}")
      }

    }.first()

    if (result == "ALL") {
      selections = mine.currentResources.toMutableMap()
      break
    }

    val oreType = OreTypes.getFromShort(result)

    if (selections[oreType] == null) selections[oreType] = 0

    val amount = this.promptUntilAsEmbed(AnyArg, "Please enter a valid amount!", {
      it.toIntOrNull() != null && it.toInt() <= tempCurrentResources[oreType]!!
    }, {
      title = "How much do you want to mine?"
      description = "Available: ${tempCurrentResources[oreType]}/${mine.totalResources[oreType]}"

    }).toInt()

    tempCurrentResources[oreType] = tempCurrentResources[oreType]!! - amount

    selections[oreType] = selections[oreType]!! + amount

    tempCurrentResources = tempCurrentResources.filterValues { it != 0 } as MutableMap<OreTypes, Int>

    if (tempCurrentResources.isEmpty()) {
      transaction {
        player.currentLocation = "Depleted Mine"
        mines.setEmptyMine(mine.rarity)
      }
      break
    }

    isValid = promptButton {
      embed {
        title = "Do you want to mine other ore too?"
      }

      buttons {
        button("Yes", Emojis.whiteCheckMark, true)
        button("No", Emojis.x, false)
      }
    }
  }
  selections.forEach { (oreType, amount) ->
    mine.currentResources[oreType] = mine.currentResources[oreType]!! - amount
  }
  transaction {
    mines.setMine(mine)
  }

  val duration = calculateMiningDuration(selections)

  transaction {

    player.currentActivityType = ActivityTypes.MINING
    player.currentActivity = "mining at ${mine.rarity.name}-Tier Mine"
    player.currentlyGathering = selections.mapKeys { it.key.short }.toString()
    player.activityDuration = duration
    player.activityStartTime = System.currentTimeMillis()

  }

  respond {
    title = "You went mining for ${millisecondsToDuration(duration)}."
  }

}


