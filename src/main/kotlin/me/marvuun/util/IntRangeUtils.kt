package me.marvuun.util

fun IntRange.toMyString(multiplier: Int = 1, delimiter: String = "..") = if (this.first != this.last) "${this.first * multiplier}$delimiter${this.last * multiplier}" else (this.first * multiplier).toString()

fun Int.toIntRange() = IntRange(this, this)