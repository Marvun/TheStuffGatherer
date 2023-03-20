package me.marvuun.database.daos

import me.marvuun.database.tables.Homes
import me.marvuun.enums.StationTypes
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
  var stoneCutterLevel by Homes.stoneCutterLevel
  var upgradeStartTime by Homes.upgradeStartTime
  var upgradeDuration by Homes.upgradeDuration
  var upgradingStation by Homes.upgradingStation

  var selectedStation: StationTypes? = null
}