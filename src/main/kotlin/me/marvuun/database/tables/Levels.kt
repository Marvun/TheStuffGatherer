package me.marvuun.database.tables

import org.jetbrains.exposed.dao.id.IdTable

@OptIn(ExperimentalUnsignedTypes::class)
object Levels : IdTable<ULong>() {

    override val id = ulong("user_id").entityId()
    override val primaryKey = PrimaryKey(id)

    val gatheringLevel = integer("gathering_level").default(0)
    val currentGatheringExp = integer("current_gathering_exp").default(0)
    val neededGatheringExp = integer("needed_gathering_exp").default(100)

    val miningLevel = integer("mining_level").default(0)
    val currentMiningExp = integer("current_mining_exp").default(0)
    val neededMiningExp = integer("needed_mining_exp").default(50)

    val extractionLevel = integer("extraction_level").default(0)
    val currentExtractionExp = integer("current_extraction_exp").default(0)
    val neededExtractionExp = integer("needed_extraction_exp").default(50)

    val woodcuttingLevel = integer("woodcutting_level").default(0)
    val currentWoodcuttingExp = integer("current_woodcutting_exp").default(0)
    val neededWoodcuttingExp = integer("needed_woodcutting_exp").default(50)

    val botanyLevel = integer("botany_level").default(0)
    val currentBotanyExp = integer("current_botany_exp").default(0)
    val neededBotanyExp = integer("needed_botany_exp").default(50)

    val harvestingLevel = integer("harvesting_level").default(0)
    val currentHarvestingExp = integer("current_harvesting_exp").default(0)
    val neededHarvestingExp = integer("needed_harvesting_exp").default(50)

    val fishingLevel = integer("fishing_level").default(0)
    val currentFishingExp = integer("current_fishing_exp").default(0)
    val neededFishingExp = integer("needed_fishing_exp").default(50)

    val meltingLevel = integer("melting_level").default(0)
    val currentMeltingExp = integer("current_melting_exp").default(0)
    val neededMeltingExp = integer("needed_melting_exp").default(50)

    val sawingLevel = integer("sawing_level").default(0)
    val currentSawingExp = integer("current_sawing_exp").default(0)
    val neededSawingExp = integer("needed_sawing_exp").default(50)

    val stoneCuttingLevel = integer("stone_cutting_level").default(0)
    val currentStoneCuttingExp = integer("current_stone_cutting_exp").default(0)
    val neededStoneCuttingExp = integer("needed_stone_cutting_exp").default(50)
}
