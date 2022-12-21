package me.marvuun.logic

import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent

suspend fun  GuildSlashCommandEvent<NoArgs>.showHelp() {
  val commands = discord.commands
  val activityCommands = commands.filter { it.category == "Activity" }
  val characterCommands = commands.filter { it.category == "Character" }
  respondMenu {

    page {
      title = "Activity Commands"
      activityCommands.forEach {
        field {
          name = "`/${it.name}`"
          value = it.description
        }
      }
    }

    page {
      title = "Character Commands"
      characterCommands.forEach {
        field {
          name = "`/${it.name}`"
          value = it.description
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