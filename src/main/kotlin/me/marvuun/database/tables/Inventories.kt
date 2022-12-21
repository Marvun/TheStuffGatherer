package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IdTable

@OptIn(ExperimentalUnsignedTypes::class)
object Inventories : IdTable<ULong>(){

  override val id = ulong("user_id").entityId()

  val itemId = varchar("item_id", 255)
  val amount = integer("amount")

  override val primaryKey = PrimaryKey(id, itemId)
}