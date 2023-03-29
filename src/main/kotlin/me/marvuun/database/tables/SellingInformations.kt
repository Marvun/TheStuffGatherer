package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IdTable

object SellingInformations : IdTable<String>() {
    override val id = varchar("short", 255).entityId()
    override val primaryKey = PrimaryKey(id)

    val baseAmount = integer("base_amount")
    val basePrice = integer("base_price")
    val unlockedAtLevel = integer("unlocked_at_level")
}
