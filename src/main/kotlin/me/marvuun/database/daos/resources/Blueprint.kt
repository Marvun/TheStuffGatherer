package me.marvuun.database.daos.resources

import me.marvuun.database.daos.PlayerSite.Companion.transform
import me.marvuun.database.tables.resources.Blueprints
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

class Blueprint(id: EntityID<String>): Resource<String>(id) {
  companion object: EntityClass<String, Blueprint>(Blueprints)

  override var name by Blueprints.id
  override var short by Blueprints.short
  override var type by Blueprints.type
  var rarity by Blueprints.rarity
  var obtainableFrom by Blueprints.obtainableFrom.transformList()
  var price by Blueprints.price
}

private fun Column<String>.transformList() =
  transform({
    it.toString()
  }, {
    it.split(", ")
  })