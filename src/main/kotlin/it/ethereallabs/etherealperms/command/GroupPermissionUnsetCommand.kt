package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory

class GroupPermissionUnsetCommand : CommandBase("unset", "etherealperms.command.group.permission.unset.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val nodeArg = withRequiredArg("node", "Permission node", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.permission.unset")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val nodeKey = nodeArg.get(context)
        val manager = EtherealPerms.instance.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        val removed = group.nodes.removeIf { it.key.equals(nodeKey, ignoreCase = true) }
        manager.saveData()
        context.sendMessage(MessageFactory.success(if (removed) "Permission '$nodeKey' unset." else "Permission not found."))
    }
}