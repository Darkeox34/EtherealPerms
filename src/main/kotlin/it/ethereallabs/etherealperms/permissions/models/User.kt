package it.ethereallabs.etherealperms.permissions.models

import it.ethereallabs.etherealperms.data.models.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    var username: String,
    override val nodes: MutableSet<Node> = mutableSetOf()
) : PermissionSubject