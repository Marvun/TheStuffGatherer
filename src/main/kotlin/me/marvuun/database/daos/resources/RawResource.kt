package me.marvuun.database.daos.resources

import me.marvuun.database.daos.PlayerSite.Companion.transform
import me.marvuun.database.tables.resources.RawResources
import me.marvuun.enums.SiteTypes
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

class RawResource(id: EntityID<String>): Resource<String>(id) {
  companion object: EntityClass<String, RawResource>(RawResources)

  override var name by RawResources.id
  override var short by RawResources.short
  override var type by RawResources.type
  var gatherDuration by RawResources.gatherDuration
  var siteTypes by RawResources.siteTypes.transformList()
  var maxAtLevel by RawResources.maxAtLevel
  var maxAmount by RawResources.maxAmount

}

private fun Column<String>.transformList() =
  transform({
    it.toString()
  }, {
    it.split(", ").map { site -> SiteTypes.getFromString(site) }
  })


