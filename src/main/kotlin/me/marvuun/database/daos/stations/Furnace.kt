package me.marvuun.database.daos.stations

import me.marvuun.database.daos.location.transformResources
import me.marvuun.database.tables.Furnaces
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Furnace(id: EntityID<Int>) : Station<Int>(id) {
    companion object : EntityClass<Int, Furnace>(Furnaces)

    override var level by Furnaces.id
    override var constructionTime by Furnaces.constructionTime
    override var neededResources by Furnaces.neededResources.transformResources()
    override var requiredLevel by Furnaces.requiredLevel
}
