package me.marvuun.threads

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.marvuun.database.daos.CurrentActivity
import me.marvuun.database.tables.CurrentActivities
import me.marvuun.logic.finishActivity
import org.jetbrains.exposed.sql.transactions.transaction

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