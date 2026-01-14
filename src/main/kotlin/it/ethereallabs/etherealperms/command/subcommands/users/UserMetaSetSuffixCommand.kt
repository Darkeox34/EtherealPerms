package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class UserMetaSetSuffixCommand : CommandBase("setsuffix", "etherealperms.command.user.meta.setsuffix.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Suffix priority", ArgTypes.INTEGER)
    private val suffixArg = withRequiredArg("suffix", "Suffix string", ArgTypes.STRING)
    private val colorArg = withOptionalArg("color", "Suffix color (Hex)", ArgTypes.STRING)
    private val formatArg = withOptionalArg("format", "Format (bold,italic,etc)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.setsuffix")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val suffix = suffixArg.get(context)
        val color = if (colorArg.provided(context)) colorArg.get(context) else null
        val format = if (formatArg.provided(context)) formatArg.get(context) else null
        
        val manager = EtherealPerms.instance.permissionManager
        val user = manager.loadUser(player.uuid, player.username)

        // Remove all existing suffix nodes
        user.nodes.removeIf { 
            it.key.startsWith("suffix.") || 
            it.key.startsWith("suffix_color.") || 
            it.key.startsWith("suffix_format.") 
        }

        user.nodes.add(Node("suffix.$priority.$suffix"))
        if (color != null) {
            user.nodes.add(Node("suffix_color.$priority.$color"))
        }
        if (format != null) {
            user.nodes.add(Node("suffix_format.$priority.$format"))
        }
        
        manager.saveData()
        context.sendMessage(MessageFactory.success("Set suffix '$suffix' (prio: $priority) for user '${player.username}' (cleared old suffixes)."))
    }
}