package me.marvuun.database.daos

import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import me.marvuun.database.daos.resources.FurnaceRecipe
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.database.daos.resources.Recipe
import me.marvuun.database.daos.resources.Resource
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

  var meltingLevel by Levels.meltingLevel
  var currentMeltingExp by Levels.currentMeltingExp
  var neededMeltingExp by Levels.neededMeltingExp

  var sawingLevel by Levels.sawingLevel
  var currentSawingExp by Levels.currentSawingExp
  var neededSawingExp by Levels.neededSawingExp

  var stoneCuttingLevel by Levels.stoneCuttingLevel
  var currentStoneCuttingExp by Levels.currentStoneCuttingExp
  var neededStoneCuttingExp by Levels.neededStoneCuttingExp



  private suspend fun checkForLevelUp(user: User, channel: MessageChannel) {

    if (currentGatheringExp >= neededGatheringExp) {
      transaction {
        currentGatheringExp -= neededGatheringExp
        gatheringLevel += 1
        neededGatheringExp = getNeededGatheringExperience()
      }
      sendLevelUpMessage(user, channel, ::gatheringLevel, gatheringLevel)
      checkForLevelUp(user, channel)
    }
    if (currentMiningExp >= neededMiningExp) {
      transaction {
        currentMiningExp -= neededMiningExp
        miningLevel += 1
        neededMiningExp = getNeededExperience(miningLevel)
      }
      sendLevelUpMessage(user, channel, ::miningLevel, miningLevel)
      checkForLevelUp(user, channel)
    }
    if (currentWoodcuttingExp >= neededWoodcuttingExp) {
      transaction {
        currentWoodcuttingExp -= neededWoodcuttingExp
        woodcuttingLevel += 1
        neededWoodcuttingExp = getNeededExperience(woodcuttingLevel)
      }
      sendLevelUpMessage(user, channel, ::woodcuttingLevel, woodcuttingLevel)
      checkForLevelUp(user, channel)
    }
    if (currentExtractionExp >= neededExtractionExp) {
      transaction {
        currentExtractionExp -= neededExtractionExp
        extractionLevel += 1
        neededExtractionExp = getNeededExperience(extractionLevel)
      }
      sendLevelUpMessage(user, channel, ::extractionLevel, extractionLevel)
      checkForLevelUp(user, channel)
    }
    if (currentBotanyExp >= neededBotanyExp) {
      transaction {
        currentBotanyExp -= neededBotanyExp
        botanyLevel += 1
        neededBotanyExp = getNeededExperience(botanyLevel)
      }
      sendLevelUpMessage(user, channel, ::botanyLevel, botanyLevel)
      checkForLevelUp(user, channel)
    }
    if (currentHarvestingExp >= neededHarvestingExp) {
      transaction {
        currentHarvestingExp -= neededHarvestingExp
        harvestingLevel += 1
        neededHarvestingExp = getNeededExperience(harvestingLevel)
      }
      sendLevelUpMessage(user, channel, ::harvestingLevel, harvestingLevel)
      checkForLevelUp(user, channel)
    }
    if (currentFishingExp >= neededFishingExp) {
      transaction {
        currentFishingExp -= neededFishingExp
        fishingLevel += 1
        neededFishingExp = getNeededExperience(fishingLevel)
      }
      sendLevelUpMessage(user, channel, ::fishingLevel, fishingLevel)
      checkForLevelUp(user, channel)
    }
    if (currentMeltingExp >= neededMeltingExp) {
      transaction {
        currentMeltingExp -= neededMeltingExp
        meltingLevel += 1
        neededMeltingExp = getNeededExperience(meltingLevel)
      }
      sendLevelUpMessage(user, channel, ::meltingLevel, meltingLevel)
      checkForLevelUp(user, channel)
    }
    if (currentSawingExp >= neededSawingExp) {
      transaction {
        currentSawingExp -= neededSawingExp
        sawingLevel += 1
        neededSawingExp = getNeededExperience(sawingLevel)
      }
      sendLevelUpMessage(user, channel, ::sawingLevel, sawingLevel)
      checkForLevelUp(user, channel)
    }
    if (currentStoneCuttingExp >= neededStoneCuttingExp) {
      transaction {
        currentStoneCuttingExp -= neededStoneCuttingExp
        stoneCuttingLevel += 1
        neededStoneCuttingExp = getNeededExperience(stoneCuttingLevel)
      }
      sendLevelUpMessage(user, channel, ::stoneCuttingLevel, stoneCuttingLevel)
      checkForLevelUp(user, channel)
    }
  }

  private suspend fun sendLevelUpMessage(
    user: User,
    channel: MessageChannel,
    levelType: KMutableProperty0<Int>,
    level: Int
    ) {
    val levelName = levelType.name.replace("Level", "")
    channel.createEmbed {
      title = "${user.username} leveled up at $levelName."
      description = "${user.username} is now level $level."
    }
  }

  private fun getNeededGatheringExperience() = ((gatheringLevel + 1) * 10.0).pow(2).toInt()

  private fun getNeededExperience(level: Int) = ((level + 1) * sqrt(50.0)).pow(2).toInt()

  suspend fun addExperience(resource: Resource<String>, amount: Int, user: User, channel: MessageChannel) {
    var exp = if (resource is RawResource) {
       resource.maxAtLevel * 2 * amount

    }
    else {
      (resource as Recipe<*>).requiredLevel * 20 * amount
    }
    transaction {
      if (resource.name.value == "Firewood")
        currentWoodcuttingExp += exp
      else
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
          ResourceCategories.NUGGET -> currentMeltingExp += exp
          ResourceCategories.INGOT -> {
            exp = (exp * 1.5).toInt()
            currentMeltingExp += exp
          }
          ResourceCategories.PLANK -> currentSawingExp += exp
          ResourceCategories.STONE_BLOCK -> {
            exp = (exp * 1.5).toInt()
            currentStoneCuttingExp += exp
          }


        }
      currentGatheringExp += exp

    }
    checkForLevelUp(user, channel)

  }
}

