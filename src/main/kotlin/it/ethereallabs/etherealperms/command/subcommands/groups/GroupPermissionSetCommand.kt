package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.permissions.models.Node
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class GroupPermissionSetCommand : CommandBase("set", "etherealperms.command.group.permission.set.desc") {

    private val groupArg = withRequiredArg("group", "The target group", ArgTypes.STRING)
    private val nodeArg = withRequiredArg("node", "The permission node", ArgTypes.STRING)
    private val valueArg = withOptionalArg("value", "true or false", ArgTypes.BOOLEAN)

    init {
        requirePermission("etherealperms.group.permission.set")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val nodeKey = nodeArg.get(context)
        val value = if (valueArg.provided(context)) valueArg.get(context) else true

        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group '$groupName' not found."))
            return
        }

        group.nodes.removeIf { it.key.equals(nodeKey, ignoreCase = true) }
        group.nodes.add(Node(nodeKey, value))

        manager.saveData()

        context.sendMessage(MessageFactory.success("Set permission '$nodeKey' to '$value' for group '${group.name}'."))
    }
}