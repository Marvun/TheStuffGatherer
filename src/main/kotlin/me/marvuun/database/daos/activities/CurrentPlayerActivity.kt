package me.marvuun.database.daos.activities

import me.marvuun.database.tables.CurrentPlayerActivities
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CurrentPlayerActivity(id: EntityID<Int>) : CurrentActivity<Int>(id) {
    companion object : EntityClass<Int, CurrentPlayerActivity>(CurrentPlayerActivities)
    override var guildId by CurrentPlayerActivities.guildId
    override var channelId by CurrentPlayerActivities.channelId
    override var userId by CurrentPlayerActivities.userId
    override var activityEnd by CurrentPlayerActivities.activityEnd
}
