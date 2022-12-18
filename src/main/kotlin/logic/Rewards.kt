package com.theStuffGatherer.logic

import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.enums.SiteTypes

fun generateRewards(duration: Long): MutableMap<SiteTypes, MutableList<RarityTypes>>{
  val map = mutableMapOf<SiteTypes, MutableList<RarityTypes>>()
  for (i in 1..100) {
    val randomNum = (1..100).random()
    val randomNum2 = (0..100).random()
    if (randomNum2 > 20) {
      val rarity = RarityTypes.values().find { randomNum in it.range }!!
      val site = SiteTypes.values().random()
      if (map[site] == null) {
        map[site] = mutableListOf(rarity)
      } else {
        map[site]!!.add(rarity)
      }
    }
  }
  map.forEach { (site, list) ->  map[site] = list.sortedBy { it.ordinal }.toMutableList() }
  return map
}

fun generateTravelTime(rarity: RarityTypes): Long {
  return when (rarity.name) {
    "S" -> (10800000L..21600000).random()
    "A" -> (7200000L..14400000).random()
    "B" -> (3600000L..5400000).random()
    "C" -> (1800000L..3600000).random()
    "D" -> (900000L..1800000).random()
    else -> 0
  }
}

