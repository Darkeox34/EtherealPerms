package it.ethereallabs.etherealperms.permissions.models

data class Node(
    val key: String,
    val value: Boolean = true,
    val expiry: Long? = null,
    val context: Map<String, String> = emptyMap()
)