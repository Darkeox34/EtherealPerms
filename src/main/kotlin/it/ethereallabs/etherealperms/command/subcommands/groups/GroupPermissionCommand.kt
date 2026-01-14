package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class GroupPermissionCommand : AbstractCommandCollection("permission", "etherealperms.command.group.permission.desc") {
    init {
        addSubCommand(GroupPermissionSetCommand())
        addSubCommand(GroupPermissionUnsetCommand())
    }
}