package me.marvuun.database.daos

import me.marvuun.database.daos.activities.CurrentStationUpgrade.Companion.transform
import me.marvuun.database.tables.Players
import me.marvuun.util.stringToMap
import me.marvuun.util.toIntRange
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

class Player(id: EntityID<ULong>): Entity<ULong>(id) {
  companion object: EntityClass<ULong, Player>(Players)

  var userId by Players.id
  var activityStartTime by Players.activityStartTime
  var activityDuration by Players.activityDuration
  var currentActivityType by Players.currentActivityType
  var currentActivity by Players.currentActivity
  var currentLocation by Players.currentLocation
  var currentlyMaking by Players.currentlyMaking.transformCurrentlyMakingMap()
  var destination by Players.destination

}

private fun Column<String>.transformCurrentlyMakingMap() =
  transform({
    it.toString()
  },
    {
      stringToMap(it)
        .mapValues { entry ->  entry.value.toIntRange()  }
    })