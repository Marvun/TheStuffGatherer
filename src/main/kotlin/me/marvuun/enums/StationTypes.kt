package me.marvuun.enums

import me.marvuun.database.daos.resources.FurnaceRecipe
import me.marvuun.database.daos.resources.Recipe
import me.marvuun.database.daos.resources.SawmillRecipe

enum class StationTypes {
  SAWMILL, FURNACE, STONECUTTER;

  fun getDisplayName() = this.name.lowercase().replaceFirstChar { it.uppercase() }
}

fun String.getStationType() = StationTypes.values().find { it.name == this.uppercase().replace(" ", "") }!!

fun getStationTypeFromResource(resource: Recipe<*>) =
  when (resource) {
    is FurnaceRecipe -> StationTypes.FURNACE
    is SawmillRecipe -> StationTypes.SAWMILL
    else -> StationTypes.STONECUTTER
  }