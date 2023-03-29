package me.marvuun.database.daos.resources

import me.marvuun.database.daos.location.transformResources
import me.marvuun.database.tables.resources.StoneCutterRecipes
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StoneCutterRecipe(id: EntityID<String>) : Recipe<String>(id) {
    companion object : EntityClass<String, StoneCutterRecipe>(StoneCutterRecipes)

    override var name by StoneCutterRecipes.id
    override var short by StoneCutterRecipes.short
    override var type by StoneCutterRecipes.type
    override var craftingDuration by StoneCutterRecipes.craftingDuration
    override var requiredLevel by StoneCutterRecipes.requiredLevel
    override var neededResources by StoneCutterRecipes.neededResources.transformResources()
    override var outputAmount by StoneCutterRecipes.outputAmount.transformOutputAmount()
}
