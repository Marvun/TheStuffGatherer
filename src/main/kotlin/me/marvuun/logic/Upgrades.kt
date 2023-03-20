package me.marvuun.logic

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.extensions.toPartialEmoji
import me.marvuun.database.daos.activities.CurrentStationUpgrade
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.daos.stations.Station
import me.marvuun.database.daos.stations.getNextStation
import me.marvuun.enums.StationTypes
import me.marvuun.util.millisecondsToDuration
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun GuildSlashCommandEvent<NoArgs>.openUpgradeMenu() {
  interaction!!.respondPublic {
    actionRow {
      interactionButton(ButtonStyle.Secondary, "homeButton") {
        emoji = Emojis.house.toPartialEmoji()
      }
      interactionButton(ButtonStyle.Secondary, "toolsButton") {
        emoji = Emojis.tools.toPartialEmoji()
      }
    }
    embed {
      title = "What do you want to upgrade?"
      field {
        value = "${Emojis.house}: Home"
      }
      field {
        value = "${Emojis.tools}: Tools"
      }
    }
  }
}

suspend fun openHomeUpgradeMenu(ci: ComponentInteraction) {
  val home = getHome(ci.user)
  val currentStationUpgrade = home.upgradingStation
  if (currentStationUpgrade != null)
    ci.message.edit {
    components = mutableListOf()
      embed {
        title = "You are currently upgrading your ${currentStationUpgrade.getDisplayName()}."
        description = "Time remaining: ${millisecondsToDuration(home.upgradeStartTime + home.upgradeDuration - System.currentTimeMillis())}"
      }
    }
  else
    ci.message.edit {
      embed {
        title = "Home Upgrades"
        description = "Select what you want to upgrade at your home."
      }
      actionRow {
        selectMenu("stationMenu") {
          option("Sawmill", "sawmill") {
            description = "Current level: ${home.sawmillLevel}"
          }
          option("Furnace", "furnace") {
            description = "Current level: ${home.furnaceLevel}"
          }
          option("Stone Cutting Station", "stone cutter") {
            description = "Current level: ${home.stoneCutterLevel}"
          }
        }
      }
    }
}

suspend fun openStationUpgrade(
  station: StationTypes,
  ci: ComponentInteraction
): StationTypes? {

  val home = getHome(ci.user)
  val inventory = transaction { getInventory(ci.user).toList() }
  val levels = getLevels(ci.user)
  val nextStation = getNextStation(station, home)

  if (nextStation == null) {
    ci.message.edit {
      components = mutableListOf()
      embed {
        title = "Your ${station.getDisplayName()} has reached the maximum level!"
      }
    }
    return null
  }
  var hasRequiredLevel = true
  val missingMaterials = nextStation.getMissingResources(inventory)

  if (nextStation.level.value == 1) {
    if (levels.gatheringLevel < nextStation.requiredLevel) hasRequiredLevel = false
  }
  else
    when (station) {
      StationTypes.FURNACE -> if (levels.meltingLevel < nextStation.requiredLevel) hasRequiredLevel = false
      StationTypes.SAWMILL -> if (levels.sawingLevel < nextStation.requiredLevel) hasRequiredLevel = false
      StationTypes.STONECUTTER -> if (levels.stoneCuttingLevel < nextStation.requiredLevel) hasRequiredLevel = false
    }


  ci.message.edit {
    components = mutableListOf()
    embed {
      title = "Next ${station.getDisplayName()} Level: ${nextStation.id}"
      fields =
        buildStationUpgradeEmbedFields(nextStation, hasRequiredLevel, ci.user, missingMaterials, station)

    }
    if (missingMaterials.isEmpty() && hasRequiredLevel)
      actionRow {
        interactionButton(ButtonStyle.Secondary, "doStationUpgrade") {
          emoji = Emojis.whiteCheckMark.toPartialEmoji()
        }
        interactionButton(ButtonStyle.Secondary, "cancelStationUpgrade") {
          emoji = Emojis.x.toPartialEmoji()
        }
      }


  }
  return station
}

