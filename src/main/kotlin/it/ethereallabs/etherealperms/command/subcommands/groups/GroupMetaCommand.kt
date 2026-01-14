package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection

class GroupMetaCommand : AbstractCommandCollection("meta", "etherealperms.command.group.meta.desc") {
    init {
        addSubCommand(GroupMetaSetCommand())
        addSubCommand(GroupMetaAddPrefixCommand())
        addSubCommand(GroupMetaSetPrefixCommand())
        addSubCommand(GroupMetaSetPlayerColorCommand())
        addSubCommand(GroupMetaSetChatColorCommand())
        addSubCommand(GroupMetaAddSuffixCommand())
        addSubCommand(GroupMetaSetSuffixCommand())
        addSubCommand(GroupMetaRemovePrefixCommand())
        addSubCommand(GroupMetaRemoveSuffixCommand())
    }
}