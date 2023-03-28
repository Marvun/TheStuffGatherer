package me.marvuun.logic

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.GuildApplicationCommandInteraction
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.extensions.toPartialEmoji

private var commandInvoker: User? = null
suspend fun showHelp(ci: ActionInteraction, discord: Discord, page: Int) {
  commandInvoker = ci.user

  val menu = buildHelpMenu(discord)

  if (ci is ComponentInteraction)
    menu.defaultPageIndex = (ci.message.embeds[0].footer!!.text.split(" ").last().split("/").first().toIntOrNull() ?: 1) - 1
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

suspend fun buildHelpMenu(discord: Discord): MyMenu {
  val commands = discord.commands
  val activityCommands = commands.filter { it.category == "Activity" }
  val characterCommands = commands.filter { it.category == "Character" }
  return myMenu {
    page {
      embed {
        title = "General Info"
        description = "Hi, to get started with the bot simply use the `/start` command. After that you have to go explore with `/explore`. Exploring is important, because that is how you find sites. You can travel to them via the `/travel` command. After reaching a site you can start gathering the local resources with `/gather`.\n\n There is also a level system. Levels determine the amount you get and what kind of resources, you will find. \n\n For more information about specific commands use the following site. \n\n Happy gathering!"
        footer {
          text = "Page 1/3"
        }
      }
      actionRow {
        interactionButton(ButtonStyle.Secondary,"previousHelpPage") {
          emoji =  Emojis.arrowLeft.toPartialEmoji()

        }
        interactionButton(ButtonStyle.Secondary,"nextHelpPage") {
          emoji =  Emojis.arrowRight.toPartialEmoji()
        }
      }
     }

    page {
      embed {
        title = "Activity Commands"
        activityCommands.forEach {
          field {
            name = "`/${it.name}`"
            value = it.description
          }
        }
        footer {
          text = "Page 2/3"
        }
      }
      actionRow {
        interactionButton(ButtonStyle.Secondary,"previousHelpPage") {
          emoji =  Emojis.arrowLeft.toPartialEmoji()

        }
        interactionButton(ButtonStyle.Secondary,"nextHelpPage") {
          emoji =  Emojis.arrowRight.toPartialEmoji()
        }
      }
    }

    page {
      embed {
        title = "Character Commands"
        characterCommands.forEach {
          field {
            name = "`/${it.name}`"
            value = it.description
          }
        }
        footer {
          text = "Page 3/3"
        }
      }
      actionRow {
        interactionButton(ButtonStyle.Secondary,"previousHelpPage") {
          emoji =  Emojis.arrowLeft.toPartialEmoji()

        }
        interactionButton(ButtonStyle.Secondary,"nextHelpPage") {
          emoji =  Emojis.arrowRight.toPartialEmoji()
        }
      }
    }
  }
}