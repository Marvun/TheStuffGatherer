package me.marvuun.database.tables

import me.marvuun.database.tables.locations.Cities
import org.jetbrains.exposed.dao.id.IdTable
import java.util.*

object Quests : IdTable<UUID>() {
    override val id = uuid("quest_id").entityId()
    override val primaryKey = PrimaryKey(id)

    val wantedItems = varchar("sellable_items", 255)
    val money = integer("money")
    val specialRewards = varchar("special_rewards", 255)
    val timeLimit = long("time_limit").nullable()
    val city = reference("city", Cities)
}
