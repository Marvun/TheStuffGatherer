package me.marvuun.database.tables.resources

import me.marvuun.enums.RarityTypes

object Blueprints: Resources() {
  val rarity = enumerationByName<RarityTypes>("rarity", 1)
  val obtainableFrom = varchar("obtainable_from", 255)
  val price = integer("price").nullable()
}