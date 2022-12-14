package com.theStuffGatherer.enums

enum class RarityTypes(val range: IntRange) {
  S(96..100),
  A(86..95),
  B(61..85),
  C(26..60),
  D(0..25)
}