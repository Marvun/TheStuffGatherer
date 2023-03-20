package me.marvuun.database.tables.resources

import me.marvuun.enums.ResourceCategories
import org.jetbrains.exposed.dao.id.IdTable

open class Resources : IdTable<String>() {
  final override val id = varchar("name", 255).entityId()
  final override val primaryKey = PrimaryKey(id)

  val short = varchar("short", 255)
  val type = enumerationByName<ResourceCategories>("type", 255)
}





