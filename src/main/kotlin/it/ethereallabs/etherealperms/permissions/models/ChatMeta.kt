package it.ethereallabs.etherealperms.permissions.models

/**
 * Data class holding chat formatting information.
 */
data class ChatMeta(
    val prefix: String,
    val prefixColor: String?,
    val prefixFormat: String?,
    val suffix: String,
    val suffixColor: String?,
    val suffixFormat: String?,
    val usernameColor: String?,
    val usernameFormat: String?,
    val chatColor: String?,
    val chatFormat: String?
)