package me.marvuun.listeners

import dev.kord.common.entity.MessageType
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import me.jakejmattson.discordkt.dsl.listeners

fun listeners() = listeners {
  on<MessageCreateEvent> {
    if (this.message.type == MessageType.UserJoin && this.message.author!!.id.value == 1051460910864158770UL) {

      this.message.channel.createEmbed {
        title = "Hi, thank you for inviting me!"
        description = "To start your journey use `/start`."
      }
    }

  }
}


