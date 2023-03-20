package me.marvuun.util

operator fun IntRange.times(i: Int) = IntRange(this.first * i, this.last * i)
