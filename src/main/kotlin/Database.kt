package com.theStuffGatherer

import com.theStuffGatherer.tables.PlayersTable
import com.theStuffGatherer.tables.SkillsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
  private val db by lazy {
    Database.connect("jdbc:mariadb://localhost:3306/thestuffgatherer",
      user = "root", password = "Gather")

    transaction {
      SchemaUtils.create(PlayersTable)
      SchemaUtils.create(SkillsTable)
    }
  }
  fun init() {
    db
  }
}