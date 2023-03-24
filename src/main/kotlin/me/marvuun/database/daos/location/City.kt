package me.marvuun.database.daos.location

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.GuildApplicationCommandInteraction
import dev.kord.rest.builder.message.create.embed
import me.marvuun.database.daos.Buyer
import me.marvuun.database.daos.Buyer.Companion.transform
import me.marvuun.database.daos.Inventory
import me.marvuun.database.daos.resources.RawResource
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.database.daos.resources.getResourcesDisplayName
import me.marvuun.database.tables.locations.Cities
import me.marvuun.database.tables.locations.Sites
import me.marvuun.logic.getCities
import me.marvuun.logic.getInventory
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.abs
import java.util.*
import kotlin.math.sqrt

class City(id: EntityID<UUID>) : Location(id) {
  companion object : EntityClass<UUID, City>(Cities)

  override var xCoordinate by Cities.xCoordinate
  override var yCoordinate by Cities.yCoordinate
  var name by Cities.name
  var cityId by Cities.id
  var userId by Cities.userId
  var travelTime by Cities.travelTime
  var buyers by Cities.buyers.transformBuyers()
  var purchasableItems by Cities.purchasableItems.transformResources()

  suspend fun checkCoordinates(user: User, interaction: GuildApplicationCommandInteraction) {
    val inventory = getInventory(user)
    val site = transaction { Site.find { (Sites.xCoordinate eq xCoordinate) and (Sites.yCoordinate eq yCoordinate) and (Sites.userId eq userId)}.firstOrNull() } ?: return
    transaction {
      site.currentResources.forEach { (short, amount) ->
        val invEntry = inventory.find { it.itemId == short && it.userId == userId }
        if (invEntry == null)
          Inventory.new {
            userId = this@City.userId
            itemId = short
            this.amount = (getResourceFromShort(short) as RawResource).calculateResourceAmount(user) * amount
          }
        else
          invEntry.amount = invEntry.amount + (getResourceFromShort(short) as RawResource).calculateResourceAmount(user) * amount
      }
    }

    interaction.respondEphemeral {
      embed {
        title = "Oh?"
        description = "The place where the city $name should be located was a ${site.type.name.lowercase()} that you discovered.\nThe site will be removed, but as a compensation you will get the resources anyway.\n\nYou've got the following resources:\n${getResourcesDisplayName(site.currentResources, "\n")}"
      }
    }

    transaction { site.delete() }

  }
}

private fun Column<String>.transformBuyers() = transform({
  it.map { entry -> entry.buyerId.value }.joinToString(",")
}, {
  val buyers = mutableListOf<Buyer>()
  it.split(",").forEach {entry ->
    buyers.add(Buyer.findById(UUID.fromString(entry))!!)
  }
  buyers
})

