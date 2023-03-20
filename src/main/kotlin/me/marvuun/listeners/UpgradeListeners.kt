package me.marvuun.listeners

import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.dsl.listeners
import me.marvuun.enums.StationTypes
import me.marvuun.enums.getStationType
import me.marvuun.logic.*

private var selectedStation: StationTypes? = null
fun upgradeButtonListeners() = listeners {
  on<InteractionCreateEvent> {
    val ci = interaction as? ComponentInteraction ?: return@on // Component == Button, in this case
    when (ci.componentId) {
      "homeButton" -> {
        ci.deferPublicMessageUpdate()
        openHomeUpgradeMenu(ci)
      }

      "toolsButton" -> println("A 'test2' button was pressed.") // TODO: Ditto

      "doStationUpgrade" -> {
        ci.deferPublicMessageUpdate()
        performStationUpgrade(ci, selectedStation)
      }

      "cancelStationUpgrade" -> {
        cancelStationUpgrade(ci, selectedStation)
      }
    }
  }
}

fun upgradeSelectListeners() = listeners {
  on<InteractionCreateEvent> {
    val ci = interaction as? SelectMenuInteraction ?: return@on
    when (ci.componentId) {
      "stationMenu" -> {
        ci.deferPublicMessageUpdate()
        selectedStation = openStationUpgrade(ci.values.first().getStationType(), ci)
      }
    }
  }
}
