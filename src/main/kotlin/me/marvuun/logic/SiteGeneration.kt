package me.marvuun.logic

import dev.kord.core.entity.User
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.database.tables.resources.RawResources
import me.marvuun.enums.RarityTypes
import me.marvuun.enums.ResourceCategories
import me.marvuun.enums.SiteTypes

fun generateResources(rarityType: RarityTypes, siteType: SiteTypes, user: User): Map<String, Int> {

  val map = mutableMapOf<String, Int>()
  val resourceCategories = siteTypeToResourceCategories(siteType)

  val (typeCount, amount) =
    when (rarityType) {
      RarityTypes.S -> arrayOf((3..5).random(), 5..10)
      RarityTypes.A -> arrayOf(3, 4..6)
      RarityTypes.B -> arrayOf((2..3).random(), 3..3)
      RarityTypes.C -> arrayOf(2, 2..3)
      RarityTypes.D -> arrayOf((1..2).random(), 1..2)
    }

  if (typeCount is Int && amount is IntRange) {

    repeat(typeCount) {
      val type = RawResource.find { RawResources.type inList resourceCategories }.toList().filter { it.maxAtLevel - it.getLevelForResourceCategory(user) <= 10 }.random().short
      if (map[type] == null) map[type] = amount.random()
      else map[type] = map[type]!! + amount.random()
    }

  }
  return map
}

fun generateTravelTime(rarity: RarityTypes) =
  when (rarity) {
    RarityTypes.S -> (5400000..7200000).random()
    RarityTypes.A -> (3600000..5400000).random()
    RarityTypes.B -> (2700000..3600000).random()
    RarityTypes.C -> (1800000..2700000).random()
    RarityTypes.D -> (900000..1800000).random()
  }


fun siteTypeToResourceCategories(siteType: SiteTypes): List<ResourceCategories> {
  return when (siteType) {
    SiteTypes.LAKE -> listOf(ResourceCategories.FISH, ResourceCategories.SAND)
    SiteTypes.RIVER -> listOf(ResourceCategories.FISH, ResourceCategories.SAND)
    SiteTypes.MINE -> listOf(ResourceCategories.ORE, ResourceCategories.STONE, ResourceCategories.GEM)
    SiteTypes.FOREST -> listOf(ResourceCategories.LOG, ResourceCategories.PLANT, ResourceCategories.ANIMAL, ResourceCategories.FRUIT, ResourceCategories.BERRY)
    SiteTypes.MEADOW -> listOf(ResourceCategories.BERRY, ResourceCategories.PLANT, ResourceCategories.ANIMAL, ResourceCategories.FRUIT)
  }
}