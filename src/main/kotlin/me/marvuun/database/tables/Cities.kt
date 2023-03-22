package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IdTable
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
object Cities: IdTable<UUID>() {
  override val id = Sites.uuid("city_id").entityId()
  override val primaryKey = PrimaryKey(id)

  val userId = ulong("user_id")
  val travelTime = integer("travel_time")
  val buyers = varchar("buyers", 1023)
  val purchasableItems = varchar("purchasable_items", 1023)
}