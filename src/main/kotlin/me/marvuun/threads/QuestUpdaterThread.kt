package me.marvuun.threads

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.marvuun.database.daos.generateQuests
import me.marvuun.database.daos.location.City
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Discord.questUpdaterThread() {
    val questUpdaterThread = Thread {
        runBlocking {
            while (true) {
                val time = System.currentTimeMillis()
                if (time % 86400000 == 0L) {
                    newSuspendedTransaction {
                        val cities = City.all()
                        cities.forEach {
                            val oldQuests = it.quests
                            oldQuests.forEach { quest ->
                                quest.delete()
                            }
                            it.quests = generateQuests(kord.getUser(Snowflake(it.userId))!!, it)
                        }
                    }

                    delay(1000)
                }
            }
        }
    }
    questUpdaterThread.start()
}
