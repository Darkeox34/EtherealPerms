package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import it.ethereallabs.etherealperms.commands.group.GroupInfoCommand

class GroupCommand : AbstractCommandCollection("group", "etherealperms.command.group.desc") {
    init {
        addSubCommand(GroupInfoCommand())
        addSubCommand(GroupPermissionCommand())
        addSubCommand(DeleteGroupCommand())
        addSubCommand(GroupParentCommand())
        addSubCommand(GroupMetaCommand())
        addSubCommand(GroupSetWeightCommand())
        addSubCommand(GroupRenameCommand())
    }
}