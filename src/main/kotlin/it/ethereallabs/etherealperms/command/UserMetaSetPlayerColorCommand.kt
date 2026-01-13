package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory
import it.ethereallabs.etherealperms.data.Node

class UserMetaSetPlayerColorCommand : CommandBase("setplayercolor", "etherealperms.command.user.meta.setplayercolor.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Priority", ArgTypes.INTEGER)
    private val colorArg = withRequiredArg("color", "Name color (Hex)", ArgTypes.STRING)
    private val formatArg = withOptionalArg("format", "Format (bold,italic,etc)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.setplayercolor")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val color = colorArg.get(context)
        val format = if (formatArg.provided(context)) formatArg.get(context) else null
        
        val manager = EtherealPerms.instance.permissionManager
        val user = manager.loadUser(player.uuid, player.username)

        // Remove existing username color/format nodes
        user.nodes.removeIf { 
            it.key.startsWith("username_color.") || 
            it.key.startsWith("username_format.") 
        }

        user.nodes.add(Node("username_color.$priority.$color"))
        if (format != null) {
            user.nodes.add(Node("username_format.$priority.$format"))
        }
        
        manager.saveData()
        context.sendMessage(MessageFactory.success("Set username color '$color' for user '${player.username}'."))
    }
}