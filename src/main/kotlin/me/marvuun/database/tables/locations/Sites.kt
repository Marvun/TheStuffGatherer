package me.marvuun.database.tables.locations

import me.marvuun.enums.RarityTypes
import me.marvuun.enums.SiteTypes
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
object Sites: Locations() {

  override val id = uuid("site_id").entityId()
  override val primaryKey = PrimaryKey(id)

  val userId = ulong("user_id")
  val type = enumerationByName<SiteTypes>("site_type", 255)
  val rarity = enumerationByName<RarityTypes>("rarity_type", 255)
  val totalResources = varchar("total_resources", 255)
  val currentResources = varchar("current_resources", 255)
  val travelTime = integer("travel_time")

}