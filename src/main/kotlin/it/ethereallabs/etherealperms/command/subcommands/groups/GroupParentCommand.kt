package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class GroupParentCommand : AbstractCommandCollection("parent", "etherealperms.command.group.parent.desc") {
    init {
        addSubCommand(GroupParentAddCommand())
        addSubCommand(GroupParentRemoveCommand())
    }
}