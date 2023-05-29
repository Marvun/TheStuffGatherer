package me.marvuun.database.daos.resources

import dev.kord.core.entity.User
import me.marvuun.database.daos.Level
import me.marvuun.database.daos.Quest.Companion.transform
import me.marvuun.database.tables.resources.*
import me.marvuun.enums.ResourceCategories
import me.marvuun.util.stringToMap
import me.marvuun.util.toIntRange
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Resource<T : Comparable<T>>(id: EntityID<T>) : Entity<T>(id) {
    abstract var name: EntityID<String>
    abstract var short: String
    abstract var type: ResourceCategories

    protected fun Column<String>.transformOutputAmount() =
        transform(
            {
                println(it.toString())
                it.toString()
            },
            {
                stringToMap(it)
                    .mapValues { entry -> entry.value.toIntRange() }
            },
        )
    fun getLevelForResourceCategory(user: User): Int {
        val levels = transaction { Level.findById(user.id.value)!! }
        return when (type) {
            ResourceCategories.ORE -> levels.miningLevel
            ResourceCategories.LOG -> levels.woodcuttingLevel
            ResourceCategories.STONE, ResourceCategories.GEM, ResourceCategories.SAND, ResourceCategories.FUEL -> levels.extractionLevel
            ResourceCategories.BERRY, ResourceCategories.FRUIT -> levels.harvestingLevel
            ResourceCategories.ANIMAL -> TODO()
            ResourceCategories.MEAT -> TODO()
            ResourceCategories.SKIN -> TODO()
            ResourceCategories.FISH -> levels.fishingLevel
            ResourceCategories.PLANT, ResourceCategories.HERB -> levels.botanyLevel
            ResourceCategories.NUGGET, ResourceCategories.INGOT -> levels.smeltingLevel
            ResourceCategories.PLANK -> levels.sawingLevel
            ResourceCategories.STONE_BLOCK -> levels.stoneCuttingLevel
            ResourceCategories.BLUEPRINT -> throw Exception("Something went wrong. A Blueprint should never require a level.")
        }
    }
}

fun getResourceFromShort(short: String) =
    transaction {
        when {
            short.contains("fur_") -> FurnaceRecipe.find { FurnaceRecipes.short eq short }.first()
            short.contains("saw_") -> SawmillRecipe.find { SawmillRecipes.short eq short }.first()
            short.contains("stc_") -> StoneCutterRecipe.find { StoneCutterRecipes.short eq short }.first()
            short.contains("fuel_") -> FuelResource.find { FuelResources.short eq short }.first()
            else -> RawResource.find { RawResources.short eq short }.first()
        }
    }

fun getResourceFromName(name: String) =
    transaction {
        var resource: Resource<String>?
        resource = FurnaceRecipe.find { FurnaceRecipes.id eq name }.firstOrNull()
        if (resource == null) {
            resource = SawmillRecipe.find { SawmillRecipes.id eq name }.firstOrNull()
        }
        if (resource == null) {
            resource = StoneCutterRecipe.find { StoneCutterRecipes.id eq name }.firstOrNull()
        }
        if (resource == null) {
            resource = FuelResource.find { FuelResources.id eq name }.firstOrNull()
        }
        if (resource == null) {
            RawResource.find { RawResources.short eq name }.firstOrNull()
        }
        resource!!
    }

fun getResourcesDisplayName(resources: Map<String, Int>, delimiter: String, multiplier: Int = 1): String {
    val readableResources = mutableListOf<String>()
    resources.forEach { (short, amount) ->
        readableResources.add("${amount * multiplier}x ${getResourceFromShort(short).name.value}")
    }
    return readableResources.joinToString(delimiter)
}
