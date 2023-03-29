package me.marvuun.database.tables.locations

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID

abstract class Locations : IdTable<UUID>() {

    abstract override val id: Column<EntityID<UUID>>
    abstract override val primaryKey: PrimaryKey

    val xCoordinate = integer("x_coordinate")
    val yCoordinate = integer("y_coordinate")
}
