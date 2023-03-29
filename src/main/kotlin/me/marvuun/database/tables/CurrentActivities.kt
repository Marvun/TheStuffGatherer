package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
@OptIn(ExperimentalUnsignedTypes::class)

open class CurrentActivities : IntIdTable() {

    val guildId = ulong("guild_id")
    val channelId = ulong("channel_id")
    val userId = ulong("user_id")
    val activityEnd = long("activity_end")
}

object CurrentPlayerActivities : CurrentActivities()

object CurrentStationUpgrades : CurrentActivities()
