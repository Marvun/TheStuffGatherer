package me.marvuun.database.daos

import dev.kord.core.behavior.channel.createEmbed
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.marvuun.database.tables.Levels
import me.marvuun.enums.ResourceCategories
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.reflect.KMutableProperty0

class Level(id: EntityID<ULong>) : Entity<ULong>(id) {
  companion object : EntityClass<ULong, Level>(Levels)

  var userId by Levels.id

  var gatheringLevel by Levels.gatheringLevel
  var currentGatheringExp by Levels.currentGatheringExp
  var neededGatheringExp by Levels.neededGatheringExp

  var miningLevel by Levels.miningLevel
  var currentMiningExp by Levels.currentMiningExp
  var neededMiningExp by Levels.neededMiningExp

  var extractionLevel by Levels.extractionLevel
  var currentExtractionExp by Levels.currentExtractionExp
  var neededExtractionExp by Levels.neededExtractionExp

  var woodcuttingLevel by Levels.woodcuttingLevel
  var currentWoodcuttingExp by Levels.currentWoodcuttingExp
  var neededWoodcuttingExp by Levels.neededWoodcuttingExp

  var botanyLevel by Levels.botanyLevel
  var currentBotanyExp by Levels.currentBotanyExp
  var neededBotanyExp by Levels.neededBotanyExp

  var harvestingLevel by Levels.harvestingLevel
  var currentHarvestingExp by Levels.currentHarvestingExp
  var neededHarvestingExp by Levels.neededHarvestingExp

  var fishingLevel by Levels.fishingLevel
  var currentFishingExp by Levels.currentFishingExp
  var neededFishingExp by Levels.neededFishingExp


  var context: GuildSlashCommandEvent<NoArgs>? = null
  private suspend fun checkForLevelUp() {

    if (currentGatheringExp >= neededGatheringExp) {
      transaction {
        currentGatheringExp -= neededGatheringExp
        gatheringLevel += 1
        neededGatheringExp = getNeededGatheringExperience()
      }
      sendLevelUpMessage(context, ::gatheringLevel, gatheringLevel)
      checkForLevelUp()
    }
    if (currentMiningExp >= neededMiningExp) {
      transaction {
        currentMiningExp -= neededMiningExp
        miningLevel += 1
        neededMiningExp = getNeededExperience(miningLevel)
      }
      sendLevelUpMessage(context, ::miningLevel, miningLevel)
      checkForLevelUp()
    }
    if (currentWoodcuttingExp >= neededWoodcuttingExp) {
      transaction {
        currentWoodcuttingExp -= neededWoodcuttingExp
        woodcuttingLevel += 1
        neededWoodcuttingExp = getNeededExperience(woodcuttingLevel)
      }
      sendLevelUpMessage(context, ::woodcuttingLevel, woodcuttingLevel)
      checkForLevelUp()
    }
    if (currentExtractionExp >= neededExtractionExp) {
      transaction {
        currentExtractionExp -= neededExtractionExp
        extractionLevel += 1
        neededExtractionExp = getNeededExperience(extractionLevel)
      }
      sendLevelUpMessage(context, ::extractionLevel, extractionLevel)
      checkForLevelUp()
    }
    if (currentBotanyExp >= neededBotanyExp) {
      transaction {
        currentBotanyExp -= neededBotanyExp
        botanyLevel += 1
        neededBotanyExp = getNeededExperience(botanyLevel)
      }
      sendLevelUpMessage(context, ::botanyLevel, botanyLevel)
      checkForLevelUp()
    }
    if (currentHarvestingExp >= neededHarvestingExp) {
      transaction {
        currentHarvestingExp -= neededHarvestingExp
        harvestingLevel += 1
        neededHarvestingExp = getNeededExperience(harvestingLevel)
      }
      sendLevelUpMessage(context, ::harvestingLevel, harvestingLevel)
      checkForLevelUp()
    }
    if (currentFishingExp >= neededFishingExp) {
      transaction {
        currentFishingExp -= neededFishingExp
        fishingLevel += 1
        neededFishingExp = getNeededExperience(fishingLevel)
      }
      sendLevelUpMessage(context, ::fishingLevel, fishingLevel)
      checkForLevelUp()
    }
  }

  private suspend fun sendLevelUpMessage(
    context: GuildSlashCommandEvent<NoArgs>?,
    levelType: KMutableProperty0<Int>,
    level: Int
    ) {
    val levelName = levelType.name.replace("Level", "")
    context!!.channel.createEmbed {
      title = "${context.author.username} leveled up at $levelName."
      description = "${context.author.username} is now level $level."
    }
  }

  private fun getNeededGatheringExperience() = ((gatheringLevel + 1) * 10.0).pow(2).toInt()

  private fun getNeededExperience(level: Int) = ((level + 1) * sqrt(50.0)).pow(2).toInt()

  suspend fun addExperience(resource: Resource, amount: Int) {

    val exp = resource.maxAtLevel * 2 * amount
    transaction {
      when (resource.type) {
        ResourceCategories.ORE -> currentMiningExp += exp
        ResourceCategories.LOG -> currentWoodcuttingExp += exp
        ResourceCategories.STONE, ResourceCategories.GEM, ResourceCategories.SAND, ResourceCategories.FUEL -> currentExtractionExp += exp
        ResourceCategories.BERRY, ResourceCategories.FRUIT -> currentHarvestingExp += exp
        ResourceCategories.ANIMAL -> TODO()
        ResourceCategories.MEAT -> TODO()
        ResourceCategories.SKIN -> TODO()
        ResourceCategories.FISH -> currentFishingExp += exp
        ResourceCategories.PLANT, ResourceCategories.HERB -> currentBotanyExp += exp
        ResourceCategories.NUGGET -> TODO()

      }
      currentGatheringExp += exp

    }
    checkForLevelUp()

  }
}

