package me.marvuun


import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.conversations.conversation
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.extensions.toPartialEmoji
import me.marvuun.database.Database


@OptIn(KordPreview::class)
fun main(args: Array<String>) {
  Database.init()

  val token = args[0]

  bot(token) {
    prefix { "%" }
  }
}

fun demo() = commands("Demo") {
  slash("testme", "A 'Hello World' command.") {
    execute {
      val result = testconvo().startSlashResponse(discord, author, this)
      println(result)
    }
  }
}

fun testconvo() = conversation("exit", 30) {
  val name = prompt(AnyArg, "What is your name?")

  val age = prompt(IntegerArg) {
    title = "How old are you?"
  }

  val response = promptButton {
    embed {
      title = "Do you like DiscordKt?"
      color = Color(0x00bfff)
    }

    buttons {
      button("Yes", Emojis.whiteCheckMark, "Glad you like it.")
      button("No", Emojis.x, "You should let me know how to fix the lib.")
    }
  }

  val selection = promptSelect {
    this.selectionCount = 1..1

    content {
      title = "Selection"
      description = "What's your favorite letter?"
    }

    option("A", description = "The first letter", emoji = Emojis.regionalIndicatorA.toPartialEmoji())
    option("B", description = "The second letter", emoji = Emojis.regionalIndicatorB.toPartialEmoji())
    option("C", description = "The third letter", emoji = Emojis.regionalIndicatorC.toPartialEmoji())
  }

  respond("Nice to meet you $name ($age)! $response ${selection.first()} is my favorite letter too.")
}

