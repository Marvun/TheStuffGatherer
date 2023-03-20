package me.marvuun.database.tables

import me.marvuun.enums.StationTypes
import org.jetbrains.exposed.dao.id.IdTable
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
object Homes: IdTable<UUID>() {

  override val id = uuid("home_id").entityId()
  override val primaryKey = PrimaryKey(id)

  val userId = ulong("user_id")
  val travelTime = integer("travel_time").default(0)
  val furnaceLevel = integer("furnace_level").default(0)
  val sawmillLevel = integer("sawmill_level").default(0)
  val stoneCutterLevel = integer("stone_cutter_level").default(0)
  val upgradeStartTime = long("upgrade_start_time").default(System.currentTimeMillis())
  val upgradeDuration = long("upgrade_duration").default(0L)
  val upgradingStation = enumerationByName<StationTypes>("upgrading_station", 255).nullable()
}