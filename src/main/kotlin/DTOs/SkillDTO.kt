package com.theStuffGatherer.DTOs

data class SkillDTO(
  val name: String = "",
  var level: Int = 0,
  var progress: Double = 0.00,
  var exp: Long = 0L,
  var maxExp: Long = 0L
)
