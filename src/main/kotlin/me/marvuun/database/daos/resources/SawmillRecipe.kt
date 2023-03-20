package me.marvuun.database.daos.resources

import me.marvuun.database.daos.transformResources
import me.marvuun.database.tables.resources.SawmillRecipes
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class SawmillRecipe(id: EntityID<String>): Recipe<String>(id) {
  companion object: EntityClass<String, SawmillRecipe>(SawmillRecipes)

  override var name by SawmillRecipes.id
  override var short by SawmillRecipes.short
  override var type by SawmillRecipes.type
  override var craftingDuration by SawmillRecipes.craftingDuration
  override var requiredLevel by SawmillRecipes.requiredLevel
  override var neededResources by SawmillRecipes.neededResources.transformResources()
  override var outputAmount by SawmillRecipes.outputAmount.transformOutputAmount()
}