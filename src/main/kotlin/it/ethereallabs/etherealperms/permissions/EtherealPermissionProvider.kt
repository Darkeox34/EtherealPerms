package it.ethereallabs.etherealperms.permissions

import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider
import it.ethereallabs.etherealperms.EtherealPerms
import java.util.UUID

/**
 * Implementation of PermissionProvider to integrate with Hytale's permission system.
 */
class EtherealPermissionProvider(private val permissionManager: PermissionManager) : PermissionProvider {
    override fun getName(): String = "EtherealPerms"

    override fun getUserPermissions(uuid: UUID): Set<String> {
        val user = permissionManager.getUser(uuid) ?: return emptySet()

        val permissions =  permissionManager.getEffectivePermissions(user)
            .filter { it.value }
            .map { it.key }
            .toSet()

        return permissions
    }

    override fun getGroupsForUser(uuid: UUID): Set<String> {
        val user = permissionManager.getUser(uuid) ?: return emptySet()
        val groups = user.nodes
            .filter { it.key.startsWith("group.") }
            .filter{it.value}
            .map { it.key.substring(6) }
            .toSet()
        return groups
    }

    override fun getGroupPermissions(group: String): Set<String> = emptySet()

    override fun addUserPermissions(uuid: UUID, permissions: Set<String>) {}
    override fun removeUserPermissions(uuid: UUID, permissions: Set<String>) {}
    override fun addGroupPermissions(group: String, permissions: Set<String>) {}
    override fun removeGroupPermissions(group: String, permissions: Set<String>) {}
    override fun addUserToGroup(uuid: UUID, group: String) {}
    override fun removeUserFromGroup(uuid: UUID, group: String) {}
}