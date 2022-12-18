package com.theStuffGatherer.databass.DAOs.sites

import com.theStuffGatherer.DTOs.MineDTO
import com.theStuffGatherer.databass.DAOs.Player
import com.theStuffGatherer.databass.tables.sites.MinesTable
import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.enums.resourceTypes.OreTypes
import com.theStuffGatherer.util.getLocationRarity
import com.theStuffGatherer.util.stringToMap
import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

class Mines(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<Mines>(MinesTable) {
    fun generateResources(rarity:RarityTypes): MutableMap<OreTypes, Int> {
      val map = mutableMapOf<OreTypes, Int>()
      val (typeCount, amount) =
        when (rarity) {
          RarityTypes.S -> arrayOf((3..5).random(), 50..100)
          RarityTypes.A -> arrayOf(3, 40..60)
          RarityTypes.B-> arrayOf((2..3).random(), 30..35)
          RarityTypes.C -> arrayOf(2, 20..30)
          RarityTypes.D -> arrayOf((1..2).random(), 10..20)
          RarityTypes.NONE -> arrayOf(0, 0..0)
        }
      if (typeCount is Int && amount is IntRange) {
        repeat(typeCount) {
          val type = OreTypes.values().random()
          if (map[type] == null) map[type] = amount.random()
          else map[type] = map[type]!! + amount.random()
        }
      }
      return map
    }
  }

  var userId by Player referencedOn MinesTable.userId
  var currentSMine by MinesTable.currentSMine.transformMine()
  var currentAMine by MinesTable.currentAMine.transformMine()
  var currentBMine by MinesTable.currentBMine.transformMine()
  var currentCMine by MinesTable.currentCMine.transformMine()
  var currentDMine by MinesTable.currentDMine.transformMine()

  private fun Column<String>.transformMine(): ColumnWithTransform<String, MineDTO> {
    return this.transform(
      { tReal ->
        "${tReal.name}$${tReal.rarity.name}$${tReal.totalResources._toString()}$${tReal.currentResources._toString()}$${tReal.travelTime}"
      },
      { tColumn ->
        val properties = tColumn.split("$")
        if (tColumn == "") MineDTO()
        else MineDTO(
          properties[0],
          RarityTypes.values().find { it.name == properties[1]}!!,
          getResources(properties[2]),
          getResources(properties[3]),
          properties[4].toLong()
        )

      })
  }
  fun getResources(s: String) = s.removeSurrounding("{", "}").split(", ").associate {
    val (left, right) = it.split("=")
    OreTypes.getFromShort(left) to right.toInt()
  }.toMutableMap()


  fun getCurrentMineByRarity(rarity: RarityTypes): MineDTO {
    return when (rarity) {
      RarityTypes.S -> this.currentSMine
      RarityTypes.A -> this.currentAMine
      RarityTypes.B -> this.currentBMine
      RarityTypes.C -> this.currentCMine
      RarityTypes.D -> this.currentDMine
      RarityTypes.NONE -> MineDTO()
    }
  }

  fun getMine(location: String) = getCurrentMineByRarity(getLocationRarity(location))

  fun setMine(mine: MineDTO) {
    when (mine.rarity) {
      RarityTypes.S -> this.currentSMine = mine
      RarityTypes.A -> this.currentAMine = mine
      RarityTypes.B -> this.currentBMine = mine
      RarityTypes.C -> this.currentCMine = mine
      RarityTypes.D -> this.currentDMine = mine
      RarityTypes.NONE -> {}
    }
  }

  fun setEmptyMine(rarity: RarityTypes) {
    when (rarity) {
      RarityTypes.S -> this.currentSMine = MineDTO()
      RarityTypes.A -> this.currentAMine = MineDTO()
      RarityTypes.B -> this.currentBMine = MineDTO()
      RarityTypes.C -> this.currentCMine = MineDTO()
      RarityTypes.D -> this.currentDMine = MineDTO()
      RarityTypes.NONE -> {}
    }
  }

}

fun MutableMap<OreTypes, Int>._toString(): String {
  val list = mutableListOf<String>()
  forEach { (key, value) ->
    list.add("${key.short}=$value")
  }
  return "{${list.joinToString(", ")}}"
}

fun calculateMiningDuration(map: MutableMap<OreTypes, Int>): Long {
  var duration = 0L

  map.forEach { (oreType, amount) ->
    duration += oreType.miningDuration * amount
  }

  return duration
}
