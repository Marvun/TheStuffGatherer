package me.marvuun.database.daos

import me.marvuun.database.tables.Homes
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Home(id: EntityID<UUID>) : Entity<UUID>(id) {
  companion object : EntityClass<UUID, Home>(Homes)

  var homeId by Homes.id
  var userId by Homes.userId
  var travelTime by Homes.travelTime
  var furnaceLevel by Homes.furnaceLevel
  var sawmillLevel by Homes.sawmillLevel
  var stoneCuttingStationLevel by Homes.stoneCuttingStationLevel
}