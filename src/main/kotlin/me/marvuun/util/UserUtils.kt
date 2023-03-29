package me.marvuun.util

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.rest.builder.message.create.embed

suspend fun checkUser(ci: ActionInteraction, user2: User) = if (ci.user != user2) {
    ci.respondEphemeral {
        embed {
            title = "You are not allowed to do that."
        }
    }
    false
} else {
    true
}
