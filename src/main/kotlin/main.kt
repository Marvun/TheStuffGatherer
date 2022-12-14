package com.theStuffGatherer


import com.theStuffGatherer.databass.Database
import com.theStuffGatherer.logic.getRewards
import dev.kord.common.annotation.KordPreview
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.dsl.bot

@OptIn(KordPreview::class)
fun main(args: Array<String>) {
  Database.init()

  val token = args[0]

  bot(token) {}
}

fun demo() = commands("Demo") {
  slash("testme", "A 'Hello World' command.") {
    execute {
      val rewards = getRewards(600L)

      respond{
        for (entry in rewards) {
          val rarities = entry.value.groupingBy { it }.eachCount()
          val formattedRarities = mutableListOf<String>()
          rarities.forEach {
            formattedRarities.add("${it.value}x ${it.key.name}")
          }

          field {
            name = "__${entry.key.name}__"
            value = formattedRarities.joinToString("\n")
          }
        }
      }
    }
  }
}



