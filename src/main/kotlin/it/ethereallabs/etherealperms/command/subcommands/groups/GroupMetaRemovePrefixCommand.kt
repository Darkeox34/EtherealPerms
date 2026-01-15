package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class GroupMetaRemovePrefixCommand : CommandBase("removeprefix", "etherealperms.command.group.meta.removeprefix.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withRequiredArg("prefix", "Prefix", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.removeprefix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context)
        val manager = EtherealPerms.permissionManager

        val group = manager.getGroup(groupName)
        if (group == null) {
            context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
            return
        }
        
        val removed = group.nodes.removeIf { 
            it.key.startsWith("prefix.$priority.$prefix") ||
            it.key.startsWith("prefix_color.$priority.$prefix") ||
            it.key.startsWith("prefix_format.$priority.$prefix")
        }

        if (removed) {
            manager.saveData()
            context.sendMessage(MessageFactory.success("Removed prefix $prefix for group '$groupName' at priority $priority."))
        } else {
            context.sendMessage(MessageFactory.error("No prefix $prefix found for group '$groupName' at priority $priority."))
        }
    }
}