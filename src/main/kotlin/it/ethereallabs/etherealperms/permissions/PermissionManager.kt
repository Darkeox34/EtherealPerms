package it.ethereallabs.etherealperms.permissions

import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
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

    val groups = ConcurrentHashMap<String, Group>()
    private val users = ConcurrentHashMap<UUID, User>()

     private fun cleanupNodes(nodes: MutableCollection<Node>): Boolean {
        return nodes.removeIf { it.expiry != null && it.expiry < System.currentTimeMillis() }
    }

    private fun ensureDefaultGroup(user: User) {
        if (user.nodes.none { it.key.startsWith("group.", ignoreCase = true) }) {
            val defaultGroup = getDefaultGroup()
            if (defaultGroup != null) {
                user.nodes.add(Node("group.${defaultGroup.name}"))
            }
        }
    }

    suspend fun reloadData(){
        groups.clear()
        loadData()
    }

    /**
     * Loads groups from storage and initializes default group if necessary.
     */
    suspend fun loadData() {
        groups.putAll(storage.loadGroups())
        
        var dirty = false
        groups.values.forEach { group ->
            if (cleanupNodes(group.nodes)) dirty = true
        }

        if (groups.isEmpty()) {
            plugin.logger.atInfo().log("No groups found, creating a default group.")
            val defaultGroup = storage.loadDefaultGroup()
            groups[defaultGroup.name] = defaultGroup
            saveData()
        } else if (dirty) {
            storage.saveGroups(groups)
            plugin.logger.atInfo().log("Cleaned up expired permissions in groups.")
        }
        plugin.logger.atInfo().log("Loaded ${groups.size} groups.")
    }

    /**
     * Saves all groups and cached users to storage.
     */
    suspend fun saveData() {
        groups.values.forEach { cleanupNodes(it.nodes) }
        users.values.forEach { 
            cleanupNodes(it.nodes)
            ensureDefaultGroup(it)
        }

        storage.saveGroups(groups)
        users.values.forEach { storage.saveUser(it) }
        plugin.logger.atInfo().log("Saved all permissions data.")
    }

    /**
     * Loads a user from storage or creates a new one if not found.
     */
    suspend fun loadUser(uuid: UUID, username: String): User {
        var new = false
        val user = storage.loadUser(uuid) ?: User(uuid, username).apply {
            new = true
            val defaultGroup = getDefaultGroup()
            if(defaultGroup != null) {
                nodes.add(Node("group.${defaultGroup.name}"))
            }
            else{
                EtherealPerms.instance.logger.atSevere().log("No default group found! Please add the node etherealperms.default to a group to set it as default.")
            }
        }
        
        if (cleanupNodes(user.nodes)) {
            new = true
        }
        
        ensureDefaultGroup(user)
        if (user.nodes.none { it.key.startsWith("group.") }) new = true // Mark save if ensureDefaultGroup added something

        if(new)
            storage.saveUser(user)
        users[uuid] = user
        return user
    }

    fun getDefaultGroup(): Group?{
        return groups.values.find{
            it.nodes.contains(Node("etherealperms.default"))
        }
    }

    /**
     * Unloads a user from cache, saving their data first.
     */
    suspend fun unloadUser(uuid: UUID) {
        val user = users.remove(uuid)
        if (user != null) {
            cleanupNodes(user.nodes)
            ensureDefaultGroup(user)
            storage.saveUser(user)
        }
    }

    fun getUser(uuid: UUID): User? = users[uuid]

    fun getAllUsers(): List<PlayerRef>{
        return Universe.get().players.toList()
    }

    private fun isNodeValid(node: Node): Boolean {
        return node.expiry == null || node.expiry > System.currentTimeMillis()
    }

    fun getUserPrimaryGroup(uuid: UUID): Group? {
        val user = getUser(uuid) ?: return null
        if (cleanupNodes(user.nodes)) ensureDefaultGroup(user)
        
        return user.nodes
            .asSequence()
            .filter { it.key.startsWith("group.", ignoreCase = true) }
            .filter { it.value }
            .filter { isNodeValid(it) }
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
    suspend fun getUsersWithGroup(groupName: String): List<String> {
        val allUsers = storage.loadAllUsers()
        return allUsers.filter { user ->
            cleanupNodes(user.nodes)
            user.nodes.any { node ->
                node.key.equals("group.$groupName", ignoreCase = true) && isNodeValid(node)
            }
        }.map { it.username }
    }

    /**
     * Creates a new group if it doesn't exist.
     */
    suspend fun createGroup(name: String): Group? {
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
    suspend fun deleteGroup(name: String): Boolean {
        if (!groups.containsKey(name.lowercase())) return false
        groups.remove(name.lowercase())
        saveData()
        return true
    }

    /**
     * Renames an existing group.
     */
    suspend fun renameGroup(oldName: String, newName: String): Boolean {
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
    suspend fun cloneGroup(name: String, newName: String): Boolean {
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
            if (!node.value) continue

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
            } else if (key.startsWith("suffix.", ignoreCase = true)) {
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
        return ChatMeta(prefix, suffix)
    }

    fun hasPermission(uuid: UUID, permission: String): Boolean {
        val user = getUser(uuid) ?: return false
        val effectivePermissions = getEffectivePermissions(user)
        val node = permission.lowercase()

        if (effectivePermissions["*"] == true) return true

        if (effectivePermissions["etherealperms.*"] == true) return true

        return effectivePermissions[node] == true
    }

    /**
     * Calculates the effective permissions for a user, resolving group inheritance.
     */
    fun getEffectivePermissions(user: User): Map<String, Boolean> {
        if (cleanupNodes(user.nodes)) ensureDefaultGroup(user)

        val perms = mutableMapOf<String, Boolean>()
        val visitedGroups = mutableSetOf<String>()

        fun collectGroupPermissions(group: Group) {
            if (!visitedGroups.add(group.name.lowercase())) return

            val parents = group.nodes
                .filter { it.key.startsWith("group.") && it.value && isNodeValid(it) }
                .mapNotNull { getGroup(it.key.substring(6)) }
                .sortedBy { it.weight }

            parents.forEach { collectGroupPermissions(it) }

            group.nodes.forEach { node ->
                if (!node.key.startsWith("group.") && isNodeValid(node)) {
                    perms[node.key] = node.value
                }
            }
        }

        val userGroups = user.nodes
            .filter { it.key.startsWith("group.") && it.value && isNodeValid(it) }
            .mapNotNull { getGroup(it.key.substring(6)) }
            .sortedBy { it.weight }

        userGroups.forEach { collectGroupPermissions(it) }

        user.nodes.forEach { node ->
            if (!node.key.startsWith("group.") && isNodeValid(node)) {
                perms[node.key] = node.value
            }
        }
        return perms
    }

    private fun getEffectiveNodes(user: User): List<Node> {
        if (cleanupNodes(user.nodes)) ensureDefaultGroup(user)

        val nodes = mutableListOf<Node>()
        val visitedGroups = mutableSetOf<String>()

        fun collectGroupNodes(group: Group) {
            if (!visitedGroups.add(group.name.lowercase())) return

            val parents = group.nodes
                .filter { it.key.startsWith("group.", ignoreCase = true) && it.value && isNodeValid(it) }
                .mapNotNull { getGroup(it.key.substring(6)) }
                .sortedBy { it.weight }

            parents.forEach { collectGroupNodes(it) }

            nodes.addAll(group.nodes.filter { isNodeValid(it) })
        }

        val userGroups = user.nodes
            .filter { it.key.startsWith("group.", ignoreCase = true) && it.value && isNodeValid(it) }
            .mapNotNull { getGroup(it.key.substring(6)) }
            .sortedBy { it.weight }

        userGroups.forEach { collectGroupNodes(it) }

        nodes.addAll(user.nodes.filter { isNodeValid(it) })

        return nodes
    }
}