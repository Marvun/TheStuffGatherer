package me.marvuun.database.tables

import me.marvuun.enums.ResourceCategories
import org.jetbrains.exposed.dao.id.IdTable

object Resources: IdTable<String>() {

  override val id = varchar("name", 255).entityId()
  override val primaryKey = PrimaryKey(id)

  val short = varchar("short", 255)
  val gatherDuration = long("gather_duration")
  val type = enumerationByName<ResourceCategories>("type",255)
  val siteTypes = varchar("site_type",255)
}