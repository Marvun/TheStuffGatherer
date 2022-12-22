package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

@OptIn(ExperimentalUnsignedTypes::class)
object Inventories : IntIdTable(){

  val userId = ulong("user_id")
  val itemId = varchar("item_id", 255)
  val amount = integer("amount")

}