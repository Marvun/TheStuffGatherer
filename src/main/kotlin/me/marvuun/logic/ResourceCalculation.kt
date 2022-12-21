package me.marvuun.logic

import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.database.daos.Resource
import me.marvuun.enums.ResourceCategories

fun GuildSlashCommandEvent<NoArgs>.calculateResourceAmount(resource: Resource): Int {

  var level = getLevelForResourceCategory(resource)


  return if (resource.maxAmount < resource.maxAmount * level.toDouble() / resource.maxAtLevel) resource.maxAmount
  else {

    val temp = level / 5
    level %= 5
    if (level == 0) level += 1

    if ((resource.maxAmount * level.toDouble() / (resource.maxAtLevel - temp * 5)).toInt() == 0) 1
    else (resource.maxAmount * level.toDouble() / (resource.maxAtLevel - temp * 5)).toInt()
  }

}

fun GuildSlashCommandEvent<NoArgs>.getLevelForResourceCategory(resource: Resource): Int {
  val levels = getLevels()
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
    ResourceCategories.NUGGET -> TODO()

  }

}
