package me.marvuun

import dev.kord.common.annotation.KordPreview
import me.jakejmattson.discordkt.dsl.bot
import me.marvuun.database.Database
import me.marvuun.threads.createCurrentActivityCheckingThread
import me.marvuun.threads.createCurrentStationUpgradesCheckingThread
import me.marvuun.threads.questUpdaterThread

@OptIn(KordPreview::class)
fun main() {
    Database.init()

    val token = System.getenv("BOT_TOKEN") ?: error("Please set the BOT_TOKEN environment variable!")

    bot(token) {
        prefix { "%" }
        onStart {
            createCurrentActivityCheckingThread()
            createCurrentStationUpgradesCheckingThread()
            questUpdaterThread()
        }
    }
}
