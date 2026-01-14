package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class UserPermissionSetCommand : CommandBase("set", "etherealperms.command.user.permission.set.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val nodeArg = withRequiredArg("node", "Permission node", ArgTypes.STRING)
    private val valueArg = withOptionalArg("value", "true or false", ArgTypes.BOOLEAN)

    init {
        requirePermission("etherealperms.user.permission.set")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val nodeKey = nodeArg.get(context)
        val value = if (valueArg.provided(context)) valueArg.get(context) else true

        val manager = EtherealPerms.instance.permissionManager
        // loadUser ensures we have the user data even if they are offline (if stored) or creates it
        val user = manager.loadUser(player.uuid, player.username)

        user.nodes.removeIf { it.key.equals(nodeKey, ignoreCase = true) }
        user.nodes.add(Node(nodeKey, value))

        manager.saveData()
        context.sendMessage(MessageFactory.success("Set permission '$nodeKey' to '$value' for user '${player.username}'."))
    }
}