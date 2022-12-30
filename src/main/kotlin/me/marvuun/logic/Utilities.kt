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
      title = "General Info"
      description = "Hi, to get started with the bot simply use the `/start` command. After that you have to go explore with `/explore`. Exploring is important, because that is how you find sites. You can travel to them via the `/travel` command. After reaching a site you can start gathering the local resources with `/gather`.\n\n There is also a level system. Levels determine the amount you get and what kind of resources, you will find. \n\n For more information about specific commands use the following site. \n\n Happy gathering!"
    }

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