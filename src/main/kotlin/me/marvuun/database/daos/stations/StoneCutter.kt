package me.marvuun.database.daos.stations

import me.marvuun.database.daos.transformResources
import me.marvuun.database.tables.StoneCutters
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StoneCutter(id: EntityID<Int>) : Station<Int>(id){
  companion object : EntityClass<Int, StoneCutter>(StoneCutters)

  override var level by StoneCutters.id
  override var constructionTime by StoneCutters.constructionTime
  override var neededResources by StoneCutters.neededResources.transformResources()
  override var requiredLevel by StoneCutters.requiredLevel
}