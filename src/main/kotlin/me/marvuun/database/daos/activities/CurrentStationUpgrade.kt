package me.marvuun.database.daos.activities

import me.marvuun.database.tables.CurrentStationUpgrades
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CurrentStationUpgrade(id: EntityID<Int>) : CurrentActivity<Int>(id) {
    companion object : EntityClass<Int, CurrentStationUpgrade>(CurrentStationUpgrades)

    override var guildId by CurrentStationUpgrades.guildId
    override var channelId by CurrentStationUpgrades.channelId
    override var userId by CurrentStationUpgrades.userId
    override var activityEnd by CurrentStationUpgrades.activityEnd
}
