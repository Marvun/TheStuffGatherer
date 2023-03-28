package me.marvuun.logic

import dev.kord.core.entity.User
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.database.tables.resources.RawResources
import me.marvuun.enums.RarityTypes
import me.marvuun.enums.ResourceCategories
import me.marvuun.enums.SiteTypes
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.absoluteValue

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
      val resources = RawResource.find { RawResources.type inList resourceCategories }.toMutableList()

      if (siteType == SiteTypes.FOREST)
        resources.add(RawResource.findById("Firewood")!!)

      if (siteType == SiteTypes.MINE)
        resources.removeIf { it.name.value == "Firewood" }

      val type = resources.filter { it.maxAtLevel - it.getLevelForResourceCategory(user) <= 10 }.random().short

      if (map[type] == null) map[type] = amount.random()
      else map[type] = map[type]!! + amount.random()
    }

  }
  return map
}

fun generateCoordinates(rarity: RarityTypes): MutableList<Int> {

  val allCoordinates = mutableListOf<MutableList<Int>>()

  for (i in -500..500) {
    for (j in -500..500) {
      allCoordinates.add(mutableListOf(i,j))
    }
  }

  val randomCoordinates = transaction {
    when (rarity) {
      RarityTypes.S -> allCoordinates.filter { it[0].absoluteValue >= 450 && it [1].absoluteValue >= 450 }.random()
      RarityTypes.A -> allCoordinates.filter { it[0].absoluteValue in (350..450) && it [1].absoluteValue in (350..450) }.random()
      RarityTypes.B -> allCoordinates.filter { it[0].absoluteValue in (250..350) && it [1].absoluteValue in (250..350) }.random()
      RarityTypes.C -> allCoordinates.filter { it[0].absoluteValue in (150..250) && it [1].absoluteValue in (150..250) }.random()
      RarityTypes.D -> allCoordinates.filter { it[0].absoluteValue in (50..150) && it [1].absoluteValue in (50..150) }.random()
    }
  }

  return randomCoordinates
}


fun siteTypeToResourceCategories(siteType: SiteTypes): List<ResourceCategories> {
  return when (siteType) {
    SiteTypes.LAKE -> listOf(ResourceCategories.FISH, ResourceCategories.SAND)
    SiteTypes.RIVER -> listOf(ResourceCategories.FISH, ResourceCategories.SAND)
    SiteTypes.MINE -> listOf(ResourceCategories.ORE, ResourceCategories.STONE, ResourceCategories.GEM, ResourceCategories.FUEL)
    SiteTypes.FOREST -> listOf(ResourceCategories.LOG, ResourceCategories.PLANT, ResourceCategories.ANIMAL, ResourceCategories.FRUIT, ResourceCategories.BERRY)
    SiteTypes.MEADOW -> listOf(ResourceCategories.BERRY, ResourceCategories.PLANT, ResourceCategories.ANIMAL, ResourceCategories.FRUIT)
  }
}