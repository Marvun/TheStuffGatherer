package me.marvuun.logic

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.create.UpdateMessageInteractionResponseCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.extensions.toPartialEmoji
import me.marvuun.database.daos.Inventory
import me.marvuun.database.daos.Player
import me.marvuun.database.daos.activities.CurrentPlayerActivity
import me.marvuun.database.daos.location.Home
import me.marvuun.database.daos.resources.*
import me.marvuun.database.daos.stations.getNextStation
import me.marvuun.database.tables.Inventories
import me.marvuun.enums.ActivityTypes
import me.marvuun.enums.ResourceCategories
import me.marvuun.enums.StationTypes
import me.marvuun.enums.getStationTypeFromResource
import me.marvuun.util.checkUser
import me.marvuun.util.millisecondsToDuration
import me.marvuun.util.times
import me.marvuun.util.toMyString
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

private var commandInvoker: User? = null

suspend fun GuildSlashCommandEvent<NoArgs>.openCraftingMenu() {
    commandInvoker = author
    val home = getHome(author)

    if (home.furnaceLevel == 0 && home.sawmillLevel == 0 && home.stoneCutterLevel == 0) {
        interaction!!.respondEphemeral {
            embed {
                title = "You don't have any working stations yet.\nCheck out `/upgrade` to upgrade them!"
            }
        }
    } else {
        interaction!!.respondPublic {
            embed {
                title = "At which station do you want to craft?"
            }
            actionRow {
                stringSelect("craftingStationMenu") {
                    if (home.furnaceLevel > 0) {
                        option("Furnace", "furnace") {
                            description = "Used to process ores, nugget and ingots."
                        }
                    }
                    if (home.sawmillLevel > 0) {
                        option("Sawmill", "sawmill") {
                            description = "Used to process logs and planks."
                        }
                    }
                    if (home.stoneCutterLevel > 0) {
                        option("Stone Cutter", "stonecutter") {
                            description = "Used to process stones and blocks."
                        }
                    }
                }
            }
        }
    }
}

suspend fun openCraftingMenu(ci: ComponentInteraction, stationType: StationTypes) {
    if (!checkUser(ci, commandInvoker!!)) return
    val home = getHome(ci.user)
    val inventory = transaction { getInventory(ci.user).toList() }

    val availableRecipes = getAvailableRecipes(null, stationType, home)
    ci.updatePublicMessage {
        embed {
            title = "${stationType.getDisplayName()} Level ${getNextStation(stationType, home)!!.level.value - 1}"
            description = "Select what you want to craft!"
        }
        actionRow {
            stringSelect("${stationType.name.lowercase()}Recipes") {
                availableRecipes.forEach {
                    option(it.name.value, it.short) {
                        description =
                            "You currently have: ${inventory.find { invEntry -> invEntry.itemId == it.short }?.amount ?: "0"}"
                    }
                }
            }
        }
        actionRow {
            interactionButton(ButtonStyle.Secondary, "cancelCraftingMenu") {
                label = "Cancel"
            }
        }
    }
}

suspend fun cancelCraftingMenu(ci: ComponentInteraction) {
    ci.updatePublicMessage {
        embed {
            title = "Crafting canceled!"
        }
    }
}

suspend fun askForAmount(ci: ComponentInteraction, ingredients: MutableList<Resource<String>?>): Int {
    if (!checkUser(ci, commandInvoker!!)) return 1
    val inventory = transaction { getInventory(ci.user).toList() }
    val resource = ingredients[0] as Recipe<*>
    val fuel = ingredients[1] as FuelResource?
    val maxPossibleCrafts = mutableListOf<Int>()

    resource.neededResources.forEach { (short, amount) ->
        val maxCrafts = inventory.find { it.itemId == short }!!.amount / amount
        maxPossibleCrafts.add(maxCrafts)
    }

    if (fuel != null) {
        maxPossibleCrafts.add(inventory.find { it.itemId == fuel.short }!!.amount / 5)
        maxPossibleCrafts.sort()
    }

    ci.modal(resource.name.value, "amountModal") {
        actionRow {
            textInput(TextInputStyle.Short, "amountTextInput", "Amount") {
                placeholder = "Maximum is: ${maxPossibleCrafts.first()}"
            }
        }
    }
    return maxPossibleCrafts.first()
}

