package me.marvuun


import dev.kord.common.annotation.KordPreview
import io.ktor.client.*
import me.jakejmattson.discordkt.dsl.bot
import me.marvuun.database.Database
import me.marvuun.threads.createCurrentActivityCheckingThread
import me.marvuun.threads.createCurrentStationUpgradesCheckingThread
import java.util.*


@OptIn(KordPreview::class)
fun main() {
  Database.init()

  val token = System.getenv("BOT_TOKEN") ?: error("Please set the BOT_TOKEN environment variable!")

  bot(token) {
    prefix { "%" }
    onStart {
      createCurrentActivityCheckingThread()
      createCurrentStationUpgradesCheckingThread()
    }
  }
}


