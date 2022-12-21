package me.marvuun.database.daos

import me.marvuun.database.tables.Inventories
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Inventory(id: EntityID<ULong>) : Entity<ULong>(id) {
  companion object : EntityClass<ULong, Inventory>(Inventories)

  var userId by Inventories.id
  var itemId by Inventories.itemId
  var amount by Inventories.amount

}