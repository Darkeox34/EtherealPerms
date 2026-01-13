package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory

class UserInfoCommand : CommandBase("info", "etherealperms.command.user.info.desc") {

    // Using PLAYER_REF would be ideal, but STRING is more flexible if the player is offline.
    // The Hytale API for resolving offline players isn't specified, so we'll use STRING.
    private val playerArg = withRequiredArg("player", "The target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission("etherealperms.user.info")
    }

    override fun executeSync(context: CommandContext) {
        // This is a placeholder implementation.
        // A real implementation would need to resolve the player's UUID from their name,
        // even if they are offline, which requires a UUID cache or server API.
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

        context.sendMessage(MessageFactory.info("--- User Info: ${user.username} ---"))
        context.sendMessage(MessageFactory.info("UUID: ${user.uuid}"))
        context.sendMessage(MessageFactory.info("Permissions: ${user.nodes.joinToString { it.key }}"))
    }
}