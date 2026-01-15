package it.ethereallabs.etherealperms.events

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.ColorHelper
import it.ethereallabs.etherealperms.permissions.models.ChatMeta

/**
 * Listener for handling player chat events and applying formatting.
 */
class ChatListener {

    fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.sender
        val perms = EtherealPerms.permissionManager
        val storage = EtherealPerms.storage

        val meta = perms.getChatMeta(player.uuid)
        val group = perms.getUserPrimaryGroup(player.uuid)

        event.isCancelled = true

        val chatConfig = storage.getConfigs()

        val template = chatConfig.groupFormats[group?.name ?: "default"]
            ?: chatConfig.format

        val context = ChatContext(
            username = player.username,
            displayName = player.username,
            prefix = meta.prefix,
            suffix = meta.suffix,
            group = group?.name ?: "",
            message = event.content
        )

        val formatted = formatChat(template, context)
        val colored = ColorHelper.translateMessageColors(formatted)

        Universe.get().players.forEach {
            it.sendMessage(colored)
        }
    }


    private val PLACEHOLDERS = mapOf(
        "{MESSAGE}" to { ctx: ChatContext -> ctx.message },
        "{USERNAME}" to { ctx: ChatContext -> ctx.username },
        "{DISPLAYNAME}" to { ctx: ChatContext -> ctx.displayName },
        "{PREFIX}" to { ctx: ChatContext -> ctx.prefix },
        "{SUFFIX}" to { ctx: ChatContext -> ctx.suffix },
        "{GROUP}" to { ctx: ChatContext -> ctx.group }
    )

    data class ChatContext(
        val username: String,
        val displayName: String,
        val prefix: String,
        val suffix: String,
        val group: String,
        val message: String
    )

    fun formatChat(template: String, context: ChatContext): String {
        var result = template

        for ((placeholder, supplier) in PLACEHOLDERS) {
            result = result.replace(placeholder, supplier(context))
        }

        return result
    }
}