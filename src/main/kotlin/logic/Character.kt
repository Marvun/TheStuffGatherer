package com.theStuffGatherer.logic

import com.theStuffGatherer.databass.DAOs.Player
import com.theStuffGatherer.databass.DAOs.Sites
import com.theStuffGatherer.databass.DAOs.addSiteToDatabase
import com.theStuffGatherer.databass.DAOs.getResourcesFromSite
import com.theStuffGatherer.databass.DAOs.sites.Mines
import com.theStuffGatherer.databass.tables.InventoriesTable
import com.theStuffGatherer.databass.tables.PlayersTable
import com.theStuffGatherer.databass.tables.SitesTable
import com.theStuffGatherer.databass.tables.sites.MinesTable
import com.theStuffGatherer.enums.ActivityTypes
import com.theStuffGatherer.enums.RarityTypes
import com.theStuffGatherer.enums.SiteTypes
import com.theStuffGatherer.enums.resourceTypes.OreTypes
import com.theStuffGatherer.util.getLocationRarity
import com.theStuffGatherer.util.millisecondsToDuration
import com.theStuffGatherer.util.stringToMap
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun getPlayer(id: Snowflake) = transaction { Player.find { PlayersTable.userId eq id.value }.first() }

fun getSites(player: Player) = transaction { Sites.find { SitesTable.userId eq player.id }.first() }

fun getMines(player: Player) = transaction { Mines.find { MinesTable.userId eq player.id }.first() }
suspend fun GuildSlashCommandEvent<NoArgs>.getLocation() {
  respond {
    transaction {

      val player = getPlayer(this@getLocation.author.id)

      title = "You are currently at: `${player.currentLocation}`."

      if (player.currentLocation != "home" && !player.currentLocation.contains("Depleted")) {
        val rarity = getLocationRarity(player.currentLocation)

        val siteType: SiteTypes = when {
          player.currentLocation.contains("Mine") -> SiteTypes.MINE
          else -> {
            field {
              name = "Unknown location."
            }
            return@transaction
          }
        }

        val currentResources = getResourcesFromSite(siteType, rarity, player)
        val totalResources = getResourcesFromSite(siteType, rarity, player, true)

        totalResources.forEach { (k, v) ->
          field {
            name = k.getDisplayName()
            value = "${currentResources[k]}/${v}"
          }
        }
      }

    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.finishActivity() {
  respond {
    transaction {
      val player = getPlayer(this@finishActivity.author.id)
      val timeLeft = player.activityStartTime + player.activityDuration - System.currentTimeMillis()

      if (timeLeft > 0) {
        title = "You can't finish it yet. You are still ${player.currentActivityType.name.lowercase()}. Time remaining: ${millisecondsToDuration(timeLeft)}"
        return@transaction
      }

      when (player.currentActivityType) {

        ActivityTypes.NOTHING -> {
          title = "You can't finish your activity, if you aren't doing anything!"
          return@transaction
        }

        ActivityTypes.EXPLORING -> {

          val duration = player.activityDuration
          val rewards = generateRewards(duration / 60000L)

          if (rewards.isEmpty()) {
            title = "You found nothing while exploring. Better luck next time!"
            return@transaction
          }


          title = "You found the following while exploring:"
          description = "The letters `D` to `S` represent the rarity of the site, while S is the rarest."

          getExploringResults(rewards, player)

        }

        ActivityTypes.TRAVELING -> {
          title = "You have reached your destination!"
          field {
            name = "You are now at the ${player.destination}."
          }
          player.currentLocation = player.destination
          player.destination = ""
        }

        ActivityTypes.MINING -> {
          val mines = getMines(player)
          title = "You finished mining at: `${player.currentLocation}`."
          val resourceMap = mines.getResources(player.currentlyGathering)
          val resourceList = mutableListOf<String>()
          resourceMap.forEach { (oreType, oreAmount) ->
            transaction {
              val inventory = InventoriesTable.select { (InventoriesTable.userId eq player.userId.toLong()) and (InventoriesTable.itemId eq oreType.name.lowercase()) }
              if (inventory.empty())
                InventoriesTable.insert {
                  it[userId] = player.userId.toLong()
                  it[itemId] = oreType.name.lowercase()
                  it[amount] = oreAmount.toUInt()
                }
              else
                InventoriesTable.update({ (InventoriesTable.userId eq player.userId.toLong()) and (InventoriesTable.itemId eq oreType.name.lowercase()) }) {
                  with(SqlExpressionBuilder) {
                    it.update(amount, amount + oreAmount.toUInt())
                  }
                }
            }
            resourceList.add("${oreAmount}x ${oreType.getDisplayName()}")
          }
          field {
            name = "You mined the following resources:"
            value = resourceList.joinToString("\n")
          }
          player.currentlyGathering = ""
        }
      }
      player.activityDuration = 0L
      player.currentActivityType = ActivityTypes.NOTHING
    }
  }
}

suspend fun GuildSlashCommandEvent<NoArgs>.getInventory() {
  val player = getPlayer(this@getInventory.author.id)

  val inventory = transaction { InventoriesTable.select { InventoriesTable.userId eq player.userId.toLong() } }

  respondMenu {
    page {
      title = "Inventory - Ores"
      val entries = mutableListOf<String>()
      transaction {
        inventory.forEach {
          val oreType = OreTypes.getOreTypeFromName(it[InventoriesTable.itemId].uppercase()).getDisplayName()
          entries.add("${it[InventoriesTable.amount]}x $oreType")
        }
      }
      field {
        name = entries.joinToString("\n")
        value = EmbedBuilder.ZERO_WIDTH_SPACE
      }
    }

    buttons {
      button("Left", Emojis.arrowLeft) {
        previousPage()
      }

      button("Right", Emojis.arrowRight) {
        nextPage()
      }
    }
  }
}

fun EmbedBuilder.getExploringResults(rewards: MutableMap<SiteTypes, MutableList<RarityTypes>>, player: Player) {
  val sites = getSites(player)

  for (entry in rewards) {

    val rarities = entry.value.groupingBy { it }.eachCount()
    val formattedRarities = mutableListOf<String>()

    sites.addSiteToDatabase(entry)

    rarities.forEach {
      formattedRarities.add("${it.value}x ${it.key.name}")
    }

    field {
      name = entry.key.name
      value = formattedRarities.joinToString("\n")
    }
  }
}
