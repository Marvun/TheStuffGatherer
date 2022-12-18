package com.theStuffGatherer.DTOs

import com.theStuffGatherer.enums.resourceTypes.OreTypes
import com.theStuffGatherer.enums.RarityTypes

data class MineDTO(
  val name: String = "",
  val rarity: RarityTypes = RarityTypes.NONE,
  val totalResources: MutableMap<OreTypes, Int> = mutableMapOf(),
  val currentResources: MutableMap<OreTypes, Int> = mutableMapOf(),
  val travelTime: Long = 0L
)