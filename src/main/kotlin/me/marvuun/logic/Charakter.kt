package me.marvuun.logic

import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.conversations.abandonSiteConversation
import me.marvuun.database.daos.*
import me.marvuun.database.tables.Homes
import me.marvuun.database.tables.Levels
import me.marvuun.database.tables.PlayerSites
import me.marvuun.database.tables.Players
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

suspend fun GuildSlashCommandEvent<NoArgs>.startJourney() {
  val response = transaction {

    if (Player.findById(author.id.value) == null) {

      val homeUUID = UUID.randomUUID()

      Player.new {
        userId = EntityID(author.id.value, Players)
        currentLocation = homeUUID
      }

      PlayerSite.new {
        userId = EntityID(author.id.value, PlayerSites)
      }

      Level.new {
        userId = EntityID(author.id.value, Levels)
      }

      Home.new {
        homeId = EntityID(homeUUID, Homes)
        userId = author.id.value
      }

      "You started your journey!"
    } else "You already started your journey!"
  }
  respond {
    title = response
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.printSites() {
  val sites = getSites()

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
  val player = getPlayer()

  if (player.currentLocation == null) {
    respond {
      title = "You are in the middle of nowhere."
    }
    return
  }

  if (player.currentLocation == getHome().homeId.value) {
    respond {
      title = "You are currently at home."
    }
    return
  }


  val site = getSite()
  val totalResources = transaction { site.totalResources }
  val currentResources = transaction { site.currentResources }

  respond {
    title = "You are currently at a ${site.type.name.lowercase()}.\n"
    description = "The number represents how many times you can gather the resource here."
    footer {
      text = "Hint: Use /gather to start gathering."
    }
    totalResources.forEach { (short, amount) ->
      val resource = Resource.getResourceFromShort(short)
      field {
        name = resource.name.value
        value = "${currentResources[resource.short]}/$amount"
      }
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.printLevels() {
  val levels = getLevels()

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
    }

  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.printInventory() {
  val inventory = transaction { getInventory().toList() }

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
        items.add("${it.amount}x ${Resource.getResourceFromShort(it.itemId).name.value}")
      }
    }

    field {
      name = items.joinToString("\n")
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.abandonSite() =
  abandonSiteConversation().startSlashResponse(discord, author, this)


