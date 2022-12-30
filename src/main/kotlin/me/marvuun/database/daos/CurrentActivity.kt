package me.marvuun.database.daos

import me.marvuun.database.tables.CurrentActivities
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CurrentActivity(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<CurrentActivity>(CurrentActivities)

  var guildId by CurrentActivities.guildId
  var channelId by CurrentActivities.channelId
  var userId by CurrentActivities.userId
  var activityEnd by CurrentActivities.activityEnd

}