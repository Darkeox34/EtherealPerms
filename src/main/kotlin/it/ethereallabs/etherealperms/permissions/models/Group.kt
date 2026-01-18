package it.ethereallabs.etherealperms.permissions.models

import kotlinx.serialization.Serializable

interface PermissionSubject {
    val nodes: MutableSet<Node>
}

@Serializable
data class Group(
    @Transient
    val name: String = "",
    var weight: Int = 0,
    override val nodes: MutableSet<Node> = mutableSetOf()
) : PermissionSubject