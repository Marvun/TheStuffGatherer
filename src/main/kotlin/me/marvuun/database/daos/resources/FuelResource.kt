package me.marvuun.database.daos.resources

import me.marvuun.database.tables.resources.FuelResources
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FuelResource(id: EntityID<String>) : Resource<String>(id) {
    companion object : EntityClass<String, FuelResource>(FuelResources)

    override var name by FuelResources.id
    override var short by FuelResources.short
    override var type by FuelResources.type
    var heatLevel by FuelResources.heatLevel
}
