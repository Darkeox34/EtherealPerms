package it.ethereallabs.etherealperms

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import it.ethereallabs.etherealperms.command.EtherealPermsCommand
import it.ethereallabs.etherealperms.data.FileStorage
import it.ethereallabs.etherealperms.data.IStorageMethod
import it.ethereallabs.etherealperms.data.MongoStorage
import it.ethereallabs.etherealperms.data.MySqlStorage
import it.ethereallabs.etherealperms.data.Storage
import it.ethereallabs.etherealperms.events.ChatListener
import it.ethereallabs.etherealperms.permissions.EtherealPermissionProvider
import it.ethereallabs.etherealperms.permissions.PermissionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


/**
 * Main class for the EtherealPerms plugin.
 */
class EtherealPerms(init: JavaPluginInit) : JavaPlugin(init) {

    companion object {
        lateinit var instance: EtherealPerms
            private set
        lateinit var permissionManager: PermissionManager
            private set
        lateinit var storage: Storage
            private set
    }

    init {
        instance = this
        storage = Storage(this)
        permissionManager = PermissionManager(this)
    }

    protected override fun setup() {
        runBlocking {
            permissionManager.loadData()
        }

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
        runBlocking {
            permissionManager.saveData()
        }
        logger.atInfo().log("EtherealPerms Disabled")
        logger.atInfo().log("Created by EtherealLabs")
        logger.atInfo().log("https://ethereallabs.it")
    }

    private fun onPlayerConnect(event: PlayerConnectEvent) {
        storage.storageScope.launch{
            permissionManager.loadUser(event.playerRef.uuid, event.playerRef.username)
        }
    }

    private fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        storage.storageScope.launch {
            permissionManager.unloadUser(event.playerRef.uuid)
        }
    }
}