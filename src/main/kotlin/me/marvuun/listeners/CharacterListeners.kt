package me.marvuun.listeners

import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.dsl.listeners
import me.marvuun.logic.*

private val selectedGatheringResources = mutableMapOf<String, Int>()
private val existingGatheringResources = mutableListOf<String>()
private var modalOutputCount = 0
fun characterButtonListeners() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? ButtonInteraction ?: return@on
        when (ci.componentId) {
            "previousPlayerQuestPage" -> openPlayerQuestMenu(ci, -1)
            "nextPlayerQuestPage" -> openPlayerQuestMenu(ci, 1)
            "previousInventoryPage" -> openInventoryMenu(ci, -1)
            "nextInventoryPage" -> openInventoryMenu(ci, 1)
            "finishQuest" -> {
                finishQuest(ci)
                openPlayerQuestMenu(ci, 0)
            }
            "openGatheringMenu" -> openGatheringMenu(ci)
        }
    }
}

fun characterSelectListeners() = listeners {
    on<InteractionCreateEvent> {
        val ci = interaction as? SelectMenuInteraction ?: return@on
        when (ci.componentId) {
            "gatherSelectResource" -> {
                ci.values.forEach {
                    existingGatheringResources.add(it)
                }
                askGatheringAmount(ci, existingGatheringResources)
            }
            "abandonSelectSite" -> abandonSite(ci, ci.values.first())
        }
    }
}

fun characterModalListeners() = listeners {
    on<InteractionCreateEvent> {
        val msi = interaction as? ModalSubmitInteraction ?: return@on
        when (msi.modalId) {
            "askGatheringAmount" -> {
                val site = getSite(msi.user)
                val resources = site.currentResources.toMutableMap()
                var isValid = true
                msi.textInputs.forEach { (_, textInput) ->
                    val modalOutput = textInput.value!!
                    val maxAmount = resources[existingGatheringResources.first()]!!
                    if (modalOutput.toIntOrNull() == null || modalOutput.toInt() > maxAmount || modalOutput.toInt() < 1) {
                        isValid = false
                        return@forEach
                    } else {
                        selectedGatheringResources[existingGatheringResources.first()] = modalOutput.toInt()
                        existingGatheringResources.removeAt(0)
                    }
                }
                if (!isValid) {
                    existingGatheringResources.clear()
                    openGatheringMenu(msi)
                } else {
                    startGathering(msi, selectedGatheringResources)
                }
            }
        }
    }
}
