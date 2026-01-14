package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

/**
 * Command to display information about a user.
 */
class UserInfoCommand : CommandBase("info", "etherealperms.command.user.info.desc") {

    private val playerArg = withRequiredArg("player", "The target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission("etherealperms.user.info")
    }

    override fun executeSync(context: CommandContext) {
        // This is a placeholder implementation, does not support offline players

        val player = playerArg.get(context)

        if (player == null) {
            context.sendMessage(MessageFactory.error("Player not found."))
            return
        }

        val user = EtherealPerms.instance.permissionManager.getUser(player.uuid)
        if (user == null) {
            context.sendMessage(MessageFactory.error("Permissions data for '${player.username}' not found."))
            return
        }

        context.sendMessage(MessageFactory.info("User Info: ${user.username}"))
        context.sendMessage(MessageFactory.info("UUID: ${user.uuid}"))
        
        val groups = user.nodes.filter { it.key.startsWith("group.") }.joinToString(", ") { it.key.substring(6) }
        context.sendMessage(MessageFactory.info("Groups: $groups"))
        context.sendMessage(MessageFactory.info("Permissions: ${user.nodes.joinToString { it.key }}"))
    }
}