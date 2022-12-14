package com.theStuffGatherer.logic

import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.enums.SiteTypes

fun getRewards(duration: Long): MutableMap<SiteTypes, MutableList<RarityTypes>>{
  val map = mutableMapOf<SiteTypes, MutableList<RarityTypes>>()
  for (i in 1..duration) {
    val randomNum = (0..100).random()
    val randomNum2 = (0..100).random()
    if (randomNum2 > 99) {
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

