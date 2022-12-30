package me.marvuun


import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.dsl.bot
import me.marvuun.database.Database
import me.marvuun.database.daos.CurrentActivity
import me.marvuun.database.tables.CurrentActivities
import me.marvuun.logic.finishActivity
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


@OptIn(KordPreview::class)
fun main() {
  Database.init()

  val token = System.getenv("BOT_TOKEN") ?: error("Please set the BOT_TOKEN environment variable!")

  bot(token) {
    prefix { "%" }
    onStart {
      createCurrentActivityCheckingThread()
    }
  }
}


fun Discord.createCurrentActivityCheckingThread() {
  val thread = Thread {
    runBlocking {
      while (true) {
        val currentActivities = transaction {
          CurrentActivity.find { CurrentActivities.activityEnd lessEq System.currentTimeMillis() }.toList()
        }

        currentActivities.forEach {
          val guild = this@createCurrentActivityCheckingThread.kord.getGuildOrNull(Snowflake(it.guildId))

          if (guild != null) {
            try {

              val channel = guild.getChannel(Snowflake(it.channelId))

              if (channel is MessageChannel) {
                val user = this@createCurrentActivityCheckingThread.kord.getUser(Snowflake(it.userId))!!

                channel.createMessage {
                  content = user.mention
                }

                finishActivity(user, channel)
              }
            } catch (_: Exception) {
            }
          }
          transaction { it.delete() }
        }
        delay(1000)
      }
    }
  }
  thread.start()
}