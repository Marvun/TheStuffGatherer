package me.marvuun.logic

import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.conversations.abandonSiteConversation
import me.marvuun.database.daos.*
import me.marvuun.database.daos.location.City
import me.marvuun.database.daos.location.Home
import me.marvuun.database.daos.location.Site
import me.marvuun.database.daos.location.getLocationFromUUID
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.tables.*
import me.marvuun.database.tables.locations.Cities
import me.marvuun.database.tables.locations.Homes
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.sqrt

suspend fun GuildSlashCommandEvent<NoArgs>.startJourney() {
  val response = transaction {

    if (Player.findById(author.id.value) == null) {

      val homeUUID = UUID.randomUUID()

      Player.new {
        userId = EntityID(author.id.value, Players)
        currentLocation = homeUUID
        occupiedCoordinates = mutableListOf(mutableListOf(50, -25))
      }

      PlayerSite.new {
        userId = EntityID(author.id.value, PlayerSites)
      }

      Level.new {
        userId = EntityID(author.id.value, Levels)
      }

      Home.new {
        xCoordinate = 0
        yCoordinate = 0
        homeId = EntityID(homeUUID, Homes)
        userId = author.id.value
      }

      City.new {
        xCoordinate = 50
        yCoordinate = -25
        name = "Phanotesia"
        cityId = EntityID(UUID.randomUUID(), Cities)
        userId = author.id.value
        travelTime = (sqrt(xCoordinate.toDouble() * xCoordinate.toDouble() + yCoordinate.toDouble() * yCoordinate.toDouble()) * 10000).toInt()
        buyers = generateBuyers(author)
        purchasableItems = mutableMapOf()
      }

      "You started your journey!"
    } else "You already started your journey!"
  }
  respond {
    title = response
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.printSites() {
  val sites = getSites(author)

  respond {
    if (sites.isEmpty()){
      title = "You don't have any discovered sites at the moment."
    }
    else {
      title = "You have discovered the following sites:\n"
      sites.forEach { (key, value) ->
        field {
          name = "${key.name.lowercase()}s: ${value.size}"
        }
      }
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.getLocation() {
  val player = getPlayer(author)

  if (player.currentLocation == null) {
    respond {
      title = "You are in the middle of nowhere."
    }
    return
  }
  val location = getLocationFromUUID(author.id.value, player.currentLocation!!)

  when (location) {
    is Home -> {
      respond {
        title = "You are currently at home."

        field {
          inline = true
          name = "Furnace"
          value = "Level: ${location.furnaceLevel}"
        }

        field {
          inline = true
          name = "Sawmill"
          value = "Level: ${location.sawmillLevel}"
        }

        field {
          inline = true
          name = "Stone-Cutting Station"
          value = "Level: ${location.stoneCutterLevel}"
        }
      }
      return
    }
    is City -> {
      respond {
        title = "You are currently in ${location.name}"
      }
      return
    }
    is Site -> {
      val totalResources = transaction { location.totalResources }
      val currentResources = transaction { location.currentResources }

      respond {
        title = "You are currently at a ${location.type.name.lowercase()}.\n"
        description = "The number represents how many times you can gather the resource here."
        footer {
          text = "Hint: Use /gather to start gathering."
        }
        totalResources.forEach { (short, amount) ->
          val resource = getResourceFromShort(short)
          field {
            name = resource.name.value
            value = "${currentResources[resource.short]}/$amount"
          }
        }
      }
    }
  }

}

suspend fun GuildSlashCommandEvent<NoArgs>.printLevels() {
  val levels = getLevels(author)

  respond {
    transaction {

      title = "You have the following levels:\n"
      field {
        name = "__Gathering__ : ${levels.gatheringLevel}"
        value =
          "${levels.currentGatheringExp}/${levels.neededGatheringExp}"
      }
      field {
        name = "__Mining__ : ${levels.miningLevel}"
        value = "${levels.currentMiningExp}/${levels.neededMiningExp}"
      }

      field {
        name = "__Extraction__ : ${levels.extractionLevel}"
        value = "${levels.currentExtractionExp}/${levels.neededExtractionExp}"
      }

      field {
        name = "__Woodcutting__ : ${levels.woodcuttingLevel}"
        value = "${levels.currentWoodcuttingExp}/${levels.neededWoodcuttingExp}"
      }

      field {
        name = "__Harvesting__ : ${levels.harvestingLevel}"
        value = "${levels.currentHarvestingExp}/${levels.neededHarvestingExp}"
      }

      field {
        name = "__Botany__ : ${levels.botanyLevel}"
        value = "${levels.currentBotanyExp}/${levels.neededBotanyExp}"
      }

      field{
        name = "__Fishing__ : ${levels.fishingLevel}"
        value = "${levels.currentFishingExp}/${levels.neededFishingExp}"
      }

      field {
        name = "__Melting__ : ${levels.meltingLevel}"
        value = "${levels.currentMeltingExp}/${levels.neededMeltingExp}"
      }
    }

  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.printInventory() {
  val inventory = transaction { getInventory(author).toList() }

  if (inventory.isEmpty()) {
    respond {
      title = "Your inventory is empty."
    }
    return
  }

  respond {
    title = "You have the following items in your inventory:"

    val items = mutableListOf<String>()

    transaction {
      inventory.forEach {
        items.add("${it.amount}x ${getResourceFromShort(it.itemId).name.value}")
      }
    }

    field {
      name = items.joinToString("\n")
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.abandonSite() =
  abandonSiteConversation().startSlashResponse(discord, author, this)


