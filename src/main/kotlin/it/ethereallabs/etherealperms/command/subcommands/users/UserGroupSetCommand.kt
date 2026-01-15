package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch

class UserGroupSetCommand : CommandBase("set", "etherealperms.command.user.group.set.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val groupArg = withRequiredArg("group", "Group to set", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.group.set")
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

                user.nodes.removeIf { it.key.startsWith("group.") }
                user.nodes.add(Node("group.$groupName"))

                manager.saveData()

                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Set group for user '${player.username}' to '$groupName'."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to set user group: ${e.message}"))
                }
            }
        }
    }
}