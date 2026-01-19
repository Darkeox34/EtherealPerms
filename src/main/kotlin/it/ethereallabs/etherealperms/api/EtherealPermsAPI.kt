package it.ethereallabs.etherealperms.api

import com.hypixel.hytale.server.core.Message
import it.ethereallabs.etherealperms.permissions.models.Group
import java.util.UUID

/**
 * Public API for EtherealPerms.
 */
interface EtherealPermsAPI {

    /**
     * Checks if a user has a specific permission.
     * @param uuid The UUID of the user.
     * @param permission The permission node to check.
     * @return true if the user has the permission, false otherwise.
     */
    fun hasPermission(uuid: UUID, permission: String): Boolean

    /**
     * Gets the chat prefix for a user.
     * @param uuid The UUID of the user.
     * @return The prefix string, or empty if none.
     */
    fun getUserPrefix(uuid: UUID): String

    /**
     * Gets the chat suffix for a user.
     * @param uuid The UUID of the user.
     * @return The suffix string, or empty if none.
     */
    fun getUserSuffix(uuid: UUID): String

    /**
     * Gets the name of the primary group for a user.
     * @param uuid The UUID of the user.
     * @return The group name, or null if no primary group is found.
     */
    fun getUserPrimaryGroup(uuid: UUID): Group?

    /**
     * Gets a list of all group names the user belongs to.
     * @param uuid The UUID of the user.
     * @return A list of group names.
     */
    fun getUserGroups(uuid: UUID): List<Group>

    /**
     * Checks if a player is inside a specific group.
     * @param uuid The UUID of the user.
     * @param group The group to check.
     * @return True if the player has this group, false otherwise.
     */
    fun isPlayerInGroup(uuid: UUID, group: String): Boolean

    /**
     * Grants a specific permission node to a user.
     * @param uuid The UUID of the user.
     * @param permission The permission node to add.
     */
    fun addUserPermission(uuid: UUID, permission: String)

    /**
     * Revokes a specific permission node from a user.
     * @param uuid The UUID of the user.
     * @param permission The permission node to remove.
     */
    fun removeUserPermission(uuid: UUID, permission: String)

    /**
     * Grants a specific permission node to a user.
     * @param groupName The name of the group
     * @param permission The permission node to add.
     */
    fun addGroupPermission(groupName: String, permission: String)

    /**
     * Revokes a specific permission node from a user.
     * @param groupName The name of the group
     * @param permission The permission node to remove.
     */
    fun removeGroupPermission(groupName: String, permission: String)

    /**
     * Grants a specific permission node to a user.
     * @param group The group object instance
     * @param permission The permission node to add.
     */
    fun addGroupPermission(group: Group, permission: String)

    /**
     * Revokes a specific permission node from a user.
     * @param group The group object instance
     * @param permission The permission node to remove.
     */
    fun removeGroupPermission(group: Group, permission: String)

    /**
     * Sets a player's primary group, typically replacing existing groups.
     * @param uuid The UUID of the user.
     * @param group The group name to set.
     */
    fun setPlayerToGroup(uuid: UUID, group: String)

    /**
     * Removes a player from a specific group.
     * @param uuid The UUID of the user.
     * @param group The group name to remove the player from.
     */
    fun removePlayerFromGroup(uuid: UUID, group: String)

    /**
     * Adds a player to a group without removing them from their current groups.
     * @param uuid The UUID of the user.
     * @param group The group name to add.
     */
    fun addPlayerToGroup(uuid: UUID, group: String)

    /**
     * Translates hex and legacy color codes into a message component.
     * @param message The string containing color tags (e.g., "<red>Text</red>" or "&cText").
     * @return A [Message] component with applied styles and colors.
     */
    fun translateColors(message: String): Message
}