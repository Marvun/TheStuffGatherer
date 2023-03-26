package me.marvuun.database.tables


import me.marvuun.enums.ActivityTypes
import org.jetbrains.exposed.dao.id.IdTable

@OptIn(ExperimentalUnsignedTypes::class)
object Players : IdTable<ULong>() {

  override val id = ulong("user_id").entityId()
  override val primaryKey = PrimaryKey(id)

  val activityStartTime = long("activity_start_time").default(System.currentTimeMillis())
  val activityDuration = long("activity_duration").default(0L)
  val currentActivityType = enumerationByName<ActivityTypes>("current_activity_type", 255).nullable()
  val currentActivity = varchar("current_activity", 255).default("")
  val currentLocation = uuid("current_location").nullable()
  val currentlyMaking = varchar("currently_making", 255).default("")
  val destination = uuid("destination").nullable()
  val occupiedCoordinates = mediumText("occupied_coordinates")
  val quests = varchar("quests", 1023).default("")
  val money = long("money").default(0L)
}