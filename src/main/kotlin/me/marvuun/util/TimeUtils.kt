package me.marvuun.util

fun millisecondsToDuration(milliseconds: Long): String {
  val hours = (milliseconds / 1000.0 / 60 / 60).toInt()
  val minutes = ((milliseconds - hours * 3600000.0) / 1000 / 60).toInt()
  val seconds = ((milliseconds - hours * 3600000.0 - minutes * 60000) / 1000).toInt()
  return listOf(hours.toString() + "h", minutes.toString() + "min", seconds.toString() + "sec").filter { it.replace(Regex("h|min|sec"), "") != "0" }.joinToString(" ")
}

fun minutesToDuration(minutes: Int): String {
  val hours = minutes / 60
  val minutesLeft = minutes % 60
  return listOf(hours.toString() + "h", minutesLeft.toString() + "min").filter { it.replace(Regex("h|min"), "") != "0" }.joinToString(" ")
}