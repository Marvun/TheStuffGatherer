package me.marvuun.database


import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import me.marvuun.database.daos.SellingInformation
import me.marvuun.database.daos.resources.*
import me.marvuun.database.daos.stations.Furnace
import me.marvuun.database.daos.stations.Sawmill
import me.marvuun.database.daos.stations.StoneCutter
import me.marvuun.database.tables.*
import me.marvuun.database.tables.Furnaces
import me.marvuun.database.tables.Sawmills
import me.marvuun.database.tables.locations.Cities
import me.marvuun.database.tables.locations.Homes
import me.marvuun.database.tables.locations.Sites
import me.marvuun.database.tables.resources.*
import me.marvuun.enums.ResourceCategories
import me.marvuun.enums.SiteTypes
import me.marvuun.util.stringToMap
import me.marvuun.util.toIntRange
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
      SchemaUtils.create(
        Players,
        Sites,
        PlayerSites,
        RawResources,
        FuelResources,
        FurnaceRecipes,
        SawmillRecipes,
        StoneCutterRecipes,
        Levels,
        Inventories,
        Homes,
        CurrentPlayerActivities,
        CurrentStationUpgrades,
        Furnaces,
        Sawmills,
        StoneCutters,
        Cities,
        Buyers,
        SellingInformations
      )
      createSellingInformation()
      createRawResources()
      createFuelResources()
      createFurnaceRecipes()
      createFurnaceLevels()
      createSawmillRecipes()
      createSawmillLevels()
      createStoneCutterRecipes()
      createStoneCutterLevels()
    }

  }

  fun init() {
    db
  }
}

fun createSellingInformation() {
  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("sellingInformation.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "short" && SellingInformation.findById(it[0]) == null)

        SellingInformation.new {
          short = EntityID(it[0], SellingInformations)
          baseAmount = it[1].toInt()
          basePrice = it[2].toInt()
          unlockedAtLevel = it[3].toInt()
        }
    }
  }
}

fun createRawResources() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("rawResources.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "name" && RawResource.findById(it[0]) == null)

        RawResource.new {
          name = EntityID(it[0], RawResources)
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

fun createFuelResources() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("fuelResources.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "name" && FuelResource.findById(it[0]) == null)

        FuelResource.new {
          name = EntityID(it[0], RawResources)
          short = it[1]
          type = ResourceCategories.getFromString(it[2])
          heatLevel = it[3].toInt()
        }
    }
  }
}

fun createFurnaceRecipes() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("furnaceRecipes.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "name" && FurnaceRecipe.findById(it[0]) == null) {

        FurnaceRecipe.new {
          name = EntityID(it[0], FurnaceRecipes)
          short = it[1]
          craftingDuration = it[2].toLong()
          type = ResourceCategories.getFromString(it[3])
          requiredLevel = it[4].toInt()
          requiredHeatLevel = it[5].toInt()
          neededResources = stringToMap(it[6])
            .mapValues { entry -> entry.value.toInt() }
          outputAmount = it[7].toIntRange()
        }
      }
    }
  }
}

fun createFurnaceLevels() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("furnaceLevels.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "constructionTime" && transaction { Furnace.find { Furnaces.requiredLevel eq it[2].toInt() }.empty() } )

        Furnace.new {
          constructionTime = it[0].toLong()
          neededResources = stringToMap(it[1])
            .mapValues { entry -> entry.value.toInt() }
          requiredLevel = it[2].toInt()
        }

    }
  }
}

fun createSawmillRecipes() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("sawmillRecipes.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "name" && SawmillRecipe.findById(it[0]) == null) {

        SawmillRecipe.new {
          name = EntityID(it[0], SawmillRecipes)
          short = it[1]
          craftingDuration = it[2].toLong()
          type = ResourceCategories.getFromString(it[3])
          requiredLevel = it[4].toInt()
          neededResources = stringToMap(it[5])
            .mapValues { entry -> entry.value.toInt() }
          outputAmount = it[6].toIntRange()
        }
      }
    }
  }
}

fun createSawmillLevels() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("sawmillLevels.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "constructionTime" && transaction { Sawmill.find { Sawmills.requiredLevel eq it[2].toInt() }.empty() } )

        Sawmill.new {
          constructionTime = it[0].toLong()
          neededResources = stringToMap(it[1])
            .mapValues { entry -> entry.value.toInt() }
          requiredLevel = it[2].toInt()
        }

    }
  }
}

fun createStoneCutterRecipes() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("stoneCutterRecipes.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "name" && StoneCutterRecipe.findById(it[0]) == null) {

        StoneCutterRecipe.new {
          name = EntityID(it[0], StoneCutterRecipes)
          short = it[1]
          craftingDuration = it[2].toLong()
          type = ResourceCategories.getFromString(it[3])
          requiredLevel = it[4].toInt()
          neededResources = stringToMap(it[5])
            .mapValues { entry -> entry.value.toInt() }
          outputAmount = it[6].toIntRange()
        }
      }
    }
  }
}

fun createStoneCutterLevels() {

  val csvFile = Database.Companion::class.java.classLoader.getResourceAsStream("stoneCutterLevels.csv")!!

  csvReader().open(csvFile) {

    readAllAsSequence().forEach {

      if (it[0] != "constructionTime" && transaction { StoneCutter.find { StoneCutters.requiredLevel eq it[2].toInt() }.empty() } )

        StoneCutter.new {
          constructionTime = it[0].toLong()
          neededResources = stringToMap(it[1])
            .mapValues { entry -> entry.value.toInt() }
          requiredLevel = it[2].toInt()
        }

    }
  }
}