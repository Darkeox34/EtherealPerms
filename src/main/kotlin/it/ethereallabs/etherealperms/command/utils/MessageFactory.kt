package it.ethereallabs.etherealperms.command.utils

import com.hypixel.hytale.server.core.Message
import java.awt.Color

/**
 * A utility object for creating formatted messages.
 */
object MessageFactory {
    /**
     * Creates an informational message.
     */
    fun info(text: String): Message {
        return Message.raw("[EtherealPerms] ").color(Color.GREEN).insert(Message.raw(text).color(Color.YELLOW))
    }

    /**
     * Creates a success message.
     */
    fun success(text: String): Message {
        return Message.raw("[EtherealPerms] ").color(Color.GREEN).insert(Message.raw(text).color(Color.CYAN))
    }

    /**
     * Creates an error message.
     */
    fun error(text: String): Message {
        return Message.raw("[EtherealPerms] ").color(Color.GREEN).insert(Message.raw(text).color(Color.RED))
    }

}