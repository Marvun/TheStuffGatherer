package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IdTable
import java.util.UUID

@OptIn(ExperimentalUnsignedTypes::class)
object Homes: IdTable<UUID>() {

  override val id = uuid("home_id").entityId()
  override val primaryKey = PrimaryKey(id)

  val userId = ulong("user_id")
  val travelTime = integer("travel_time").default(0)

}