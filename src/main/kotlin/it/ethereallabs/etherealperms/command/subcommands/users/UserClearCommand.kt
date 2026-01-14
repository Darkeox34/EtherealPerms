package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class UserClearCommand : CommandBase("clear", "etherealperms.command.user.clear.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission("etherealperms.user.clear")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val manager = EtherealPerms.instance.permissionManager
        val user = manager.loadUser(player.uuid, player.username)

        user.nodes.clear()
        manager.saveData()
        context.sendMessage(MessageFactory.success("Cleared all permissions for user '${player.username}'."))
    }
}