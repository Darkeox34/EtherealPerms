package it.ethereallabs.etherealperms.data

import com.hypixel.hytale.server.core.Message

/**
 * A utility object for creating formatted messages.
 */
object MessageFactory {
    fun info(text: String): Message {
        return Message.raw("[EtherealPerms] ").color("#cb14e3").insert(Message.raw(text).color("#b6c926"))
    }

    fun success(text: String): Message {
        return Message.raw("[EtherealPerms] ").color("#cb14e3").insert(Message.raw(text).color("#32a852"))
    }

    fun error(text: String): Message {
        return Message.raw("[EtherealPerms] ").color("#cb14e3").insert(Message.raw(text).color("#a30321"))
    }

}