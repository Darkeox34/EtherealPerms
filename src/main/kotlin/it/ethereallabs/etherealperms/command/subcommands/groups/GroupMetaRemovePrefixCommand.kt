package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class GroupMetaRemovePrefixCommand : CommandBase("removeprefix", "etherealperms.command.group.meta.removeprefix.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withOptionalArg("prefix", "Prefix", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.removeprefix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context) // Ottieni il valore o null
        val manager = EtherealPerms.permissionManager

        val group = manager.getGroup(groupName)
        if (group == null) {
            context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
            return
        }

        val searchKey = if (prefix != null) {
            "prefix.$priority.$prefix"
        } else {
            "prefix.$priority."
        }

        val removed = group.nodes.removeIf { node ->
            if (prefix != null) {
                node.key == searchKey
            } else {
                node.key.startsWith(searchKey)
            }
        }

        if (removed) {
            manager.saveData()
            val msg = if (prefix != null) "prefix '$prefix'" else "all prefixes"
            context.sendMessage(MessageFactory.success("Removed $msg for group '$groupName' at priority $priority."))
        } else {
            context.sendMessage(MessageFactory.error("No matching prefix found for group '$groupName' at priority $priority."))
        }
    }
}