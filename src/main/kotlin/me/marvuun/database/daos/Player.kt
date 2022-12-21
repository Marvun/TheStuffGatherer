package me.marvuun.database.daos

import me.marvuun.database.tables.Players
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Player(id: EntityID<ULong>): Entity<ULong>(id) {
  companion object: EntityClass<ULong, Player>(Players)

  var userId by Players.id
  var activityStartTime by Players.activityStartTime
  var activityDuration by Players.activityDuration
  var currentActivityType by Players.currentActivityType
  var currentActivity by Players.currentActivity
  var currentLocation by Players.currentLocation
  var currentlyGathering by Players.currentlyGathering.transformResources()
  var destination by Players.destination


}