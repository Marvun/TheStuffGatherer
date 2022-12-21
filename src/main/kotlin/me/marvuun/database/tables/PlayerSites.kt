package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IdTable

@OptIn(ExperimentalUnsignedTypes::class)
object PlayerSites: IdTable<ULong>() {

  override val id = ulong("site_id").entityId()
  override val primaryKey = PrimaryKey(id)

  val mineId = uuid("mine").nullable()
  val pondId = uuid("pond").nullable()
  val lakeId = uuid("lake").nullable()
  val riverId = uuid("river").nullable()
  val forestId = uuid("forest").nullable()
  val meadowId = uuid("meadow").nullable()

}
