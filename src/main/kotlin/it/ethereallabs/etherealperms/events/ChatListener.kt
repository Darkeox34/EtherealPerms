package it.ethereallabs.etherealperms.events

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms

/**
 * Listener for handling player chat events and applying formatting.
 */
class ChatListener {

    /**
     * Handles the chat event, formats the message, and broadcasts it.
     */
    fun onPlayerChat(event: PlayerChatEvent) {
            val player = event.sender
            val meta = EtherealPerms.Companion.instance.permissionManager.getChatMeta(player.uuid)

            // Cancel original event to send formatted message
            event.isCancelled = true

            var message = Message.raw("")

            if (meta.prefix.isNotEmpty()) {
                var pMsg = Message.raw(meta.prefix + " ")
                if (!meta.prefixColor.isNullOrEmpty()) {
                    pMsg = pMsg.color(meta.prefixColor)
                }
                applyFormats(pMsg, meta.prefixFormat)
                message = message.insert(pMsg)
            }

            var uMsg = Message.raw(player.username)
            if (!meta.usernameColor.isNullOrEmpty()) {
                uMsg = uMsg.color(meta.usernameColor)
            }
            applyFormats(uMsg, meta.usernameFormat)

            message = message.insert(uMsg)

            if (meta.suffix.isNotEmpty()) {
                var sMsg = Message.raw(" " + meta.suffix)
                if (!meta.suffixColor.isNullOrEmpty()) {
                    sMsg = sMsg.color(meta.suffixColor)
                }
                applyFormats(sMsg, meta.suffixFormat)
                message = message.insert(sMsg)
            }

            var contentMsg = Message.raw(": " + event.content)
            if (!meta.chatColor.isNullOrEmpty()) {
                contentMsg = contentMsg.color(meta.chatColor)
            }
            applyFormats(contentMsg, meta.chatFormat)

            message = message.insert(contentMsg)

            Universe.get().players.forEach{ player->
                player.sendMessage(message)
        }
    }

    private fun applyFormats(msg: Message, formatString: String?) {
        if (formatString.isNullOrEmpty()) return
        val formats = formatString.split(",")
        for (format in formats) {
            when (format.trim().lowercase()) {
                "bold" -> msg.bold(true)
                "italic" -> msg.italic(true)
            }
        }
    }
}