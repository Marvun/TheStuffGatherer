package me.marvuun.database.daos.resources

import me.marvuun.database.daos.Inventory
import me.marvuun.database.daos.location.Home
import me.marvuun.database.tables.resources.FurnaceRecipes
import me.marvuun.database.tables.resources.SawmillRecipes
import me.marvuun.database.tables.resources.StoneCutterRecipes
import me.marvuun.enums.StationTypes
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Recipe<T : Comparable<T>>(id: EntityID<String>) : Resource<String>(id) {

    abstract var craftingDuration: Long
    abstract var requiredLevel: Int
    abstract var neededResources: Map<String, Int>
    abstract var outputAmount: Map<String, IntRange>

    fun getMissingResources(inventory: List<Inventory>): String {
        var missingMaterials = ""
        this.neededResources.forEach { (resource, amount) ->
            val inventoryEntry = transaction { inventory.find { it.itemId == resource } }

            if ((inventoryEntry == null) || (inventoryEntry.amount < amount)) {
                missingMaterials += "${getResourceFromShort(resource).name.value}: ${inventoryEntry?.amount ?: "0"}/$amount\n"
            }
        }
        return missingMaterials
    }
}

fun getAvailableRecipes(resource: Recipe<*>? = null, stationType: StationTypes? = null, home: Home) =
    if (resource != null) {
        when (resource) {
            is FurnaceRecipe -> transaction {
                FurnaceRecipe.find { FurnaceRecipes.requiredLevel lessEq home.furnaceLevel }.toList()
            }
            is StoneCutterRecipe -> transaction {
                StoneCutterRecipe.find { StoneCutterRecipes.requiredLevel lessEq home.stoneCutterLevel }.toList()
            }
            else -> transaction { SawmillRecipe.find { SawmillRecipes.requiredLevel lessEq home.sawmillLevel }.toList() }
        }
    } else if (stationType != null) {
        when (stationType) {
            StationTypes.SAWMILL -> transaction {
                SawmillRecipe.find { SawmillRecipes.requiredLevel lessEq home.sawmillLevel }.toList()
            }

            StationTypes.FURNACE -> transaction {
                FurnaceRecipe.find { FurnaceRecipes.requiredLevel lessEq home.furnaceLevel }.toList()
            }

            StationTypes.STONECUTTER -> transaction {
                StoneCutterRecipe.find { StoneCutterRecipes.requiredLevel lessEq home.stoneCutterLevel }.toList()
            }
        }
    } else {
        throw Exception("Can't get available recipes since the resource and the station type are null.")
    }
