package com.theStuffGatherer.databass.tables.sites

import com.theStuffGatherer.databass.tables.PlayersTable
import org.jetbrains.exposed.dao.id.LongIdTable

object MinesTable : LongIdTable() {
  val userId = reference("userId", PlayersTable)
  val currentSMine = varchar("currentSMine", 255).default("")
  val currentAMine = varchar("currentAMine", 255).default("")
  val currentBMine = varchar("currentBMine", 255).default("")
  val currentCMine = varchar("currentCMine", 255).default("")
  val currentDMine = varchar("currentDMine", 255).default("")
}