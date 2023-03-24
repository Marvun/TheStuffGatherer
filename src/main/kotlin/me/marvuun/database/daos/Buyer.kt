package me.marvuun.database.daos

import dev.kord.core.entity.User
import me.marvuun.database.daos.location.transformResources
import me.marvuun.database.daos.resources.Recipe
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.tables.Buyers
import me.marvuun.enums.RarityTypes
import me.marvuun.logic.generateSites
import me.marvuun.logic.getPlayer
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class Buyer(id: EntityID<UUID>) : Entity<UUID>(id) {
  companion object : EntityClass<UUID, Buyer>(Buyers)

  var buyerId by Buyers.id
  var sellableItems by Buyers.sellableItems.transformResources()
  var money by Buyers.money
  var specialRewards by Buyers.specialRewards
  var timeLimit by Buyers.timeLimit
}

fun generateBuyers(user: User) : MutableList<Buyer> {
  val buyerList = mutableListOf<Buyer>()
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

    println("Count: $resourceCount")
    repeat(resourceCount) { _ ->
      println("AMulti: $amountMultiplier")
      println("MMulti: $moneyMultiplier")
      println("special: $hasSpecialRewards")
      val randomResource = SellingInformation.all().filter {
        val resource = getResourceFromShort(it.short.value)
        val level = resource.getLevelForResourceCategory(user)
        it.unlockedAtLevel <= level
      }.random()
      val short = randomResource.short.value
      println("Short: $short")
      println("baseAmount: ${randomResource.baseAmount}")
      println("amount: ${(randomResource.baseAmount * amountMultiplier).toInt()}")

      if (sellableItemMap[short] == null)
        sellableItemMap[short] = (randomResource.baseAmount * amountMultiplier).toInt()
      else
        sellableItemMap[short] = (sellableItemMap[short]!! + randomResource.baseAmount * amountMultiplier).toInt()
      println("basePrice: ${randomResource.basePrice}")
      println("price: ${((randomResource.baseAmount * amountMultiplier).toInt() * randomResource.basePrice * moneyMultiplier).toInt()}")
      money += ((randomResource.baseAmount * amountMultiplier).toInt() * randomResource.basePrice * moneyMultiplier).toInt()
      println("Money: $money")
    }

    if (hasSpecialRewards){
      when ((1..2).random()) {
        1 -> {
          generateSites(getPlayer(user), user, 1, RarityTypes.S)
          specialRewardString = "1x S-Site"
        }
        2 -> {
          val randomSellingInformation = SellingInformation.all().filter { !it.short.value.contains("raw_") }.random()
          val resource = getResourceFromShort(randomSellingInformation.short.value) as Recipe<*>
          val amount = resource.outputAmount[resource.short]!!.random() * 3
          specialRewardString = "${amount}x ${resource.name.value}"
        }
      }
      startOfNextDay = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000
    }

    val buyer = Buyer.new {
      buyerId = EntityID(UUID.randomUUID(), Buyers)
      sellableItems = sellableItemMap
      this.money = money
      specialRewards = specialRewardString
      timeLimit = if (hasSpecialRewards) startOfNextDay else null
    }
    buyerList.add(buyer)
  }
  return buyerList
}