suspend fun startCrafting(ci: ComponentInteraction, ingredients: MutableList<Resource<String>?>, amount: Int) {
    if (!checkUser(ci, commandInvoker!!)) return
    val player = getPlayer(ci.user)
    val inventory = transaction { getInventory(ci.user).toList() }
    val resource = ingredients[0] as Recipe<*>
    val fuel = ingredients[1] as FuelResource?
    val guildId = ci.message.getGuild().id.value
    val startTime = System.currentTimeMillis()

    transaction {
        resource.outputAmount.mapValues { entry -> entry.value * amount }
        player.activityStartTime = startTime
        player.currentActivity =
            "Crafting ${resource.outputAmount[resource.short]!!.toMyString(amount, "-")}x ${resource.name.value}."
        player.currentActivityType = ActivityTypes.CRAFTING
        player.activityDuration = resource.craftingDuration * amount
        player.currentlyMaking = resource.outputAmount

        CurrentPlayerActivity.new {
            this.guildId = guildId
            channelId = ci.channelId.value
            userId = ci.user.id.value
            activityEnd = startTime + resource.craftingDuration * amount
        }

        var invEntries =
            inventory.filter { resource.neededResources.keys.contains(it.itemId) && it.userId == ci.user.id.value }
        invEntries.forEach {
            it.amount = it.amount - resource.neededResources[it.itemId]!! * amount
        }

        if (fuel != null) {
            invEntries =
                inventory.filter { fuel.short == it.itemId && it.userId == ci.user.id.value }
            invEntries.forEach {
                it.amount = it.amount - amount * 5
            }
        }
    }

    ci.updatePublicMessage {
        components = mutableListOf()
        embed {
            title = "You started to craft ${
                resource.outputAmount[resource.short]!!.toMyString(
                    amount,
                    "-",
                )
            }x ${resource.name.value}."
            description = "It will take ${millisecondsToDuration(resource.craftingDuration * amount)}."
        }
    }
}

suspend fun updateCraftingMenu(
    ingredients: MutableList<Resource<String>?>,
    ci: ActionInteraction,
    amount: Int = 1,
    validAmount: Boolean = true,
    needsHeat: Boolean = false,
) {
    if (!checkUser(ci, commandInvoker!!)) return
    val home = getHome(ci.user)
    val inventory = transaction { getInventory(ci.user).toList() }
    val message = buildCraftingMessage(ingredients, inventory, home, amount, validAmount, needsHeat)

    if (ci is ComponentInteractionBehavior) {
        ci.updatePublicMessage {
            embeds = message.embeds
            components = message.components
        }
    }
}

