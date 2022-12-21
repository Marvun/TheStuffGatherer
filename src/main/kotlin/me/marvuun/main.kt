package me.marvuun


import dev.kord.common.annotation.KordPreview
import me.jakejmattson.discordkt.dsl.bot
import me.marvuun.database.Database


@OptIn(KordPreview::class)
fun main(args: Array<String>) {
  Database.init()

  val token = args[0]

  bot(token) {
    prefix { "%" }
  }
}
