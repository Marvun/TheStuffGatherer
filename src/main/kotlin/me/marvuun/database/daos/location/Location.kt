package me.marvuun.database.daos.location

import dev.kord.core.entity.User
import me.marvuun.database.tables.locations.Cities
import me.marvuun.database.tables.locations.Homes
import me.marvuun.database.tables.locations.Sites
import me.marvuun.logic.getCities
import me.marvuun.logic.getHome
import me.marvuun.logic.getSites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

abstract class Location(id: EntityID<UUID>) : Entity<UUID>(id) {
    abstract var xCoordinate: Int
    abstract var yCoordinate: Int
}

fun getLocationFromUUID(userId: ULong, locationUUID: UUID) =
    transaction {
        Home.find { (Homes.userId eq userId) and (Homes.id eq locationUUID) }.firstOrNull()
            ?: City.find { (Cities.userId eq userId) and (Cities.id eq locationUUID) }.firstOrNull()
            ?: Site.find { (Sites.userId eq userId) and (Sites.id eq locationUUID) }.first()
    }

fun updateTravelTimes(destination: UUID, user: User) {
    val cities = getCities(user)
    val home = getHome(user)
    val sites = getSites(user)
    val location = getLocationFromUUID(user.id.value, destination)
    val x = location.xCoordinate
    val y = location.yCoordinate
    var xLength: Double
    var yLength: Double
    transaction {
        cities.forEach {
            xLength = abs(x - it.xCoordinate).toDouble()
            yLength = abs(y - it.yCoordinate).toDouble()
            it.travelTime = (sqrt(xLength * xLength + yLength * yLength) * 5000).toInt()
        }
        xLength = abs(x - home.xCoordinate).toDouble()
        yLength = abs(y - home.yCoordinate).toDouble()
        home.travelTime = (sqrt(xLength * xLength + yLength * yLength) * 5000).toInt()
        sites.flatMap { it.value }.forEach {
            xLength = abs(x - it.xCoordinate).toDouble()
            yLength = abs(y - it.yCoordinate).toDouble()
            it.travelTime = (sqrt(xLength * xLength + yLength * yLength) * 5000).toInt()
        }
    }
}
