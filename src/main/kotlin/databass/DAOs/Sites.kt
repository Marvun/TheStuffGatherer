package com.theStuffGatherer.databass.DAOs

import com.theStuffGatherer.databass.tables.SitesTable
import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.enums.SiteTypes
import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

class Sites(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<Sites>(SitesTable)

  var userId by Player referencedOn SitesTable.userId
  var mines by SitesTable.mines.transformSite()
  var ponds by SitesTable.ponds.transformSite()
  var lakes by SitesTable.lakes.transformSite()
  var rivers by SitesTable.rivers.transformSite()
  var forests by SitesTable.forests.transformSite()
  var meadows by SitesTable.meadows.transformSite()

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
          map[rarityType] = entry.first().code
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

private fun _addSiteToDatabase(
  site: MutableMap<RarityTypes, Int>,
  rarities: Map<RarityTypes, Int>
): MutableMap<RarityTypes, Int> = (site.toList() + rarities.toList()).groupBy({ it.first }, { it.second })
  .map { (key, values) -> key to values.sum() }
  .toMap()
  .toMutableMap()
