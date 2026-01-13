package it.ethereallabs.etherealperms

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent
import it.ethereallabs.etherealperms.command.EtherealPermsCommand
import it.ethereallabs.etherealperms.data.ChatListener
import it.ethereallabs.etherealperms.data.PermissionManager
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level


/**
 * Main class for the EtherealPerms plugin.
 */
class EtherealPerms(init: JavaPluginInit) : JavaPlugin(init) {

    lateinit var permissionManager: PermissionManager
        private set

    companion object {
        lateinit var instance: EtherealPerms
            private set
    }

    init {
        instance = this
    }

    protected override fun setup() {
        permissionManager = PermissionManager(this)
        permissionManager.loadData()

        val permissionsModule = PermissionsModule.get()
        if (permissionsModule != null) {
            permissionsModule.addProvider(EtherealPermissionProvider(permissionManager))
            logger.atInfo().log("Registered EtherealPermissionProvider with PermissionsModule")
        } else {
            logger.atSevere().log("PermissionsModule not found! Permissions will not work correctly.")
        }

        commandRegistry.registerCommand(EtherealPermsCommand())

        eventRegistry.register(PlayerConnectEvent::class.java, this::onPlayerConnect)
        eventRegistry.register(PlayerDisconnectEvent::class.java, this::onPlayerDisconnect)
        eventRegistry.register(PlayerChatEvent::class.java, ChatListener()::onPlayerChat)
        
        logger.atInfo().log("EtherealPerms Setup Completed")
        logger.atInfo().log("Created by EtherealLabs")
        logger.atInfo().log("https://ethereallabs.it")
    }

    protected override fun start() {
        logger.atInfo().log("EtherealPerms Enabled")
        logger.atInfo().log("Created by EtherealLabs")
        logger.atInfo().log("https://ethereallabs.it")
    }

    protected override fun shutdown() {
        permissionManager.saveData()
        logger.atInfo().log("EtherealPerms Disabled")
        logger.atInfo().log("Created by EtherealLabs")
        logger.atInfo().log("https://ethereallabs.it")
    }

    private fun onPlayerConnect(event: PlayerConnectEvent) {
        permissionManager.loadUser(event.playerRef.uuid, event.playerRef.username)
    }

    private fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        permissionManager.unloadUser(event.playerRef.uuid)
    }
}

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
        // Maps boolean values: true -> "node", false -> "-node" to handle negations.
        // Removes trailing ".*" for compatibility with Hytale's resolution logic.
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