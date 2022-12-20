package me.marvuun.database.daos

import me.marvuun.database.daos.PlayerSite.Companion.transform
import me.marvuun.database.tables.Resources
import me.marvuun.enums.SiteTypes
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

class Resource(id: EntityID<String>): Entity<String>(id) {
  companion object: EntityClass<String, Resource>(Resources)

  var name by Resources.id
  var short by Resources.short
  var gatherDuration by Resources.gatherDuration
  var type by Resources.type
  var siteTypes by Resources.siteTypes.transformList()
}

private fun Column<String>.transformList() =
  transform({
    it.toString()
  }, {
    it.split(", ").map { site -> SiteTypes.getFromString(site) }
  })
