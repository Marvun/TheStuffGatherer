package com.theStuffGatherer.databass.tables

import org.jetbrains.exposed.sql.Table

@OptIn(ExperimentalUnsignedTypes::class)
object InventoriesTable : Table() {


  val userId = long("userId")
  val itemId = varchar("itemId", 255)
  val amount = uinteger("amount")

  override val primaryKey = PrimaryKey(userId, itemId)
}