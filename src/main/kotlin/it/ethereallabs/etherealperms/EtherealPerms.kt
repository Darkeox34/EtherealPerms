package it.ethereallabs.etherealperms

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import it.ethereallabs.etherealperms.command.EtherealPermsCommand
import it.ethereallabs.etherealperms.data.PermissionManager
import java.util.UUID

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

        // Registra il PermissionProvider nel PermissionsModule di Hytale
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
        
        logger.atInfo().log("EtherealPerms Setup Completed")
    }

    protected override fun start() {
        logger.atInfo().log("EtherealPerms Enabled")
    }

    protected override fun shutdown() {
        permissionManager.saveData()
        logger.atInfo().log("EtherealPerms Disabled")
    }

    private fun onPlayerConnect(event: PlayerConnectEvent) {
        logger.atInfo().log("DEBUG: onPlayerConnect - Loading user ${event.playerRef.username}")
        permissionManager.loadUser(event.playerRef.uuid, event.playerRef.username)
    }

    private fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        permissionManager.unloadUser(event.playerRef.uuid)
    }
}

class EtherealPermissionProvider(private val permissionManager: PermissionManager) : PermissionProvider {
    override fun getName(): String = "EtherealPerms"

    override fun getUserPermissions(uuid: UUID): Set<String> {
        val user = permissionManager.getUser(uuid)
        if (user == null) {
            EtherealPerms.instance.logger.atInfo().log("DEBUG: getUserPermissions - User $uuid not found in cache")
            return emptySet()
        }
        
        // Ottiene tutti i permessi calcolati (inclusi gruppi ed ereditarietà)
        // Mappa i valori booleani: true -> "nodo", false -> "-nodo" per gestire le negazioni
        // Rimuove ".*" finale per compatibilità con la logica di risoluzione di Hytale
        val effective = permissionManager.getEffectivePermissions(user)
        val result = effective.map { (key, value) ->
            val cleanKey = if (key.endsWith(".*")) key.dropLast(2) else key
            if (value) cleanKey else "-$cleanKey"
        }.toSet()

        EtherealPerms.instance.logger.atInfo().log("DEBUG: getUserPermissions(${user.username}) - Returning: $result")
        return result
    }

    // Ritorna i gruppi dell'utente per compatibilità con altri plugin
    override fun getGroupsForUser(uuid: UUID): Set<String> {
        val user = permissionManager.getUser(uuid) ?: return emptySet()
        val groups = user.nodes
            .filter { it.key.startsWith("group.") }
            .map { it.key.substring(6) }
            .toSet()

        EtherealPerms.instance.logger.atInfo().log("DEBUG: getGroupsForUser(${user.username}) - Returning: $groups")
        return groups
    }

    override fun getGroupPermissions(group: String): Set<String> = emptySet()

    // Metodi di modifica lasciati vuoti perché gestiamo tutto tramite i comandi del plugin
    override fun addUserPermissions(uuid: UUID, permissions: Set<String>) {}
    override fun removeUserPermissions(uuid: UUID, permissions: Set<String>) {}
    override fun addGroupPermissions(group: String, permissions: Set<String>) {}
    override fun removeGroupPermissions(group: String, permissions: Set<String>) {}
    override fun addUserToGroup(uuid: UUID, group: String) {}
    override fun removeUserFromGroup(uuid: UUID, group: String) {}
}