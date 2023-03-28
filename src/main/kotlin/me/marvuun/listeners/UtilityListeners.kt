package me.marvuun.listeners

import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.dsl.listeners
import me.marvuun.logic.showHelp

fun utilityButtonListeners() = listeners {
  on<InteractionCreateEvent> {
    val ci = interaction as? ButtonInteraction ?: return@on
    when (ci.componentId) {
      "previousHelpPage" -> showHelp(ci, discord, -1)
      "nextHelpPage" -> showHelp(ci, discord, 1)
    }
  }
}