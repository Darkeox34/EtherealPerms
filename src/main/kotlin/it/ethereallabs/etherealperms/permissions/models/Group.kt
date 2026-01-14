package it.ethereallabs.etherealperms.permissions.models

interface PermissionSubject {
    val nodes: MutableSet<Node>
}

data class Group(
    val name: String,
    var weight: Int = 0,
    override val nodes: MutableSet<Node> = mutableSetOf()
) : PermissionSubject