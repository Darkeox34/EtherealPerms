package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class GroupMetaSetSuffixCommand : CommandBase("setsuffix", "etherealperms.command.group.meta.setsuffix.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Suffix priority", ArgTypes.INTEGER)
    private val suffixArg = withRequiredArg("suffix", "Suffix string", ArgTypes.STRING)
    private val colorArg = withOptionalArg("color", "Suffix color (Hex)", ArgTypes.STRING)
    private val formatArg = withOptionalArg("format", "Format (bold,italic,etc)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.setsuffix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val suffix = suffixArg.get(context)
        val color = if (colorArg.provided(context)) colorArg.get(context) else null
        val format = if (formatArg.provided(context)) formatArg.get(context) else null

        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        group.nodes.removeIf { it.key.startsWith("suffix.") || it.key.startsWith("suffix_color.") || it.key.startsWith("suffix_format.") }

        group.nodes.add(Node("suffix.$priority.$suffix"))
        if (color != null) {
            group.nodes.add(Node("suffix_color.$priority.$color"))
        }
        if (format != null) {
            group.nodes.add(Node("suffix_format.$priority.$format"))
        }
        manager.saveData()
        context.sendMessage(MessageFactory.success("Set suffix '$suffix' (prio: $priority) for group '${group.name}' (cleared old suffixes)."))
    }
}