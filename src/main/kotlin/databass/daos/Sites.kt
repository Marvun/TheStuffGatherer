package com.theStuffGatherer.databass.DAOs

import com.theStuffGatherer.databass.DAOs.sites.Mines
import com.theStuffGatherer.databass.tables.SitesTable
import com.theStuffGatherer.databass.tables.sites.MinesTable
import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.enums.SiteTypes
import com.theStuffGatherer.enums.resourceTypes.OreTypes
import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

class Sites(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Sites>(SitesTable)

  var userId by Player referencedOn SitesTable.userId
  var mines by SitesTable.mines.transformSite()
  var ponds by SitesTable.ponds.transformSite()
  var lakes by SitesTable.lakes.transformSite()
  var rivers by SitesTable.rivers.transformSite()
  var forests by SitesTable.forests.transformSite()
  var meadows by SitesTable.meadows.transformSite()

  val sites: MutableMap<String, MutableMap<RarityTypes, Int>>
    get() = mutableMapOf(
      "mines" to mines,
      "ponds" to ponds,
      "lakes" to lakes,
      "rivers" to rivers,
      "forests" to forests,
      "meadows" to meadows
    )

  private fun Column<String>.transformSite(): ColumnWithTransform<String, MutableMap<RarityTypes, Int>> {
    val transformed = this.transform(
      { tReal ->
        val list = mutableListOf<String>()
        tReal.forEach { (rarityType, count) ->
          list.add("${count}x${rarityType.name}")
        }
        list.joinToString(",")
      }, { tColumn ->
        val map = mutableMapOf<RarityTypes, Int>()
        if (tColumn == "") return@transform map
        val entries = tColumn.split(",")
        entries.forEach { entry ->
          val rarityType = RarityTypes.values().find { it.name == entry.last().toString() }!!
          map[rarityType] = entry.split("x").first().toInt()
        }
        map
      })
    return transformed
  }
}


fun Sites.addSiteToDatabase(entry: MutableMap.MutableEntry<SiteTypes, MutableList<RarityTypes>>) {
  val rarities = entry.value.groupingBy { it }.eachCount()

  when (entry.key) {
    SiteTypes.LAKE -> this@addSiteToDatabase.lakes = _addSiteToDatabase(this@addSiteToDatabase.lakes, rarities)
    SiteTypes.POND -> this@addSiteToDatabase.ponds = _addSiteToDatabase(this@addSiteToDatabase.ponds, rarities)
    SiteTypes.RIVER -> this@addSiteToDatabase.rivers = _addSiteToDatabase(this@addSiteToDatabase.rivers, rarities)
    SiteTypes.MINE -> this@addSiteToDatabase.mines = _addSiteToDatabase(this@addSiteToDatabase.mines, rarities)
    SiteTypes.FOREST -> this@addSiteToDatabase.forests = _addSiteToDatabase(this@addSiteToDatabase.forests, rarities)
    SiteTypes.MEADOW -> this@addSiteToDatabase.meadows = _addSiteToDatabase(this@addSiteToDatabase.meadows, rarities)
  }

}

fun MutableMap<RarityTypes, Int>.removeSite(rarity: String) {
  val rarityType = RarityTypes.getFromString(rarity)
  this[rarityType] = this[rarityType]!! - 1
  if (this[rarityType] == 0) this.remove(rarityType)
}

private fun _addSiteToDatabase(
  site: MutableMap<RarityTypes, Int>,
  rarities: Map<RarityTypes, Int>
): MutableMap<RarityTypes, Int> = (site.toList() + rarities.toList()).groupBy({ it.first }, { it.second })
  .map { (key, values) -> key to values.sum() }
  .toMap()
  .toMutableMap()

fun getResourcesFromSite(
  siteType: SiteTypes,
  rarityType: RarityTypes,
  player: Player,
  total: Boolean = false
): MutableMap<OreTypes, Int> {

  var resources = mutableMapOf<OreTypes, Int>()

  transaction {
    when (siteType) {
      SiteTypes.MINE -> {

        val mine = Mines.find { MinesTable.userId eq player.id }.first()
        val site = mine.getCurrentMineByRarity(rarityType)
        resources = if (total) site.totalResources else site.currentResources

      }

      SiteTypes.LAKE -> TODO()
      SiteTypes.POND -> TODO()
      SiteTypes.RIVER -> TODO()
      SiteTypes.FOREST -> TODO()
      SiteTypes.MEADOW -> TODO()
    }
  }

  return resources
}