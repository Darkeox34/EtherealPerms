package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import it.ethereallabs.etherealperms.command.UserCommand

class EtherealPermsCommand : AbstractCommandCollection("ep", "etherealperms.command.ep.desc") {
    init {
        requirePermission("etherealperms.command.base")
        addAliases("eperms", "etherealperms")

        addSubCommand(UserCommand())
        addSubCommand(GroupCommand())
        addSubCommand(ListGroupsCommand())
        addSubCommand(CreateGroupCommand())

    }
}