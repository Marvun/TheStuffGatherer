package me.marvuun.database.daos.stations

import me.marvuun.database.daos.location.transformResources
import me.marvuun.database.tables.Sawmills
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Sawmill(id: EntityID<Int>) : Station<Int>(id){
  companion object : EntityClass<Int, Sawmill>(Sawmills)

  override var level by Sawmills.id
  override var constructionTime by Sawmills.constructionTime
  override var neededResources by Sawmills.neededResources.transformResources()
  override var requiredLevel by Sawmills.requiredLevel
}