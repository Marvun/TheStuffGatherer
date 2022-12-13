package com.theStuffGatherer.DAOs

import com.theStuffGatherer.tables.PlayersTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Player(id: EntityID<Long>) : LongEntity(id) {


  companion object : LongEntityClass<Player>(PlayersTable)

  var userId by PlayersTable.userId
  var exploreStartTime by PlayersTable.exploreStartTime
  var exploreDuration by PlayersTable.exploreDuration

  fun checkIfExploring(): Boolean {
    return System.currentTimeMillis() - exploreStartTime < exploreDuration
  }
}