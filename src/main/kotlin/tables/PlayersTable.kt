package com.theStuffGatherer.tables


import com.theStuffGatherer.enums.Activities
import org.jetbrains.exposed.dao.id.LongIdTable
@OptIn(ExperimentalUnsignedTypes::class)
object PlayersTable : LongIdTable() {
  val userId = ulong("userId")
  val activityStartTime = long("activityStartTime").default(System.currentTimeMillis())
  val activityDuration = long("activityDuration").default(0L)
  var currentActivity = enumerationByName<Activities>("currentActivity",255).default(Activities.NOTHING)
}