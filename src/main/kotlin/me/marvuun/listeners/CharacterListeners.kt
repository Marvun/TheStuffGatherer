package me.marvuun.listeners

import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.dsl.listeners
import me.marvuun.logic.finishQuest
import me.marvuun.logic.openPlayerQuestMenu

fun characterListeners() = listeners {
  on<InteractionCreateEvent> {
    val ci = interaction as? ButtonInteraction ?: return@on
    when (ci.componentId) {
      "previousPlayerQuestPage" -> openPlayerQuestMenu(ci, -1)
      "nextPlayerQuestPage" -> openPlayerQuestMenu(ci, 1)
      "finishQuest" -> {
        finishQuest(ci)
        openPlayerQuestMenu(ci, 0)
      }
    }
  }
}