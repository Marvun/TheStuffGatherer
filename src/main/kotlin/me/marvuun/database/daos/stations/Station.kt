package me.marvuun.database.daos.stations

import me.marvuun.database.daos.Home
import me.marvuun.database.daos.Inventory
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.enums.StationTypes
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Station<T : Comparable<T>>(id: EntityID<T>) : Entity<T>(id){
  abstract var level: EntityID<Int>
  abstract var constructionTime: Long
  abstract var neededResources: Map<String, Int>
  abstract var requiredLevel: Int

  fun getMissingResources(inventory: List<Inventory>): String {
    var missingMaterials = ""
    this.neededResources.forEach { (resource, amount) ->
      val inventoryEntry = transaction { inventory.find { it.itemId == resource } }

      if ((inventoryEntry == null) || (inventoryEntry.amount < amount))
        missingMaterials += "${getResourceFromShort(resource).name.value}: ${inventoryEntry?.amount ?: "0"}/$amount\n"

    }
    return missingMaterials
  }
}

fun getNextStation(station: StationTypes, home: Home) =
    transaction {
      when (station) {
        StationTypes.FURNACE -> Furnace.findById(home.furnaceLevel + 1)
        StationTypes.SAWMILL -> Sawmill.findById(home.sawmillLevel + 1)
        StationTypes.STONECUTTER -> StoneCutter.findById(home.stoneCutterLevel + 1)
      }
    }





