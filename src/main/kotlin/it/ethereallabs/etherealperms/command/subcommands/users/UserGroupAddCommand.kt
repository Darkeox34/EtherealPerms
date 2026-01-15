package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch

class UserGroupAddCommand : CommandBase("add", "etherealperms.command.user.group.add.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val groupArg = withRequiredArg("group", "Group to add", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.group.add")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val groupName = groupArg.get(context)
        val manager = EtherealPerms.permissionManager

        if (manager.getGroup(groupName) == null) {
            context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
            return
        }

        EtherealPerms.storage.storageScope.launch {
            try {
                val user = manager.loadUser(player.uuid, player.username)
                val parentNode = "group.$groupName"

                if (user.nodes.any { it.key.equals(parentNode, ignoreCase = true) }) {
                    Universe.get().worlds.values.random().execute {
                        context.sendMessage(MessageFactory.error("User is already in group '$groupName'."))
                    }
                    return@launch
                }

                user.nodes.add(Node(parentNode))
                manager.saveData()

                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Added group '$groupName' to user '${player.username}'."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to add group to user: ${e.message}"))
                }
            }
        }
    }
}