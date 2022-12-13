package com.theStuffGatherer.tables

import org.jetbrains.exposed.dao.id.LongIdTable


object SkillsTable: LongIdTable() {
  val userId = reference("userId", PlayersTable)
  val fishingSkill = varchar("Fishing",255).default("Fishing,0,0.0,0,100")
  val choppingSkill = varchar("Chopping",255).default("Chopping,0,0.0,0,100")
  val miningSkill = varchar("Mining",255).default("Mining,0,0.0,0,100")
}