package it.ethereallabs.etherealperms.commands.group

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory

class GroupInfoCommand : CommandBase("info", "etherealperms.command.group.info.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.info")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val group = EtherealPerms.instance.permissionManager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        context.sendMessage(MessageFactory.info("Group: ${group.name}"))
        context.sendMessage(MessageFactory.info("Weight: ${group.weight}"))
        context.sendMessage(MessageFactory.info("Display Name: ${group.displayName ?: "None"}"))
        context.sendMessage(MessageFactory.info("Nodes: ${group.nodes.size}"))

        val members = EtherealPerms.instance.permissionManager.getUsersWithGroup(groupName)
        val count = members.size
        val displayMembers = members.take(10).joinToString(", ")
        val suffix = if (count > 10) " ... and ${count - 10} more" else ""

        context.sendMessage(MessageFactory.info("Members ($count): $displayMembers$suffix"))
    }
}