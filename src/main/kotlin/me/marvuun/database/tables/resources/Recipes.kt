package me.marvuun.database.tables.resources

open class Recipes : Resources() {
    val craftingDuration = long("crafting_duration")
    val requiredLevel = integer("required_level")
    val neededResources = varchar("needed_resources", 255)
    val outputAmount = varchar("output_amount", 255)
}

object FurnaceRecipes : Recipes() {
    val requiredHeatLevel = integer("required_heat_level")
}

object SawmillRecipes : Recipes()

object StoneCutterRecipes : Recipes()
