package me.marvuun.database.daos.resources

import me.marvuun.database.daos.transformResources
import me.marvuun.database.tables.resources.FurnaceRecipes
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FurnaceRecipe(id: EntityID<String>): Recipe<String>(id) {
  companion object: EntityClass<String, FurnaceRecipe>(FurnaceRecipes)

  override var name by FurnaceRecipes.id
  override var short by FurnaceRecipes.short
  override var type by FurnaceRecipes.type
  override var craftingDuration by FurnaceRecipes.craftingDuration
  override var requiredLevel by FurnaceRecipes.requiredLevel
  override var neededResources by FurnaceRecipes.neededResources.transformResources()
  override var outputAmount by FurnaceRecipes.outputAmount.transformOutputAmount()
  var requiredHeatLevel by FurnaceRecipes.requiredHeatLevel
}