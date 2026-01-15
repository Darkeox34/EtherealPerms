package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class GroupMetaRemoveSuffixCommand : CommandBase("removesuffix", "etherealperms.command.group.meta.removesuffix.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Suffix priority", ArgTypes.INTEGER)
    private val suffixArg = withOptionalArg("suffix", "Suffix", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.removesuffix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val suffix = suffixArg.get(context)
        val manager = EtherealPerms.permissionManager

        val group = manager.getGroup(groupName)
        if (group == null) {
            context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
            return
        }

        val searchKey = if (suffix != null) {
            "suffix.$priority.$suffix"
        } else {
            "suffix.$priority."
        }

        val removed = group.nodes.removeIf { node ->
            if (suffix != null) {
                node.key == searchKey
            } else {
                node.key.startsWith(searchKey)
            }
        }

        if (removed) {
            manager.saveData()
            val msg = if (suffix != null) "suffix '$suffix'" else "all suffixes"
            context.sendMessage(MessageFactory.success("Removed $msg for group '$groupName' at priority $priority."))
        } else {
            context.sendMessage(MessageFactory.error("No matching suffix found for group '$groupName' at priority $priority."))
        }
    }
}