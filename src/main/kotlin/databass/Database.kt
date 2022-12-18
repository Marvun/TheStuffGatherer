package com.theStuffGatherer.databass

import com.theStuffGatherer.databass.tables.InventoriesTable
import com.theStuffGatherer.databass.tables.PlayersTable
import com.theStuffGatherer.databass.tables.SitesTable
import com.theStuffGatherer.databass.tables.SkillsTable
import com.theStuffGatherer.databass.tables.sites.MinesTable
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
      SchemaUtils.create(SitesTable)
      SchemaUtils.create(MinesTable)
      SchemaUtils.create(InventoriesTable)
    }
  }
  fun init() {
    db
  }
}