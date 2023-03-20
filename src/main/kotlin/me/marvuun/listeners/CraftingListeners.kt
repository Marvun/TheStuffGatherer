package me.marvuun.listeners

import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import me.jakejmattson.discordkt.dsl.listeners
import me.marvuun.database.daos.resources.FurnaceRecipe
import me.marvuun.database.daos.resources.Resource
import me.marvuun.database.daos.resources.getResourceFromShort
import me.marvuun.enums.getStationType
import me.marvuun.logic.*

private val ingredients = mutableListOf<Resource<String>?>(null, null)
private var amount = 1
private var maxAmount = 0
fun craftingSelectListeners() = listeners {
  on<InteractionCreateEvent> {
    val ci = interaction as? SelectMenuInteraction ?: return@on
    when (ci.componentId) {

      "craftingStationMenu" -> {
        openCraftingMenu(ci, ci.values.first().getStationType())
      }

      "furnaceRecipes" -> {
        ingredients[0] = getResourceFromShort(ci.values.first())
        updateCraftingMenu(ingredients, ci, needsHeat = true)
      }

      "furnaceFuel" -> {
        ingredients[1] = getResourceFromShort(ci.values.first())
        updateCraftingMenu(ingredients, ci, needsHeat = true)

      }

      "sawmillRecipes", "stonecutterRecipes" -> {
        ingredients[0] = getResourceFromShort(ci.values.first())
        updateCraftingMenu(ingredients, ci)
      }


    }
  }
}

fun craftingButtonListeners() = listeners {
  on<InteractionCreateEvent> {
    val ci = interaction as? ComponentInteraction ?: return@on
    when (ci.componentId) {
      "changeRecipeAmount" -> {
        maxAmount = askForAmount(ci, ingredients)
      }
      "confirmRecipe" -> {
        startCrafting(ci, ingredients, amount)
      }
    }


  }
}


fun craftingModalListeners() = listeners {
  on<InteractionCreateEvent> {
    val msi = interaction as? ModalSubmitInteraction ?: return@on
    when (msi.modalId) {
      "amountModal" -> {
        val modalOutput = msi.textInputs["amountTextInput"]!!.value!!
        if (modalOutput.toIntOrNull() == null || modalOutput.toInt() > maxAmount || modalOutput.toInt() < 1) {
          if (ingredients[0] is FurnaceRecipe)
            updateCraftingMenu(ingredients, msi, 1, false, needsHeat = true)
          else
            updateCraftingMenu(ingredients, msi, 1, false)
        }
        else {
          if (ingredients[0] is FurnaceRecipe)
            updateCraftingMenu(ingredients, msi, modalOutput.toInt(), needsHeat = true)
          else
            updateCraftingMenu(ingredients, msi, modalOutput.toInt())
          amount = modalOutput.toInt()
        }

      }

    }
  }
}