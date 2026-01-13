package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

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