package it.ethereallabs.etherealperms.events

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.ColorHelper

class ChatListener {

    fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.sender
        val perms = EtherealPerms.permissionManager
        val storage = EtherealPerms.storage

        if (!perms.hasPermission(player.uuid, "etherealperms.chatcolor")) {
            val stripped = stripColors(event.content)
            event.content = stripped
        }

        val meta = perms.getChatMeta(player.uuid)
        val group = perms.getUserPrimaryGroup(player.uuid)
        val chatConfig = storage.getConfigs()

        val template = chatConfig.groupFormats[group?.name ?: "default"]
            ?: chatConfig.format

        event.setFormatter { playerRef, content ->
            var formatted = template
                .replace("{USERNAME}", playerRef.username)
                .replace("{DISPLAYNAME}", playerRef.username)
                .replace("{PREFIX}", meta.prefix)
                .replace("{SUFFIX}", meta.suffix)
                .replace("{GROUP}", group?.name ?: "")
                .replace("{MESSAGE}", content)

            ColorHelper.translateMessageColors(formatted)
        }
    }

    private fun stripColors(input: String): String {
        val pattern = Regex("<([^>]+)>|&([0-9a-fA-Fklmnor])")
        return input.replace(pattern, "")
    }
}