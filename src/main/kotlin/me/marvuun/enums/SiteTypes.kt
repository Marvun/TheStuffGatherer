package me.marvuun.enums

enum class SiteTypes {
    LAKE, RIVER, MINE, FOREST, MEADOW;

    companion object {
        fun getFromString(s: String): SiteTypes = SiteTypes.values().find { it.name == s || it.name.lowercase() == s }!!
    }
    fun getDisplayName() = this.name.lowercase().replaceFirstChar { it.uppercase() }
}
