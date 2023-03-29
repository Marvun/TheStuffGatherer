package me.marvuun.threads

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.marvuun.database.daos.activities.CurrentPlayerActivity
import me.marvuun.database.tables.CurrentPlayerActivities
import me.marvuun.logic.finishActivity
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

fun Discord.createCurrentActivityCheckingThread() {
    val currentActivityCheckingThread = Thread {
        runBlocking {
            while (true) {
                val currentActivities = transaction {
                    CurrentPlayerActivity.find { CurrentPlayerActivities.activityEnd lessEq System.currentTimeMillis() }.toList()
                }

                newSuspendedTransaction {
                    currentActivities.forEach {
                        val guild = kord.getGuildOrNull(Snowflake(it.guildId))

                        if (guild != null) {
                            val channel = guild.getChannel(Snowflake(it.channelId))

                            val user = kord.getUser(Snowflake(it.userId))

                            if (user != null) {
                                finishActivity(user, channel as MessageChannel)
                            }
                        }
                        it.delete()
                    }
                }
                delay(1000)
            }
        }
    }
    currentActivityCheckingThread.start()
}
