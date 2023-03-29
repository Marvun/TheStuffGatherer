package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

open class Stations : IntIdTable() {
    val constructionTime = long("construction_time")
    val neededResources = varchar("needed_resources", 255)
    val requiredLevel = integer("required_level")
}

object Furnaces : Stations()
object Sawmills : Stations()
object StoneCutters : Stations()
