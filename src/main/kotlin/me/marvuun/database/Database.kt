package me.marvuun.database


import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import me.marvuun.database.daos.Resource
import me.marvuun.database.tables.*
import me.marvuun.enums.ResourceCategories
import me.marvuun.enums.SiteTypes
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
  private val db by lazy {
    Database.connect(
      url = "jdbc:mariadb://localhost:3306/thestuffgatherer",
      user = System.getenv("DB_USER"),
      password = System.getenv("DB_PASSWORD")
    )

    transaction {
      SchemaUtils.create(Players, Sites, PlayerSites, Resources, Levels, Inventories, Homes, CurrentActivities)
      createResourceTypes()
    }

  }

  fun init() {
    db
  }
}

fun createResourceTypes() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("resources.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "name" && Resource.findById(it[0]) == null)

        Resource.new {
          name = EntityID(it[0], Resources)
          short = it[1]
          gatherDuration = it[2].toLong()
          type = ResourceCategories.getFromString(it[3])
          siteTypes = it[4].split(",").map { entry -> SiteTypes.getFromString(entry) }
          maxAtLevel = it[5].toInt()
          maxAmount = it[6].toInt()
        }
    }
  }

}