package me.marvuun.database.daos.activities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID

abstract class CurrentActivity<T : Comparable<T>>(id: EntityID<T>) : Entity<T>(id) {
    abstract var guildId: ULong
    abstract var channelId: ULong
    abstract var userId: ULong
    abstract var activityEnd: Long
}
