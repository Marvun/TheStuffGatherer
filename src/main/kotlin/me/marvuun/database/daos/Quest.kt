package me.marvuun.database.daos

import dev.kord.core.entity.User
import me.marvuun.database.daos.location.City
import me.marvuun.database.daos.location.transformResources
import me.marvuun.database.daos.resources.Blueprint
import me.marvuun.database.daos.resources.Recipe
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.tables.Quests
import me.marvuun.database.tables.resources.Blueprints
import me.marvuun.enums.RarityTypes
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class Quest(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, Quest>(Quests)

    var questId by Quests.id
    var wantedItems by Quests.wantedItems.transformResources()
    var money by Quests.money
    var specialRewards by Quests.specialRewards
    var timeLimit by Quests.timeLimit
    var city by City referencedOn Quests.city
}

fun generateQuests(user: User, city: City): MutableList<Quest> {
    val questList = mutableListOf<Quest>()
    repeat(5) { _ ->

        val sellableItemMap = mutableMapOf<String, Int>()
        var specialRewardString = ""
        val randomRarityNum = (1..100).random()
        val randomizerNum = (1..5).random()
        var hasSpecialRewards = false
        var moneyMultiplier = 1.0
        var amountMultiplier = 1.0
        var resourceCount = 1
        var money = 0
        var startOfNextDay = 0L

        when (RarityTypes.values().find { randomRarityNum in it.range }!!) {
            RarityTypes.S -> {
                when (randomizerNum) {
                    1 -> {
                        resourceCount = 4
                        moneyMultiplier = 1.5
                    }
                    2 -> {
                        resourceCount = 3
                        moneyMultiplier = 0.8
                        hasSpecialRewards = true
                    }
                    3 -> {
                        resourceCount = 2
                        moneyMultiplier = 1.5
                        amountMultiplier = 1.5
                    }
                    4 -> {
                        resourceCount = 1
                        amountMultiplier = 3.0
                    }
                    5 -> {
                        resourceCount = 4
                        amountMultiplier = 0.8
                        hasSpecialRewards = true
                    }
                }
            }
            RarityTypes.A -> {
                when (randomizerNum) {
                    1 -> {
                        resourceCount = 3
                        moneyMultiplier = 1.2
                    }
                    2 -> {
                        resourceCount = 4
                        amountMultiplier = 1.5
                        moneyMultiplier = 1.2
                    }
                    3 -> {
                        resourceCount = 2
                        amountMultiplier = 2.0
                    }
                    4 -> {
                        resourceCount = 1
                        amountMultiplier = 3.0
                        moneyMultiplier = 0.9
                    }
                    5 -> {
                        resourceCount = 4
                        amountMultiplier = 2.0
                        hasSpecialRewards = true
                    }
                }
            }
            RarityTypes.B -> {
                when (randomizerNum) {
                    1 -> {
                        resourceCount = 2
                        moneyMultiplier = 1.1
                    }
                    2 -> {
                        resourceCount = 3
                    }
                    3 -> {
                        resourceCount = 1
                        moneyMultiplier = 1.2
                        amountMultiplier = 1.5
                    }
                    4 -> {
                        resourceCount = 3
                        amountMultiplier = 0.8
                    }
                    5 -> {
                        resourceCount = 2
                        amountMultiplier = 1.5
                    }
                }
            }
            RarityTypes.C -> {
                when (randomizerNum) {
                    1 -> {
                        resourceCount = 2
                    }
                    2 -> {
                        resourceCount = 1
                        moneyMultiplier = 1.2
                    }
                    3 -> {
                        resourceCount = 2
                        moneyMultiplier = 0.8
                        amountMultiplier = 1.5
                    }
                    4 -> {
                        resourceCount = 1
                        amountMultiplier = 1.1
                    }
                    5 -> {
                        resourceCount = 2
                        moneyMultiplier = 2.0
                        amountMultiplier = 0.5
                    }
                }
            }
            RarityTypes.D -> {
                when (randomizerNum) {
                    1 -> {
                        resourceCount = 1
                    }
                    2 -> {
                        resourceCount = 1
                        moneyMultiplier = 1.2
                        amountMultiplier = 0.8
                    }
                    3 -> {
                        resourceCount = 3
                        moneyMultiplier = 0.6
                        amountMultiplier = 1.2
                    }
                    4 -> {
                        resourceCount = 1
                        amountMultiplier = 5.0
                        moneyMultiplier = 0.5
                    }
                    5 -> {
                        resourceCount = 2
                        moneyMultiplier = 0.8
                    }
                }
            }
        }

        repeat(resourceCount) { _ ->
            val randomResource = SellingInformation.all().filter {
                val resource = getResourceFromShort(it.short.value)
                val level = resource.getLevelForResourceCategory(user)
                it.unlockedAtLevel <= level
            }.random()
            val short = randomResource.short.value

            if (sellableItemMap[short] == null) {
                sellableItemMap[short] = (randomResource.baseAmount * amountMultiplier).toInt()
            } else {
                sellableItemMap[short] = (sellableItemMap[short]!! + randomResource.baseAmount * amountMultiplier).toInt()
            }
            money += ((randomResource.baseAmount * amountMultiplier).toInt() * randomResource.basePrice * moneyMultiplier).toInt()
        }

        if (hasSpecialRewards) {
            when ((1..3).random()) {
                1 -> {
                    specialRewardString = "1x S-Site"
                }
                2 -> {
                    val randomSellingInformation = SellingInformation.all().filter { !it.short.value.contains("raw_") }.random()
                    val resource = getResourceFromShort(randomSellingInformation.short.value) as Recipe<*>
                    val amount = resource.outputAmount[resource.short]!!.random() * 3
                    specialRewardString = "${amount}x ${resource.name.value}"
                }
                3 -> {
                    val sBlueprints = Blueprint.find { Blueprints.rarity eq RarityTypes.S }
                    specialRewardString = "1x ${sBlueprints.toList().random().name}"
                }
            }
            startOfNextDay = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000
        }

        val quest = Quest.new {
            questId = EntityID(UUID.randomUUID(), Quests)
            wantedItems = sellableItemMap
            this.money = money
            specialRewards = specialRewardString
            timeLimit = if (hasSpecialRewards) startOfNextDay else null
            this.city = city
        }
        questList.add(quest)
    }
    return questList
}
