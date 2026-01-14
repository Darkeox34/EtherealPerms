package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class UserMetaAddPrefixCommand : CommandBase("addprefix", "etherealperms.command.user.meta.addprefix.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withRequiredArg("prefix", "Prefix string", ArgTypes.STRING)
    private val colorArg = withOptionalArg("color", "Prefix color (Hex)", ArgTypes.STRING)
    private val formatArg = withOptionalArg("format", "Format (bold,italic,etc)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.addprefix")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context)
        val color = if (colorArg.provided(context)) colorArg.get(context) else null
        val format = if (formatArg.provided(context)) formatArg.get(context) else null
        val manager = EtherealPerms.instance.permissionManager
        val user = manager.loadUser(player.uuid, player.username)

        user.nodes.add(Node("prefix.$priority.$prefix"))
        if (color != null) {
            user.nodes.add(Node("prefix_color.$priority.$color"))
        }
        if (format != null) {
            user.nodes.add(Node("prefix_format.$priority.$format"))
        }
        
        manager.saveData()
        context.sendMessage(MessageFactory.success("Added prefix '$prefix' with priority $priority to user '${player.username}'."))
    }
}