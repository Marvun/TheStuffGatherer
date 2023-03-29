package me.marvuun.database.daos.location

import me.marvuun.database.daos.PlayerSite.Companion.transform
import me.marvuun.database.tables.locations.Sites
import me.marvuun.util.stringToMap
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import java.util.*

class Site(id: EntityID<UUID>) : Location(id) {
    companion object : EntityClass<UUID, Site>(Sites)

    override var xCoordinate by Sites.xCoordinate
    override var yCoordinate by Sites.yCoordinate
    var siteId by Sites.id
    var userId by Sites.userId
    var type by Sites.type
    var rarity by Sites.rarity
    var totalResources by Sites.totalResources.transformResources()
    var currentResources by Sites.currentResources.transformResources()
    var travelTime by Sites.travelTime
}

fun Column<String>.transformResources() =
    transform({
        it.toString()
    }, {
        stringToMap(it)
            .mapValues { entry -> entry.value.toInt() }
    })
