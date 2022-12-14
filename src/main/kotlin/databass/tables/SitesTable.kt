package com.theStuffGatherer.databass.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object SitesTable: LongIdTable() {
  val userId = reference("userId", PlayersTable)
  val mines = varchar("mines", 255).default("")
  val ponds = varchar("ponds", 255).default("")
  val lakes = varchar("lakes", 255).default("")
  val rivers = varchar("rivers", 255).default("")
  val forests = varchar("forests", 255).default("")
  val meadows = varchar("meadows", 255).default("")

}
