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
  var currentActivity by PlayersTable.currentActivity

}