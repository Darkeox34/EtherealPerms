package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class UserGroupCommand : AbstractCommandCollection("group", "etherealperms.command.user.group.desc") {
    init {
        addSubCommand(UserGroupAddCommand())
        addSubCommand(UserGroupRemoveCommand())
        addSubCommand(UserGroupSetCommand())
    }
}