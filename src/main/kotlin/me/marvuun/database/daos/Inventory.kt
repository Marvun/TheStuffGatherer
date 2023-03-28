package me.marvuun.database.daos

import me.marvuun.database.tables.Inventories
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Inventory(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Inventory>(Inventories)

  var userId by Inventories.userId
  var itemId by Inventories.itemId
  var amount by Inventories.amount
  var type by Inventories.type

}