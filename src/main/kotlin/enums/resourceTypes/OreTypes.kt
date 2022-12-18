package com.theStuffGatherer.enums.resourceTypes

enum class OreTypes(val short: String, private val displayName: String, val miningDuration: Long) : ResourceTypes {
  SUNUYARA_ORE("sun", "Sunuyara Ore", 20000),
  SUFARA_ORE("suf", "Sufara Ore", 25000),
  VELIA_ORE("vel", "Velia Ore", 30000),
  ENARD_ORE("en", "Enard Ore", 35000),
  SABZARA_ORE("sab", "Sabzara Ore", 40000);

  companion object {
    fun getFromShort(s: String): OreTypes = OreTypes.values().find { it.short == s }!!

    fun getNameFromShort(s: String): String = getFromShort(s).displayName

    fun getOreTypeFromName(s: String): OreTypes = OreTypes.values().find { it.name == s }!!

  }

  override fun getDisplayName(): String = displayName
}