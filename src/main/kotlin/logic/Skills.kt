package com.theStuffGatherer.logic

import kotlin.math.roundToInt
fun calculateProgress(value: Double): String {
  var progressString = ""
  val pipeCount = (value / 4).roundToInt()
  repeat(pipeCount) {
    progressString += "\\|"
  }
  return progressString
}
