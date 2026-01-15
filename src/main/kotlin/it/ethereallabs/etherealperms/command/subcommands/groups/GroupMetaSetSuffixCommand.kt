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

    init {
        requirePermission("etherealperms.group.meta.setsuffix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val suffix = suffixArg.get(context)

        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        group.nodes.removeIf { it.key.startsWith("suffix.") || it.key.startsWith("suffix_color.") || it.key.startsWith("suffix_format.") }

        group.nodes.add(Node("suffix.$priority.$suffix"))

        manager.saveData()
        context.sendMessage(MessageFactory.success("Set suffix '$suffix' (prio: $priority) for group '${group.name}' (cleared old suffixes)."))
    }
}