fun buildStationUpgradeEmbedFields(
  nextStationLevel: Station<Int>,
  hasRequiredLevel: Boolean,
  user: User,
  missingMaterials: String,
  station: StationTypes
): MutableList<EmbedBuilder.Field> {

  val levels = getLevels(user)
  val fieldList = mutableListOf<EmbedBuilder.Field>()
  val field1 = EmbedBuilder.Field()
  val field2 = EmbedBuilder.Field()

  field1.name = "Needed Resources:"
  field1.value = nextStationLevel.neededResources.map { (short, amount) ->
    "${amount}x ${getResourceFromShort(short).name.value}"
  }.joinToString("\n")

  field2.name = "Construction Time:"
  field2.value = millisecondsToDuration(nextStationLevel.constructionTime)

  fieldList.add(field1)
  fieldList.add(field2)

  if (missingMaterials.isNotEmpty()) {
    val field = EmbedBuilder.Field()
    field.name = "You have enough materials."
    fieldList.add(field)
  }
  else {
    val field = EmbedBuilder.Field()
    field.name = "You don't have enough materials!"
    field.value = "You are missing the following materials:\n$missingMaterials"
    fieldList.add(field)
  }

  if (hasRequiredLevel) {
    val field = EmbedBuilder.Field()
    field.name = "You have a sufficient level."
    fieldList.add(field)
  }
  else {
    val field = EmbedBuilder.Field()
    field.name = "Your level is too low!"
    field.value = if (nextStationLevel.level.value == 1)
      "You are gathering level 0, but level 1 is required."
    else when (station) {
      StationTypes.SAWMILL -> "You are sawing level ${levels.sawingLevel}, but level ${nextStationLevel.requiredLevel} is required."
      StationTypes.FURNACE -> "You are melting level ${levels.meltingLevel}, but level ${nextStationLevel.requiredLevel} is required."
      StationTypes.STONECUTTER -> "You are stone cutting level ${levels.stoneCuttingLevel}, but level ${nextStationLevel.requiredLevel} is required."
    }
    fieldList.add(field)
  }

  if (missingMaterials.isEmpty() && hasRequiredLevel) {
    val field = EmbedBuilder.Field()
    field.name = "Do you want to upgrade your station?"
    fieldList.add(field)
  }

  return fieldList
}

suspend fun cancelStationUpgrade(ci: ComponentInteraction, station: StationTypes?) {
  ci.message.edit {
    components = mutableListOf()
    embed {
      title = "You chose not to upgrade your ${station!!.getDisplayName()}."
    }
  }
}

suspend fun performStationUpgrade(ci: ComponentInteraction, station: StationTypes?) {
  val home = getHome(ci.user)
  val inventory = transaction { getInventory(ci.user).toList() }
  val nextStationLevel = getNextStation(station!!, home)
  val guildId = ci.message.getGuild().id.value
  ci.message.edit {
    components = mutableListOf()
    embed {
      title = "You started to upgrade your ${station.getDisplayName()}."
    }
  }
  transaction {
    home.upgradeStartTime = System.currentTimeMillis()
    home.upgradeDuration = getNextStation(station, home)!!.constructionTime
    home.upgradingStation = station
    CurrentStationUpgrade.new {
      this.guildId = guildId
      channelId = ci.channelId.value
      userId = ci.user.id.value
      activityEnd = home.upgradeDuration + home.upgradeStartTime
    }
    val invEntries =
      inventory.filter { nextStationLevel!!.neededResources.keys.contains(it.itemId) && it.userId == ci.user.id.value }
    invEntries.forEach {
      it.amount = it.amount - nextStationLevel!!.neededResources[it.itemId]!!
    }
  }
}

suspend fun finishStationUpgrade(user: User, channel: MessageChannel) {
  val home = getHome(user)
  val upgradingStation = home.upgradingStation!!

  val newStationLevel = transaction {
    home.upgradingStation = null
    home.upgradeStartTime = 0
    home.upgradeDuration = 0
    return@transaction when (upgradingStation) {
      StationTypes.FURNACE -> {
        home.furnaceLevel += 1
        home.furnaceLevel
      }

      StationTypes.SAWMILL -> {
        home.sawmillLevel += 1
        home.sawmillLevel
      }

      StationTypes.STONECUTTER -> {
        home.stoneCutterLevel += 1
        home.stoneCutterLevel
      }
    }

  }

  channel.createMessage {
    content = user.mention
    embed {
      title = "Your ${upgradingStation.getDisplayName()} finished upgrading!"
      description = "Your ${upgradingStation.getDisplayName()} is now level $newStationLevel."
    }
  }
}