package com.theStuffGatherer.tables


import org.jetbrains.exposed.dao.id.LongIdTable
@OptIn(ExperimentalUnsignedTypes::class)
object PlayersTable : LongIdTable() {
  val userId = ulong("userId")
  val exploreStartTime = long("exploreStartTime").default(System.currentTimeMillis())
  val exploreDuration = long("exploreDuration").default(0L)
}