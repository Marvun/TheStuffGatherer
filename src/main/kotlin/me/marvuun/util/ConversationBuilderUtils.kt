package me.marvuun.util

import dev.kord.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.arguments.Argument
import me.jakejmattson.discordkt.conversations.ConversationBuilder

suspend fun <T> ConversationBuilder.promptUntilAsEmbed(
    argument: Argument<*, T>,
    error: String,
    isValid: (T) -> Boolean,
    embed: EmbedBuilder.() -> Unit,
): T {
    var value: T = prompt(argument, embed = embed)

    while (!isValid.invoke(value)) {
        respond {
            title = error
        }
        value = prompt(argument, embed = embed)
    }

    return value
}
