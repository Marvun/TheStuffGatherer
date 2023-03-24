package me.marvuun.database.daos.location

import me.marvuun.database.tables.locations.Homes
import me.marvuun.enums.StationTypes
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Home(id: EntityID<UUID>) : Location(id) {
  companion object : EntityClass<UUID, Home>(Homes)

  override var xCoordinate by Homes.xCoordinate
  override var yCoordinate by Homes.yCoordinate
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