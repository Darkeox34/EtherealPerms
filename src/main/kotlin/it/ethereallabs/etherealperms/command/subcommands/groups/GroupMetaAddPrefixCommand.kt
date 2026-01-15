package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class GroupMetaAddPrefixCommand : CommandBase("addprefix", "etherealperms.command.group.meta.addprefix.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withRequiredArg("prefix", "Prefix string", ArgTypes.STRING)
    private val colorArg = withOptionalArg("color", "Prefix color (Hex)", ArgTypes.STRING)
    private val formatArg = withOptionalArg("format", "Format (bold,italic,etc)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.addprefix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context)
        val color = if (colorArg.provided(context)) colorArg.get(context) else null
        val format = if (formatArg.provided(context)) formatArg.get(context) else null
        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        group.nodes.add(Node("prefix.$priority.$prefix"))
        if (color != null) {
            group.nodes.add(Node("prefix_color.$priority.$color"))
        }
        if (format != null) {
            group.nodes.add(Node("prefix_format.$priority.$format"))
        }
        manager.saveData()
        context.sendMessage(MessageFactory.success("Added prefix '$prefix' with priority $priority to group '${group.name}'."))
    }
}