package me.marvuun.database.daos

import me.marvuun.database.tables.PlayerSites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PlayerSite(id: EntityID<ULong>): Entity<ULong>(id) {
  companion object: EntityClass<ULong, PlayerSite>(PlayerSites)

  var userId by PlayerSites.id
  var mineId by PlayerSites.mineId
  var lakeId by PlayerSites.lakeId
  var riverId by PlayerSites.riverId
  var forestId by PlayerSites.forestId
  var meadowId by PlayerSites.meadowId
}