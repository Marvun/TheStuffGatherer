package me.marvuun.commands

import me.jakejmattson.discordkt.commands.commands
import me.marvuun.logic.showHelp

fun utilityCommands() = commands("Utility") {
    slash("help", "Shows you the help to all the commands.") {
        execute {
            showHelp(interaction!!, discord, 0)
        }
    }
}
