package me.marvuun.enums

enum class ResourceCategories {
    ORE, LOG, STONE, GEM, SAND, BERRY, ANIMAL, MEAT, SKIN, FISH, PLANT, FRUIT, NUGGET, FUEL, HERB, INGOT, PLANK, STONE_BLOCK, BLUEPRINT;

    companion object {
        fun getFromString(s: String): ResourceCategories = ResourceCategories.values().find { it.name == s || it.name.lowercase() == s }!!
    }
}
