package me.marvuun.listeners
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.dsl.listeners
import me.marvuun.logic.acceptQuest
import me.marvuun.logic.openCityBuyingMenu
import me.marvuun.logic.openCityQuestMenu

fun citySelectListeners() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? SelectMenuInteraction ?: return@on

        when (ci.componentId) {
            "cityMenuSelect" -> {
                when (ci.values.first()) {
                    "quests" -> openCityQuestMenu(ci, 0)
                    "buying" -> openCityBuyingMenu(ci)
                }
            }
        }
    }
}

fun cityButtonListeners() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? ButtonInteraction ?: return@on
        when (ci.componentId) {
            "previousCityQuestPage" -> openCityQuestMenu(ci, -1)
            "nextCityQuestPage" -> openCityQuestMenu(ci, 1)
            "acceptQuest" -> {
                acceptQuest(ci)
                openCityQuestMenu(ci, 0)
            }
        }
    }
}
