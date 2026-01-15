package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class UserPermissionUnsetCommand : CommandBase("unset", "etherealperms.command.user.permission.unset.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val nodeArg = withRequiredArg("node", "Permission node", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.permission.unset")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val nodeKey = nodeArg.get(context)
        val manager = EtherealPerms.permissionManager

        EtherealPerms.storage.storageScope.launch {
            try {
                val user = manager.loadUser(player.uuid, player.username)
                val removed = user.nodes.removeIf { it.key.equals(nodeKey, ignoreCase = true) }

                if (removed) {
                    manager.saveData()
                }

                Universe.get().worlds.values.random().execute {
                    if (removed) {
                        context.sendMessage(MessageFactory.success("Permission '$nodeKey' unset for user '${player.username}'."))
                    } else {
                        context.sendMessage(MessageFactory.error("Permission '$nodeKey' not found for user '${player.username}'."))
                    }
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to unset permission: ${e.message}"))
                }
            }
        }
    }
}