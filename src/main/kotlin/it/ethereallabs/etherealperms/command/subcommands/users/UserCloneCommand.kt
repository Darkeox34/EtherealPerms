package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class UserCloneCommand : CommandBase("clone", "etherealperms.command.user.clone.desc") {

    private val sourceArg = withRequiredArg("source", "Source player", ArgTypes.PLAYER_REF)
    private val targetArg = withRequiredArg("target", "Target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission("etherealperms.user.clone")
    }

    override fun executeSync(context: CommandContext) {
        val sourcePlayer = sourceArg.get(context)
        val targetPlayer = targetArg.get(context)
        val manager = EtherealPerms.permissionManager
        
        val sourceUser = manager.loadUser(sourcePlayer.uuid, sourcePlayer.username)
        val targetUser = manager.loadUser(targetPlayer.uuid, targetPlayer.username)

        targetUser.nodes.clear()
        targetUser.nodes.addAll(sourceUser.nodes)
        
        manager.saveData()
        context.sendMessage(MessageFactory.success("Cloned permissions from '${sourcePlayer.username}' to '${targetPlayer.username}'."))
    }
}