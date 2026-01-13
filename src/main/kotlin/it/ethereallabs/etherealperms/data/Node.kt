package it.ethereallabs.etherealperms.data

data class Node(
    val key: String,
    val value: Boolean = true,
    val expiry: Long? = null,
    val context: Map<String, String> = emptyMap()
)