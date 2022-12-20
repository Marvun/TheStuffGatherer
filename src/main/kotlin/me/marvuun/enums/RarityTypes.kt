package me.marvuun.enums

enum class RarityTypes(val range: IntRange) {
  S(96..100),
  A(86..95),
  B(61..85),
  C(26..60),
  D(1..25);



  companion object {
    fun getFromString(s: String): RarityTypes = values().find { it.name == s || it.name.lowercase() == s }!!

  }
}