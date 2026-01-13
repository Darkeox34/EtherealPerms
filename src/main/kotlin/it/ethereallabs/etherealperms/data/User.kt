package it.ethereallabs.etherealperms.data

import java.util.UUID

data class User(
    val uuid: UUID,
    var username: String,
    override val nodes: MutableSet<Node> = mutableSetOf()
) : PermissionSubject