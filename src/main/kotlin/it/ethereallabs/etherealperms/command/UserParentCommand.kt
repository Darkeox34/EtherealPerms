package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class UserParentCommand : AbstractCommandCollection("parent", "etherealperms.command.user.parent.desc") {
    init {
        addSubCommand(UserParentAddCommand())
        addSubCommand(UserParentRemoveCommand())
    }
}