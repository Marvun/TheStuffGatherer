package com.theStuffGatherer.databass.tables


import com.theStuffGatherer.enums.ActivityTypes
import org.jetbrains.exposed.dao.id.LongIdTable

@OptIn(ExperimentalUnsignedTypes::class)
object PlayersTable : LongIdTable() {

  val userId = ulong("userId")
  val activityStartTime = long("activityStartTime").default(System.currentTimeMillis())
  val activityDuration = long("activityDuration").default(0L)
  var currentActivityType = enumerationByName<ActivityTypes>("currentActivityType",255).default(ActivityTypes.NOTHING)
  val currentActivity = varchar("currentActivity", 255).default("")
  val currentLocation = varchar("currentLocation", 255).default("home")
  val currentlyGathering = varchar("currentlyGathering", 255).default("")
  val destination = varchar("destination", 255).default("")
}