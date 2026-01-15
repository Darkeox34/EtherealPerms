package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class GroupMetaSetPrefixCommand : CommandBase("setprefix", "etherealperms.command.group.meta.setprefix.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withRequiredArg("prefix", "Prefix string", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.setprefix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context)

        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        group.nodes.removeIf { it.key.startsWith("prefix.") || it.key.startsWith("prefix_color.") || it.key.startsWith("prefix_format.") }

        group.nodes.add(Node("prefix.$priority.$prefix"))

        manager.saveData()
        context.sendMessage(MessageFactory.success("Set prefix '$prefix' (prio: $priority) for group '${group.name}' (cleared old prefixes)."))
    }
}