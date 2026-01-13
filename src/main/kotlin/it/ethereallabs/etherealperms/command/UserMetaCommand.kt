package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class UserMetaCommand : AbstractCommandCollection("meta", "etherealperms.command.user.meta.desc") {
    init {
        addSubCommand(UserMetaSetCommand())
        addSubCommand(UserMetaAddPrefixCommand())
    }
}