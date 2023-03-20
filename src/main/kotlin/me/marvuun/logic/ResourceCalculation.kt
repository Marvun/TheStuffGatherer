package me.marvuun.logic

import dev.kord.core.entity.User
import me.marvuun.database.daos.Level
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.enums.ResourceCategories
import org.jetbrains.exposed.sql.transactions.transaction

fun calculateResourceAmount(resource: RawResource, user: User): Int {

  var level = getLevelForResourceCategory(resource, user)


  return if (resource.maxAmount <= resource.maxAmount * level.toDouble() / resource.maxAtLevel) resource.maxAmount
  else {

    val temp = level / 5
    level %= 5
    if (level == 0) level += 1

    if ((resource.maxAmount * level.toDouble() / (resource.maxAtLevel - temp * 5)).toInt() == 0) 1
    else (resource.maxAmount * level.toDouble() / (resource.maxAtLevel - temp * 5)).toInt()
  }

}

fun getLevelForResourceCategory(resource: RawResource, user: User): Int {
  val levels = transaction { Level.findById(user.id.value)!! }
  return when (resource.type) {
    ResourceCategories.ORE -> levels.miningLevel
    ResourceCategories.LOG -> levels.woodcuttingLevel
    ResourceCategories.STONE, ResourceCategories.GEM, ResourceCategories.SAND, ResourceCategories.FUEL -> levels.extractionLevel
    ResourceCategories.BERRY, ResourceCategories.FRUIT -> levels.harvestingLevel
    ResourceCategories.ANIMAL -> TODO()
    ResourceCategories.MEAT -> TODO()
    ResourceCategories.SKIN -> TODO()
    ResourceCategories.FISH -> levels.fishingLevel
    ResourceCategories.PLANT, ResourceCategories.HERB -> levels.botanyLevel
    ResourceCategories.NUGGET, ResourceCategories.INGOT -> levels.meltingLevel
    ResourceCategories.PLANK -> levels.sawingLevel
    ResourceCategories.STONE_BLOCK -> levels.stoneCuttingLevel

  }

}
