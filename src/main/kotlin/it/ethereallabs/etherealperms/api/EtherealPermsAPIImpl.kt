package it.ethereallabs.etherealperms.api

import com.hypixel.hytale.server.core.Message
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.ColorHelper
import it.ethereallabs.etherealperms.permissions.PermissionManager
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch
import java.util.UUID

class EtherealPermsAPIImpl(private val permissionManager: PermissionManager) : EtherealPermsAPI {

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        val user = permissionManager.getUser(uuid) ?: return false
        val perms = permissionManager.getEffectivePermissions(user)
        return perms[permission.lowercase()] == true
    }

    override fun getUserPrefix(uuid: UUID): String {
        return permissionManager.getChatMeta(uuid).prefix
    }

    override fun getUserSuffix(uuid: UUID): String {
        return permissionManager.getChatMeta(uuid).suffix
    }

    override fun getUserPrimaryGroup(uuid: UUID): Group? {
        return permissionManager.getUserPrimaryGroup(uuid)
    }

    override fun getUserGroups(uuid: UUID): List<Group> {
        val user = permissionManager.getUser(uuid) ?: return emptyList()
        return user.nodes
            .filter { it.key.startsWith("group.", ignoreCase = true) }
            .mapNotNull { permissionManager.getGroup(it.key.substring(6)) }
    }

    override fun isPlayerInGroup(uuid: UUID, group: String): Boolean {
        val user = permissionManager.getUser(uuid) ?: return false
        return user.nodes.any { it.key.equals("group.$group", ignoreCase = true) }
    }

    override fun addUserPermission(uuid: UUID, permission: String) {
        val user = permissionManager.getUser(uuid) ?: return
        if (user.nodes.add(Node(permission))) {
            saveAsync()
        }
    }

    override fun removeUserPermission(uuid: UUID, permission: String) {
        val user = permissionManager.getUser(uuid) ?: return
        if (user.nodes.removeIf { it.key.equals(permission, ignoreCase = true) }) {
            saveAsync()
        }
    }

    override fun addGroupPermission(groupName: String, permission: String) {
        val group = permissionManager.getGroup(groupName) ?: return
        if (group.nodes.add(Node(permission))) {
            saveAsync()
        }
    }

    override fun removeGroupPermission(groupName: String, permission: String) {
        val group = permissionManager.getGroup(groupName) ?: return
        if (group.nodes.removeIf { it.key.equals(permission, ignoreCase = true) }) {
            saveAsync()
        }
    }

    override fun addGroupPermission(group: Group, permission: String) {
        if (group.nodes.add(Node(permission))) {
            saveAsync()
        }
    }

    override fun removeGroupPermission(group: Group, permission: String) {
        if (group.nodes.removeIf { it.key.equals(permission, ignoreCase = true) }) {
            saveAsync()
        }
    }

    override fun setPlayerToGroup(uuid: UUID, group: String) {
        val user = permissionManager.getUser(uuid) ?: return
        user.nodes.removeIf { it.key.startsWith("group.", ignoreCase = true) }
        user.nodes.add(Node("group.$group"))
        saveAsync()
    }

    override fun removePlayerFromGroup(uuid: UUID, group: String) {
        val user = permissionManager.getUser(uuid) ?: return
        if (user.nodes.removeIf { it.key.equals("group.$group", ignoreCase = true) }) {
            saveAsync()
        }
    }

    override fun addPlayerToGroup(uuid: UUID, group: String) {
        val user = permissionManager.getUser(uuid) ?: return
        if (user.nodes.none { it.key.equals("group.$group", ignoreCase = true) }) {
            user.nodes.add(Node("group.$group"))
            saveAsync()
        }
    }

    override fun translateColors(message: String): Message {
        return ColorHelper.translateMessageColors(message)
    }

    private fun saveAsync() {
        EtherealPerms.storage.storageScope.launch {
            permissionManager.saveData()
        }
    }
}