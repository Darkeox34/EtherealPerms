package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class ListGroupsCommand : CommandBase("listgroups", "etherealperms.command.listgroups.desc") {

    init {
        requirePermission("etherealperms.listgroups")
    }

    override fun executeSync(context: CommandContext) {
        val groups = EtherealPerms.permissionManager.getAllGroups()
        val groupNames = groups.joinToString(", ") { it.name }

        context.sendMessage(MessageFactory.info("Groups (${groups.size}): $groupNames"))
    }
}