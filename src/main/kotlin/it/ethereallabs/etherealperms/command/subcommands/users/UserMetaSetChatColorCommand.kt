package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class UserMetaSetChatColorCommand : CommandBase("setchatcolor", "etherealperms.command.user.meta.setchatcolor.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Priority", ArgTypes.INTEGER)
    private val colorArg = withRequiredArg("color", "Chat color (Hex)", ArgTypes.STRING)
    private val formatArg = withOptionalArg("format", "Format (bold,italic,etc)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.setchatcolor")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val color = colorArg.get(context)
        val format = if (formatArg.provided(context)) formatArg.get(context) else null
        
        val manager = EtherealPerms.instance.permissionManager
        val user = manager.loadUser(player.uuid, player.username)

        // Remove existing chat color/format nodes
        user.nodes.removeIf { 
            it.key.startsWith("chat_color.") || 
            it.key.startsWith("chat_format.") 
        }

        user.nodes.add(Node("chat_color.$priority.$color"))
        if (format != null) {
            user.nodes.add(Node("chat_format.$priority.$format"))
        }
        
        manager.saveData()
        context.sendMessage(MessageFactory.success("Set chat color '$color' for user '${player.username}'."))
    }
}