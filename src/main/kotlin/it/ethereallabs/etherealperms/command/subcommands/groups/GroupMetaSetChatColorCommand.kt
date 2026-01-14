package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class GroupMetaSetChatColorCommand : CommandBase("setchatcolor", "etherealperms.command.group.meta.setchatcolor.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Priority", ArgTypes.INTEGER)
    private val colorArg = withRequiredArg("color", "Chat color (Hex)", ArgTypes.STRING)
    private val formatArg = withOptionalArg("format", "Format (bold,italic,etc)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.setchatcolor")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val color = colorArg.get(context)
        val format = if (formatArg.provided(context)) formatArg.get(context) else null

        val manager = EtherealPerms.instance.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        group.nodes.removeIf { it.key.startsWith("chat_color.") || it.key.startsWith("chat_format.") }

        group.nodes.add(Node("chat_color.$priority.$color"))
        if (format != null) {
            group.nodes.add(Node("chat_format.$priority.$format"))
        }
        manager.saveData()
        context.sendMessage(MessageFactory.success("Set chat color '$color' for group '${group.name}'."))
    }
}