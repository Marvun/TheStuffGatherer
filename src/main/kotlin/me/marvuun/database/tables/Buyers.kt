package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IdTable
import java.util.*

object Buyers: IdTable<UUID>() {
  override val id = uuid("buyer_id").entityId()
  override val primaryKey = PrimaryKey(id)

  val sellableItems = varchar("sellable_items", 255)
  val money = integer("money")
  val specialRewards = varchar("special_rewards", 255)
  val timeLimit = long("time_limit").nullable()
}