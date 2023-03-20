package me.marvuun.threads

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.marvuun.database.daos.activities.CurrentStationUpgrade
import me.marvuun.database.tables.CurrentStationUpgrades
import me.marvuun.logic.finishStationUpgrade
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

fun Discord.createCurrentStationUpgradesCheckingThread() {
  val currentStationUpgradesCheckingThread = Thread {
    runBlocking {
      while (true) {
        val finishedUpgrades = transaction {
          CurrentStationUpgrade.find { CurrentStationUpgrades.activityEnd lessEq System.currentTimeMillis() }
        }

        newSuspendedTransaction {
          finishedUpgrades.forEach {
            val guild = kord.getGuildOrNull(Snowflake(it.guildId))

            if (guild != null) {
              val channel = guild.getChannel(Snowflake(it.channelId))

              val user = kord.getUser(Snowflake(it.userId))

              if (user != null)
                finishStationUpgrade(user, channel as MessageChannel)

            }
            it.delete()
          }
        }

        delay(1000)
      }
    }
  }
  currentStationUpgradesCheckingThread.start()
}