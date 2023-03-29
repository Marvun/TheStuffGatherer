package me.marvuun.logic

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.GuildApplicationCommandInteraction
import dev.kord.rest.builder.message.create.MessageCreateBuilder

class MyMenu {
    private val pages = mutableListOf<MessageCreateBuilder.() -> Unit>()
    var defaultPageIndex = 0
        set(value) {
            pageIndex = value
            field = value
        }

    private var pageIndex = defaultPageIndex

    fun navigate(i: Int) {
        pageIndex += i
        pageIndex = when {
            pageIndex > pages.lastIndex -> 0
            pageIndex < 0 -> pages.lastIndex
            else -> pageIndex
        }
    }

    fun getPage() = pages[pageIndex]

    fun page(construct: MessageCreateBuilder.() -> Unit) {
        pages.add(construct)
    }

    suspend fun respond(ai: ActionInteraction, page: Int) {
        defaultPageIndex = if (ai is ComponentInteraction) {
            (ai.message.embeds[0].footer!!.text.split(" ").last().split("/").first().toIntOrNull() ?: 1) - 1
        } else {
            0
        }
        navigate(page)
        if (ai is GuildApplicationCommandInteraction) {
            ai.respondPublic(getPage())
        } else {
            ai as ComponentInteraction
            ai.updatePublicMessage(getPage())
        }
    }
}

suspend fun myMenu(menuBuilder: suspend MyMenu.() -> Unit): MyMenu {
    val menu = MyMenu()
    menu.menuBuilder()
    return menu
}
