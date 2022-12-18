package com.theStuffGatherer.databass.DAOs

import com.theStuffGatherer.databass.tables.PlayersTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Player(id: EntityID<Long>) : LongEntity(id) {


  companion object : LongEntityClass<Player>(PlayersTable)

  var userId by PlayersTable.userId
  var activityStartTime by PlayersTable.activityStartTime
  var activityDuration by PlayersTable.activityDuration
  var currentActivityType by PlayersTable.currentActivityType
  var currentActivity by PlayersTable.currentActivity
  var currentLocation by PlayersTable.currentLocation
  var currentlyGathering by PlayersTable.currentlyGathering
  var destination by PlayersTable.destination
}