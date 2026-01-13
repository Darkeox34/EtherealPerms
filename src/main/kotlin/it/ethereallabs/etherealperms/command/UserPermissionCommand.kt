package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class UserPermissionCommand : AbstractCommandCollection("permission", "etherealperms.command.user.permission.desc") {
    init {
        addSubCommand(UserPermissionSetCommand())
        addSubCommand(UserPermissionUnsetCommand())
    }
}