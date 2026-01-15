package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class GroupParentRemoveCommand : CommandBase("remove", "etherealperms.command.group.parent.remove.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val parentArg = withRequiredArg("parent", "Parent group to remove", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.parent.remove")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val parentName = parentArg.get(context)
        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        val parentNode = "group.$parentName"
        val removed = group.nodes.removeIf { it.key.equals(parentNode, ignoreCase = true) }
        manager.saveData()
        context.sendMessage(MessageFactory.success(if (removed) "Removed parent '$parentName'." else "Parent not found."))
    }
}