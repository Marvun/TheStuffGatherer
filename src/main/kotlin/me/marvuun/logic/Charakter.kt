package me.marvuun.logic

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.*
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.extensions.toPartialEmoji
import me.marvuun.conversations.abandonSiteConversation
import me.marvuun.database.daos.*
import me.marvuun.database.daos.location.*
import me.marvuun.database.daos.resources.Blueprint
import me.marvuun.database.daos.resources.getResourceFromName
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.tables.*
import me.marvuun.database.tables.locations.Cities
import me.marvuun.database.tables.locations.Homes
import me.marvuun.database.tables.resources.Blueprints
import me.marvuun.enums.RarityTypes
import me.marvuun.util.checkUser
import me.marvuun.util.millisecondsToDuration
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.sqrt

private var commandInvoker: User? = null

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

      val city = City.new {
        xCoordinate = 50
        yCoordinate = -25
        name = "Phanotesia"
        cityId = EntityID(UUID.randomUUID(), Cities)
        userId = author.id.value
        travelTime = (sqrt(xCoordinate.toDouble() * xCoordinate.toDouble() + yCoordinate.toDouble() * yCoordinate.toDouble()) * 5000).toInt()
        quests = mutableListOf()
        purchasableItems = generatePurchasableItems()
      }
      city.quests = generateQuests(author, city)

      "You started your journey!"
    } else "You already started your journey!"
  }
  interaction!!.respondPublic {
    embed {
      title = response
    }
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

suspend fun getLocation(ci: GuildApplicationCommandInteraction) {
  val player = getPlayer(ci.user)

  if (player.currentLocation == null) {
    ci.respondPublic {
      embed {
        title = "You are in the middle of nowhere."
      }
    }
    return
  }
  val location = getLocationFromUUID(ci.user.id.value, player.currentLocation!!)

  when (location) {
    is Home -> {
      ci.respondPublic {
        embed {
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
            name = "Stone Cutter"
            value = "Level: ${location.stoneCutterLevel}"
          }
        }
      }
      return
    }
    is City -> {
      openCityMenu(location, ci)
      return
    }
    is Site -> {
      val totalResources = transaction { location.totalResources }
      val currentResources = transaction { location.currentResources }
      val isBusy = transaction { player.currentActivityType } != null
      ci.respondPublic {
        embed {
          title = "You are currently at a ${location.type.name.lowercase()}.\n"
          description = "The number represents how many times you can gather the resource here."
          totalResources.forEach { (short, amount) ->
            val resource = getResourceFromShort(short)
            field {
              name = resource.name.value
              value = "${currentResources[resource.short] ?: "0"}/$amount"
            }
          }
          if (isBusy)
            field {
              name = "You can't gather right now, you are busy."
            }
        }
        actionRow {
          interactionButton(ButtonStyle.Secondary, "openGatheringMenu") {
            disabled = isBusy
            label = "Start Gathering"
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

suspend fun printInventory(gi: GuildApplicationCommandInteraction) {
  val inventory = transaction { getInventory(gi.user).toList() }

  if (inventory.isEmpty()) {
    gi.respondPublic {
      embed {
        title = "Your inventory is empty."
      }
    }
    return
  }

  gi.respondPublic {
    embed {
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
}

suspend fun GuildSlashCommandEvent<NoArgs>.abandonSite() =
  abandonSiteConversation().startSlashResponse(discord, author, this)


suspend fun openPlayerQuestMenu(ci: ActionInteraction, page: Int) {
  commandInvoker = ci.user
  val menu = buildPlayerQuestMenu(ci)

  if (ci is ComponentInteraction)
    menu.defaultPageIndex = (ci.message.embeds[0].title!!.split(" ").last().toIntOrNull() ?: 1) - 1
  else
    menu.defaultPageIndex = 0

  menu.navigate(page)
  if (ci is GuildApplicationCommandInteraction)
    ci.respondPublic(menu.getPage())
  else {
    ci as ComponentInteraction
    ci.updatePublicMessage(menu.getPage())
  }


}
private suspend fun buildPlayerQuestMenu(ci: ActionInteraction): MyMenu  {
  val player = getPlayer(ci.user)
  val inventory = getInventory(ci.user)

  return myMenu {
    if (player.quests.isEmpty())
      page {
        embed {
          title = "You don't have any quests right now."
          description = "Travel to a city and check the local quests."
        }
      }
    else {
      player.quests.forEachIndexed { index, quest ->
        var canBeFinished = transaction { player.currentLocation == quest.city.cityId.value }
        if (canBeFinished) checkIfBusy(ci)
        val resourceStrings = mutableListOf<String>()

        quest.wantedItems.forEach { (short, amount) ->
          val invEntry = transaction { inventory.find { it.itemId == short } }
          if (invEntry == null) {
            canBeFinished = false
            resourceStrings.add("${getResourceFromShort(short).name.value}: 0/$amount")
          } else
            resourceStrings.add("${getResourceFromShort(short).name.value}: ${invEntry.amount}/$amount")
        }

        page {
          embed {
            title = "Quest ${index + 1}"

            footer {
              text = "Page: ${index + 1}/${player.quests.size}"
            }

            field {
              name = "Needed Resources:"
              value = resourceStrings.joinToString("\n")
            }

            field {
              name = "Reward:"
              value = "${quest.money} Coins"
            }

            if (quest.timeLimit != null) {

              if (quest.timeLimit!! < System.currentTimeMillis()) {
                field {
                  name = "~~Special Rewards:~~"
                  value = "~~${quest.specialRewards}~~"
                }

                field {
                  name = "~~Time Limit:~~"
                  value = if (quest.timeLimit!! - System.currentTimeMillis() > 0) "~~${millisecondsToDuration(quest.timeLimit!! - System.currentTimeMillis())}~~" else "~~0s~~"
                }
              }
              else {
                field {
                  name = "Special Rewards:"
                  value = quest.specialRewards
                }

                field {
                  name = "Time Limit:"
                  value = millisecondsToDuration(quest.timeLimit!! - System.currentTimeMillis())
                }
              }
            }

            field {
              name = "City:"
              value = transaction { quest.city.name }
            }
          }
          actionRow {
            interactionButton(ButtonStyle.Secondary, "previousPlayerQuestPage") {
              emoji = Emojis.arrowLeft.toPartialEmoji()
              label = "Left"
            }
            interactionButton(ButtonStyle.Secondary, "finishQuest") {
              disabled = !canBeFinished
              emoji = Emojis.whiteCheckMark.toPartialEmoji()
              label = "Finish"
            }
            interactionButton(ButtonStyle.Secondary, "nextPlayerQuestPage") {
              emoji = Emojis.arrowRight.toPartialEmoji()
              label = "Right"
            }
          }
        }
      }
    }
  }
}

suspend fun finishQuest(ci: ComponentInteraction) {
  if (!checkUser(ci, commandInvoker!!)) return

  transaction {

    val player = getPlayer(ci.user)
    val inventory = getInventory(ci.user)
    val quest = Quest.find { Quests.id eq player.quests[ci.message.embeds[0].title!!.split(" ").last().toInt() - 1].id }.first()

      quest.wantedItems.forEach { (short, amount) ->
        val invEntry = inventory.find { it.itemId == short }!!
        invEntry.amount -= amount
      }
      player.money += quest.money

      if (quest.timeLimit != null && quest.timeLimit!! >= System.currentTimeMillis()) {

        when {
          quest.specialRewards.contains("Site") -> {
            generateSites(getPlayer(ci.user), ci.user, 1, RarityTypes.S)
          }

          else -> {
            val itemShort: String
            val amount: Int

            if (quest.specialRewards.contains("S-Tier")) {

              val blueprint = Blueprint.find { Blueprints.id eq quest.specialRewards.slice(3..quest.specialRewards.length) }.first()
              itemShort = blueprint.short
              amount = 1

            } else {

              val regex = Regex("\\d+x ")
              val resource = getResourceFromName(quest.specialRewards.replace(regex, ""))

              itemShort = (resource.short)
              amount = regex.find(quest.specialRewards)!!.value.removeSuffix("x ").toInt()
            }

            val invEntry = inventory.find { it.itemId == itemShort }

            if (invEntry != null)
              invEntry.amount += amount
            else
              Inventory.new {
                userId = ci.user.id.value
                itemId = itemShort
                this.amount = amount
              }
          }
        }
      }

      val quests = player.quests
      quests.remove(quest)
      player.quests = quests
      quest.delete()

  }
}