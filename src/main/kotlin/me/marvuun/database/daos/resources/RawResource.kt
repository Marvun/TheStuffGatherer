package me.marvuun.database.daos.resources

import dev.kord.core.entity.User
import me.marvuun.database.daos.Level
import me.marvuun.database.daos.PlayerSite.Companion.transform
import me.marvuun.database.tables.resources.RawResources
import me.marvuun.enums.ResourceCategories
import me.marvuun.enums.SiteTypes
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

class RawResource(id: EntityID<String>): Resource<String>(id) {
  companion object: EntityClass<String, RawResource>(RawResources)

  override var name by RawResources.id
  override var short by RawResources.short
  override var type by RawResources.type
  var gatherDuration by RawResources.gatherDuration
  var siteTypes by RawResources.siteTypes.transformList()
  var maxAtLevel by RawResources.maxAtLevel
  var maxAmount by RawResources.maxAmount

  fun calculateResourceAmount(user: User): Int {

    var level = getLevelForResourceCategory(user)


    return if (maxAmount <= maxAmount * level.toDouble() / maxAtLevel) maxAmount
    else {

      val temp = level / 5
      level %= 5
      if (level == 0) level += 1

      if ((maxAmount * level.toDouble() / (maxAtLevel - temp * 5)).toInt() == 0) 1
      else (maxAmount * level.toDouble() / (maxAtLevel - temp * 5)).toInt()
    }

  }
  fun getLevelForResourceCategory(user: User): Int {
    val levels = transaction { Level.findById(user.id.value)!! }
    return when (type) {
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
}

private fun Column<String>.transformList() =
  transform({
    it.toString()
  }, {
    it.split(", ").map { site -> SiteTypes.getFromString(site) }
  })


