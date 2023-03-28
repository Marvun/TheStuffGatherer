package me.marvuun.logic

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.GuildApplicationCommandInteraction
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.extensions.toPartialEmoji
import me.marvuun.database.daos.Quest
import me.marvuun.database.daos.location.City
import me.marvuun.database.daos.resources.getResourcesDisplayName
import me.marvuun.database.tables.Quests
import me.marvuun.database.tables.locations.Cities
import me.marvuun.util.checkUser
import me.marvuun.util.millisecondsToDuration
import org.jetbrains.exposed.sql.transactions.transaction

private var commandInvoker: User? = null
suspend fun openCityMenu(city: City, ci: GuildApplicationCommandInteraction) {
  commandInvoker = ci.user
  ci.respondPublic {
    embed {
      title = "You are currently in ${city.name}."
      description = "What do you want to do?"
    }
    actionRow {
      stringSelect("cityMenuSelect") {

        option("Quests", "quests") {
          description = "Available: ${city.quests.size}"
        }
        option("Buy Items", "buying")
      }
    }
  }
}

suspend fun openCityQuestMenu(ci: ComponentInteraction, page: Int) {
  if (!checkUser(ci, commandInvoker!!)) return

  val menu = buildCityQuestMenu(ci)
  menu.defaultPageIndex = (ci.message.embeds[0].title!!.split(" ").last().toIntOrNull() ?: 1) - 1
  menu.navigate(page)

  ci.updatePublicMessage(menu.getPage())

}

suspend fun acceptQuest(ci: ComponentInteraction) {
  if (!checkUser(ci, commandInvoker!!)) return
  val player = getPlayer(ci.user)

  transaction {
    val city =  City.find { Cities.id eq player.currentLocation }.first()
    val quest = Quest.find { Quests.id eq city.quests[ci.message.embeds[0].title!!.split(" ").last().toInt() - 1].id }.first()
    var quests = player.quests

    quests.add(quest)
    player.quests = quests
    quests = city.quests
    quests.remove(quest)
    city.quests = quests
  }

}

suspend fun openCityBuyingMenu(ci: ComponentInteraction) {
  if (!checkUser(ci, commandInvoker!!)) return

  val player = getPlayer(ci.user)
  val city = transaction { City.find { Cities.id eq player.currentLocation }.first() }

}
private suspend fun buildCityQuestMenu(ci: ComponentInteraction): MyMenu {
  val player = getPlayer(ci.user)
  val city = transaction { City.find { Cities.id eq player.currentLocation }.first() }
  val hasMaxQuests = player.quests.size >= 10
  return myMenu {
    if (city.quests.isEmpty())
      page {
        embed {
          title = "You already accepted all quests in this city!"
          field {
            val currentTime = System.currentTimeMillis()
            val startNextDay = 86400000 * (currentTime / 86400000) + 86400000
            name = "New quests in:"
            value = millisecondsToDuration(startNextDay - currentTime)
          }
        }
      }
    else {
      val totalPageCount = player.quests.size
      city.quests.forEachIndexed { index, quest ->
        page {
          embed {
            title = "Quest ${index + 1}"

            footer {
              text = "Page: ${index + 1}/${city.quests.size}"
            }

            field {
              name = "Needed Resources:"
              value = getResourcesDisplayName(quest.wantedItems, "\n")
            }

            field {
              name = "Reward:"
              value = "${quest.money} Coins"
            }

            if (quest.timeLimit != null) {

              field {
                name = " Special Rewards:"
                value = quest.specialRewards
              }

              field {
                name = "Time Limit:"
                value = millisecondsToDuration(quest.timeLimit!! - System.currentTimeMillis())
              }
            }

            if (hasMaxQuests)
              field {
                name = "You have the maximum amount of quests!"
              }

            footer {
              text = "Page: ${index + 1}/$totalPageCount"
            }
          }
          actionRow {
            interactionButton(ButtonStyle.Secondary, "previousCityQuestPage") {
              emoji = Emojis.arrowLeft.toPartialEmoji()
              label = "Left"
            }
            interactionButton(ButtonStyle.Secondary, "acceptQuest") {
              disabled = hasMaxQuests
              emoji = Emojis.whiteCheckMark.toPartialEmoji()
              label = "Accept"
            }
            interactionButton(ButtonStyle.Secondary, "nextCityQuestPage") {
              emoji = Emojis.arrowRight.toPartialEmoji()
              label = "Right"
            }
          }
        }
      }
    }
  }
}