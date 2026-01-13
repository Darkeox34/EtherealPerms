package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class UserCommand : AbstractCommandCollection("user", "etherealperms.command.user.desc") {
    init {
        addSubCommand(UserInfoCommand())
        addSubCommand(UserPermissionCommand())
        addSubCommand(UserParentCommand())
        addSubCommand(UserMetaCommand())
        addSubCommand(UserClearCommand())
        addSubCommand(UserCloneCommand())
    }
}