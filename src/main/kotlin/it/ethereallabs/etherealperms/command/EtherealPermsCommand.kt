package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import it.ethereallabs.etherealperms.command.subcommands.groups.CreateGroupCommand
import it.ethereallabs.etherealperms.command.subcommands.groups.GroupCommand
import it.ethereallabs.etherealperms.command.subcommands.groups.ListGroupsCommand
import it.ethereallabs.etherealperms.command.subcommands.users.UserCommand

class EtherealPermsCommand : AbstractCommandCollection("ep", "etherealperms.command.ep.desc") {
    init {
        requirePermission("etherealperms.command.base")
        addAliases("eperms", "etherealperms")

        addSubCommand(UserCommand())
        addSubCommand(GroupCommand())
        addSubCommand(ListGroupsCommand())
        addSubCommand(CreateGroupCommand())
        addSubCommand(ReloadConfigCommand())
    }
}