package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory
import it.ethereallabs.etherealperms.data.Node

class GroupParentAddCommand : CommandBase("add", "etherealperms.command.group.parent.add.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val parentArg = withRequiredArg("parent", "Parent group to add", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.parent.add")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val parentName = parentArg.get(context)
        val manager = EtherealPerms.instance.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        val parentNode = "group.$parentName"
        group.nodes.add(Node(parentNode))
        manager.saveData()
        context.sendMessage(MessageFactory.success("Added parent '$parentName' to group '${group.name}'."))
    }
}