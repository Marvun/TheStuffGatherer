package com.theStuffGatherer.util

import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.enums.resourceTypes.OreTypes

fun aOrAn(reference: String) = if (reference.matches(Regex("[aAsS]"))) "an" else "a"

fun getLocationRarity(location: String): RarityTypes {
  val rarityString = location.split(" ").first().split("-").first()
  return RarityTypes.getFromString(rarityString)
}

fun stringToMap(s: String): MutableMap<String, String> = s.removeSurrounding("{", "}").split(",").associate {
  val (left, right) = it.split("=")
  left to right
}.toMutableMap()