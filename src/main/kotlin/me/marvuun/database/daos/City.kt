package me.marvuun.database.daos

import me.marvuun.database.daos.Buyer.Companion.transform
import me.marvuun.database.tables.Cities
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import java.util.*

class City(id: EntityID<UUID>) : Entity<UUID>(id) {
  companion object : EntityClass<UUID, City>(Cities)

  var cityId by Cities.id
  var userId by Cities.userId
  var travelTime by Cities.travelTime
  var buyers by Cities.buyers.transformBuyers()
  var purchasableItems by Cities.purchasableItems.transformResources()

}

private fun Column<String>.transformBuyers() = transform({
  it.map { entry -> entry.buyerId.value }.joinToString(",")
}, {
  val buyers = mutableListOf<Buyer>()
  it.split(",").forEach {entry ->
    buyers.add(Buyer.findById(UUID.fromString(entry))!!)
  }
  buyers
})