package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class UserGroupRemoveCommand : CommandBase("remove", "etherealperms.command.user.group.remove.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val groupArg = withRequiredArg("group", "Group to remove", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.group.remove")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val groupName = groupArg.get(context)
        val manager = EtherealPerms.permissionManager

        EtherealPerms.storage.storageScope.launch {
            try {
                val user = manager.loadUser(player.uuid, player.username)
                val parentNode = "group.$groupName"
                val removed = user.nodes.removeIf { it.key.equals(parentNode, ignoreCase = true) }

                if (removed) {
                    manager.saveData()
                }

                Universe.get().worlds.values.random().execute {
                    if (removed) {
                        context.sendMessage(MessageFactory.success("Removed group '$groupName' from user '${player.username}'."))
                    } else {
                        context.sendMessage(MessageFactory.error("User '${player.username}' is not in group '$groupName'."))
                    }
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to remove group from user: ${e.message}"))
                }
            }
        }
    }
}