package it.ethereallabs.etherealperms.data

import it.ethereallabs.etherealperms.EtherealPerms
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PermissionManager(private val plugin: EtherealPerms) {

    private val storage = Storage(plugin)
    private val groups = ConcurrentHashMap<String, Group>()
    private val users = ConcurrentHashMap<UUID, User>()

    fun loadData() {
        groups.putAll(storage.loadGroups())
        if (groups.isEmpty()) {
            plugin.logger.atInfo().log("No groups found, creating a default group.")
            val defaultGroup = storage.loadDefaultGroup()
            groups[defaultGroup.name] = defaultGroup
        }
        plugin.logger.atInfo().log("Loaded ${groups.size} groups.")
    }

    fun saveData() {
        storage.saveGroups(groups)
        users.values.forEach { storage.saveUser(it) }
        plugin.logger.atInfo().log("Saved all permissions data.")
    }

    fun loadUser(uuid: UUID, username: String): User {
        val user = storage.loadUser(uuid) ?: User(uuid, username).apply {
            // Add default group to new users
            if (groups.containsKey("default")) {
                nodes.add(Node("group.default"))
            }
        }
        users[uuid] = user
        return user
    }

    fun unloadUser(uuid: UUID) {
        val user = users.remove(uuid)
        if (user != null) {
            storage.saveUser(user)
        }
    }

    fun getUser(uuid: UUID): User? = users[uuid]

    fun getGroup(name: String): Group? = groups[name.lowercase()]

    fun getAllGroups(): List<Group> = groups.values.toList()

    fun createGroup(name: String): Group? {
        if (groups.containsKey(name.lowercase())) {
            return null
        }
        val group = Group(name.lowercase())
        groups[group.name] = group
        storage.saveGroups(groups)
        return group
    }

    fun deleteGroup(name: String): Boolean {
        if (!groups.containsKey(name.lowercase())) return false
        groups.remove(name.lowercase())
        saveData()
        return true
    }

    fun renameGroup(oldName: String, newName: String): Boolean {
        val group = getGroup(oldName) ?: return false
        if (getGroup(newName) != null) return false

        val newGroup = group.copy(name = newName.lowercase())
        groups.remove(oldName.lowercase())
        groups[newGroup.name] = newGroup
        saveData()
        return true
    }

    fun cloneGroup(name: String, newName: String): Boolean {
        val group = getGroup(name) ?: return false
        if (getGroup(newName) != null) return false

        val newGroup = group.copy(name = newName.lowercase(), nodes = group.nodes.toMutableSet())
        groups[newGroup.name] = newGroup
        saveData()
        return true
    }

    fun hasPermission(uuid: UUID, permission: String): Boolean {
        val user = getUser(uuid) ?: return false

        // Resolve permissions with inheritance and wildcards
        val userPermissions = getEffectivePermissions(user) 
        val lowerPerm = permission.lowercase()

        // 1. Check specific permission
        if (userPermissions.containsKey(lowerPerm)) {
            return userPermissions[lowerPerm]!!
        }

        // 2. Check wildcards (from most specific to least specific)
        var current = lowerPerm
        while (current.contains(".")) {
            current = current.substringBeforeLast(".")
            val wildcard = "$current.*"
            if (userPermissions.containsKey(wildcard)) {
                return userPermissions[wildcard]!!
            }
        }

        // 3. Check global wildcard
        if (userPermissions.containsKey("*")) {
            return userPermissions["*"]!!
        }

        return false
    }

    fun getEffectivePermissions(user: User): Map<String, Boolean> {
        val perms = mutableMapOf<String, Boolean>()
        val visitedGroups = mutableSetOf<String>()

        fun collectGroupPermissions(group: Group) {
            if (!visitedGroups.add(group.name.lowercase())) return

            // Handle inheritance: Process parent groups first
            // Assuming parents are defined as nodes "group.<name>"
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
}