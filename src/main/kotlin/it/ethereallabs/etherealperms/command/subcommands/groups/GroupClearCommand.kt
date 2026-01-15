package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class GroupClearCommand : CommandBase("clear", "etherealperms.command.group.clear.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.clear")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = context.get(groupArg)
        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        group?.nodes?.clear()
        manager.saveData()
        context.sendMessage(MessageFactory.success("Cleared all nodes for group '${group?.name}'."))
    }
}