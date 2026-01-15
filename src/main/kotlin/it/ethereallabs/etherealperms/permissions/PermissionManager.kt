package it.ethereallabs.etherealperms.permissions

import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.EtherealPerms.Companion.storage
import it.ethereallabs.etherealperms.permissions.models.ChatMeta
import it.ethereallabs.etherealperms.permissions.models.Group
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.permissions.models.User
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages users, groups, and permission calculations.
 */
class PermissionManager(private val plugin: EtherealPerms) {

    private val groups = ConcurrentHashMap<String, Group>()
    private val users = ConcurrentHashMap<UUID, User>()

     fun reloadData(){
        groups.clear()
        loadData()
    }

    /**
     * Loads groups from storage and initializes default group if necessary.
     */
    fun loadData() {
        groups.putAll(storage.loadGroups())
        if (groups.isEmpty()) {
            plugin.logger.atInfo().log("No groups found, creating a default group.")
            val defaultGroup = storage.loadDefaultGroup()
            groups[defaultGroup.name] = defaultGroup
        }
        plugin.logger.atInfo().log("Loaded ${groups.size} groups.")
    }

    /**
     * Saves all groups and cached users to storage.
     */
    fun saveData() {
        storage.saveGroups(groups)
        users.values.forEach { storage.saveUser(it) }
        plugin.logger.atInfo().log("Saved all permissions data.")
    }

    /**
     * Loads a user from storage or creates a new one if not found.
     */
    fun loadUser(uuid: UUID, username: String): User {
        val user = storage.loadUser(uuid) ?: User(uuid, username).apply {
            if (groups.containsKey("default")) {
                nodes.add(Node("group.default"))
            }
        }
        users[uuid] = user
        return user
    }

    /**
     * Unloads a user from cache, saving their data first.
     */
    fun unloadUser(uuid: UUID) {
        val user = users.remove(uuid)
        if (user != null) {
            storage.saveUser(user)
        }
    }

    fun getUser(uuid: UUID): User? = users[uuid]

    fun getUserPrimaryGroup(uuid: UUID): Group? {
        val user = getUser(uuid) ?: return null
        return user.nodes
            .asSequence()
            .filter { it.key.startsWith("group.", ignoreCase = true) }
            .mapNotNull { node ->
                getGroup(node.key.substringAfter("group."))
            }
            .maxByOrNull { it.weight }
    }


    fun getGroup(name: String): Group? = groups[name.lowercase()]

    fun getAllGroups(): List<Group> = groups.values.toList()

    /**
     * Scans all users to find those belonging to a specific group.
     */
    fun getUsersWithGroup(groupName: String): List<String> {
        // This scans all user files. For large servers, this should be cached or indexed.
        val allUsers = storage.loadAllUsers()
        return allUsers.filter { user ->
            user.nodes.any { node ->
                node.key.equals("group.$groupName", ignoreCase = true)
            }
        }.map { it.username }
    }

    /**
     * Creates a new group if it doesn't exist.
     */
    fun createGroup(name: String): Group? {
        if (groups.containsKey(name.lowercase())) {
            return null
        }
        val group = Group(name.lowercase())
        groups[group.name] = group
        storage.saveGroups(groups)
        return group
    }

    /**
     * Deletes a group.
     */
    fun deleteGroup(name: String): Boolean {
        if (!groups.containsKey(name.lowercase())) return false
        groups.remove(name.lowercase())
        saveData()
        return true
    }

    /**
     * Renames an existing group.
     */
    fun renameGroup(oldName: String, newName: String): Boolean {
        val group = getGroup(oldName) ?: return false
        if (getGroup(newName) != null) return false

        val newGroup = group.copy(name = newName.lowercase())
        groups.remove(oldName.lowercase())
        groups[newGroup.name] = newGroup
        saveData()
        return true
    }

    /**
     * Clones an existing group to a new name.
     */
    fun cloneGroup(name: String, newName: String): Boolean {
        val group = getGroup(name) ?: return false
        if (getGroup(newName) != null) return false

        val newGroup = group.copy(name = newName.lowercase(), nodes = group.nodes.toMutableSet())
        groups[newGroup.name] = newGroup
        saveData()
        return true
    }

    /**
     * Retrieves chat metadata (prefix, suffix) for a user.
     */
    fun getChatMeta(uuid: UUID): ChatMeta {
        val user = getUser(uuid) ?: return ChatMeta("", "")
        val allNodes = getEffectiveNodes(user)

        var prefix = ""
        var prefixPriority = Int.MIN_VALUE

        var suffix = ""
        var suffixPriority = Int.MIN_VALUE

        for (node in allNodes) {
            val key = node.key
            if (key.startsWith("prefix.", ignoreCase = true)) {
                val parts = key.split(".", limit = 3)
                if (parts.size == 3) {
                    val prio = parts[1].toIntOrNull() ?: 0
                    if (prio >= prefixPriority) {
                        prefixPriority = prio
                        prefix = parts[2]
                    }
                }
            }
            else if (key.startsWith("suffix.", ignoreCase = true)) {
                val parts = key.split(".", limit = 3)
                if (parts.size == 3) {
                    val prio = parts[1].toIntOrNull() ?: 0
                    if (prio >= suffixPriority) {
                        suffixPriority = prio
                        suffix = parts[2]
                    }
                }
            }
        }
        return ChatMeta(
            prefix,
            suffix
        )
    }

    /**
     * Calculates the effective permissions for a user, resolving group inheritance.
     */
    fun getEffectivePermissions(user: User): Map<String, Boolean> {
        val perms = mutableMapOf<String, Boolean>()
        val visitedGroups = mutableSetOf<String>()

        fun collectGroupPermissions(group: Group) {
            if (!visitedGroups.add(group.name.lowercase())) return

            // Handle inheritance: Process parent groups first
            val parents = group.nodes
                .filter { it.key.startsWith("group.") }
                .mapNotNull { getGroup(it.key.substring(6)) }
                .sortedBy { it.weight } // Process lower priority first, so higher overrides

            parents.forEach { collectGroupPermissions(it) }

            // Apply current group nodes
            group.nodes.forEach { node ->
                if (!node.key.startsWith("group.")) {
                    perms[node.key.lowercase()] = node.value
                }
            }
        }

        // Process user groups
        val userGroups = user.nodes
            .filter { it.key.startsWith("group.") }
            .mapNotNull { getGroup(it.key.substring(6)) }
            .sortedBy { it.weight }

        userGroups.forEach { collectGroupPermissions(it) }

        // Apply user specific nodes (highest priority)
        user.nodes.forEach { node ->
            if (!node.key.startsWith("group.")) {
                perms[node.key.lowercase()] = node.value
            }
        }
        return perms
    }

    private fun getEffectiveNodes(user: User): List<Node> {
        val nodes = mutableListOf<Node>()
        val visitedGroups = mutableSetOf<String>()

        fun collectGroupNodes(group: Group) {
            if (!visitedGroups.add(group.name.lowercase())) return

            val parents = group.nodes
                .filter { it.key.startsWith("group.", ignoreCase = true) }
                .mapNotNull { getGroup(it.key.substring(6)) }
                .sortedBy { it.weight }

            parents.forEach { collectGroupNodes(it) }
            nodes.addAll(group.nodes)
        }

        val userGroups = user.nodes
            .filter { it.key.startsWith("group.", ignoreCase = true) }
            .mapNotNull { getGroup(it.key.substring(6)) }
            .sortedBy { it.weight }

        userGroups.forEach { collectGroupNodes(it) }
        nodes.addAll(user.nodes)

        return nodes
    }
}