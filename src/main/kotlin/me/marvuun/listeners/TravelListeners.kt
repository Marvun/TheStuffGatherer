package me.marvuun.listeners

import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.dsl.listeners
import me.marvuun.logic.openTravelCategoryMenu
import me.marvuun.logic.openTravelCityMenu
import me.marvuun.logic.openTravelSiteMenu

fun travelListeners() = listeners {
  on<InteractionCreateEvent> {
    val ci = interaction as? SelectMenuInteraction ?: return@on
    when (ci.componentId) {
      "travelMenu" -> openTravelCategoryMenu(ci, ci.values.first())

      "travelSiteMenu" -> openTravelSiteMenu(ci, ci.values.first())

      "travelCityMenu" -> openTravelCityMenu(ci, ci.values.first())
    }
  }
}