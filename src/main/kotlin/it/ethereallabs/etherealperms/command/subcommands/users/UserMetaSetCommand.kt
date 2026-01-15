package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class UserMetaSetCommand : CommandBase("set", "etherealperms.command.user.meta.set.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val keyArg = withRequiredArg("key", "Meta key", ArgTypes.STRING)
    private val valueArg = withRequiredArg("value", "Meta value", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.set")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val key = keyArg.get(context)
        val value = valueArg.get(context)
        val manager = EtherealPerms.permissionManager
        val user = manager.loadUser(player.uuid, player.username)

        user.nodes.add(Node("meta.$key.$value"))
        
        manager.saveData()
        context.sendMessage(MessageFactory.success("Meta '$key' set to '$value' for user '${player.username}'."))
    }
}