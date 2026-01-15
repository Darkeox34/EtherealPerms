package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

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
        // We use loadUser to ensure we can modify permissions even if the user wasn't previously cached
        val user = manager.loadUser(player.uuid, player.username)

        val removed = user.nodes.removeIf { it.key.equals(nodeKey, ignoreCase = true) }
        manager.saveData()
        context.sendMessage(MessageFactory.success(if (removed) "Permission '$nodeKey' unset for user '${player.username}'." else "Permission not found."))
    }
}