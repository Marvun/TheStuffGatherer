package me.marvuun.database.tables.resources

object RawResources : Resources() {
  val gatherDuration = long("gather_duration")
  val siteTypes = varchar("site_type",255)
  val maxAtLevel = integer("max_at_level")
  val maxAmount = integer("max_amount")
}