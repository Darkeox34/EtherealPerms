package it.ethereallabs.etherealperms.permissions

import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider
import java.util.UUID

/**
 * Implementation of PermissionProvider to integrate with Hytale's permission system.
 */
class EtherealPermissionProvider(private val permissionManager: PermissionManager) : PermissionProvider {
    override fun getName(): String = "EtherealPerms"

    override fun getUserPermissions(uuid: UUID): Set<String> {
        val user = permissionManager.getUser(uuid)
        if (user == null) {
            return emptySet()
        }

        // Retrieves all calculated permissions (including groups and inheritance).
        val effective = permissionManager.getEffectivePermissions(user)
        val result = effective.map { (key, value) ->
            val cleanKey = if (key.endsWith(".*")) key.dropLast(2) else key
            if (value) cleanKey else "-$cleanKey"
        }.toSet()
        return result
    }

    // Returns user groups for compatibility with other plugins.
    override fun getGroupsForUser(uuid: UUID): Set<String> {
        val user = permissionManager.getUser(uuid) ?: return emptySet()
        val groups = user.nodes
            .filter { it.key.startsWith("group.") }
            .map { it.key.substring(6) }
            .toSet()
        return groups
    }

    override fun getGroupPermissions(group: String): Set<String> = emptySet()

    // Modification methods left empty because everything is handled via plugin commands.
    override fun addUserPermissions(uuid: UUID, permissions: Set<String>) {}
    override fun removeUserPermissions(uuid: UUID, permissions: Set<String>) {}
    override fun addGroupPermissions(group: String, permissions: Set<String>) {}
    override fun removeGroupPermissions(group: String, permissions: Set<String>) {}
    override fun addUserToGroup(uuid: UUID, group: String) {}
    override fun removeUserFromGroup(uuid: UUID, group: String) {}
}