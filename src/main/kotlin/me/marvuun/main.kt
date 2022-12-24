package me.marvuun


import dev.kord.common.annotation.KordPreview
import me.jakejmattson.discordkt.dsl.bot
import me.marvuun.database.Database


@OptIn(KordPreview::class)
fun main() {
  Database.init()

  val token = System.getenv("BOT_TOKEN") ?: error("Please set the BOT_TOKEN environment variable!")

  bot(token) {
    prefix { "%" }
  }
}