private fun buildCraftingMessage(
    ingredients: MutableList<Resource<String>?>,
    inventory: List<Inventory>,
    home: Home,
    amount: Int,
    validAmount: Boolean,
    needsHeat: Boolean,
): UpdateMessageInteractionResponseCreateBuilder {
    val resource = ingredients[0] as Recipe<*>
    val stationType = getStationTypeFromResource(resource)
    val fuel = if (needsHeat) ingredients[1] as FuelResource? else null
    val missingMaterials = resource.getMissingResources(inventory)
    val availableRecipes = getAvailableRecipes(resource, null, home)

    val availableFuel = transaction { inventory.filter { it.type == ResourceCategories.FUEL } }

    return UpdateMessageInteractionResponseCreateBuilder().apply {
        embed {
            title = "${resource.outputAmount[resource.short]!!.toMyString(amount, "-")}x ${resource.name.value}"
            field {
                name = "Ingredients"
                value = "${
                    getResourcesDisplayName(
                        resource.neededResources,
                        "\n",
                        amount,
                    )
                }${
                    if (needsHeat) {
                        "\n${amount * 5}x ${fuel?.name?.value ?: "Fuel"}"
                    } else {
                        ""
                    }
                }"
            }
            if (needsHeat) {
                field {
                    name = "Required Heat Level: <=${(resource as FurnaceRecipe).requiredHeatLevel}"
                }
                if (availableFuel.isEmpty()) {
                    field {
                        name = "You don't have any usable fuel!"
                    }
                }
            }
            if (!validAmount) {
                field {
                    name = "Invalid amount! Please try again."
                }
            }
            if (missingMaterials.isNotEmpty()) {
                field {
                    name = "You don't have enough materials!"
                    value = "You are missing the following materials:\n$missingMaterials"
                }
            }
        }
        actionRow {
            stringSelect("${stationType.name.lowercase()}Recipes") {
                availableRecipes.forEach {
                    option(it.name.value, it.short) {
                        description =
                            "You currently have: ${inventory.find { invEntry -> invEntry.itemId == it.short }?.amount ?: "0"}"
                        if (it.short == resource.short) default = true
                    }
                }
            }
        }
        if (needsHeat && availableFuel.isNotEmpty()) {
            actionRow {
                stringSelect("furnaceFuel") {
                    placeholder = "Please select a fuel"
                    availableFuel.forEach {
                        val fuelResource = getResourceFromShort(it.itemId) as FuelResource
                        option(fuelResource.name.value, it.itemId) {
                            description = "Available: ${it.amount}, Heat Level: ${fuelResource.heatLevel}"
                            if (fuel?.short == it.itemId) default = true
                        }
                    }
                }
            }
            if (fuel != null && missingMaterials.isEmpty()) {
                actionRow {
                    interactionButton(ButtonStyle.Secondary, "confirmRecipe") {
                        label = "Confirm"
                        emoji = Emojis.whiteCheckMark.toPartialEmoji()
                    }
                    interactionButton(ButtonStyle.Secondary, "changeRecipeAmount") {
                        label = "Change Amount"
                    }
                }
            }
        } else if (!needsHeat && missingMaterials.isEmpty()) {
            actionRow {
                interactionButton(ButtonStyle.Secondary, "confirmRecipe") {
                    label = "Confirm"
                    emoji = Emojis.whiteCheckMark.toPartialEmoji()
                }
                interactionButton(ButtonStyle.Secondary, "changeRecipeAmount") {
                    label = "Change Amount"
                }
            }
        }
    }
}

suspend fun finishCrafting(player: Player, user: User, channel: MessageChannel) {
    val resources = player.currentlyMaking
    val levels = getLevels(user)
    val finalResources = mutableMapOf<String, Int>()
    newSuspendedTransaction {
        resources.forEach { (short, amount) ->
            val randomAmount = amount.random()
            val invEntries =
                Inventory.find { (Inventories.userId eq user.id.value) and (Inventories.itemId eq short) }.toList()

            if (invEntries.isEmpty()) {
                Inventory.new {
                    userId = user.id.value
                    itemId = short
                    this.amount = randomAmount
                }
            } else {
                val inventory = invEntries.first()
                inventory.amount = inventory.amount + randomAmount
            }
            finalResources[short] = randomAmount

            val resource = getResourceFromShort(short)
            if (resource is Recipe<*>) {
                val craftingTimes = amount.first / resource.outputAmount[resource.short]!!.first
                levels.addExperience(resource, craftingTimes, user, channel)
            }
        }
        player.currentlyMaking = mutableMapOf()
    }

    channel.createMessage {
        content = user.mention
        embed {
            title = "You finished crafting."
            field {
                name = "You crafted the following resources:"
                value = getResourcesDisplayName(finalResources, "\n")
            }
        }
    }
}
