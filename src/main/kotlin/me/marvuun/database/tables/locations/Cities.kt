package me.marvuun.database.tables.locations

import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
object Cities : Locations() {
    override val id = uuid("city_id").entityId()
    override val primaryKey = PrimaryKey(id)

    val name = varchar("name", 30)
    val userId = ulong("user_id")
    val travelTime = integer("travel_time")
    val quests = varchar("quests", 1023)
    val purchasableItems = varchar("purchasable_items", 1023)
}
