package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory

class GroupRenameCommand : CommandBase("rename", "etherealperms.command.group.rename.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val newNameArg = withRequiredArg("newname", "New name", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.rename")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val newName = newNameArg.get(context)

        if (EtherealPerms.instance.permissionManager.renameGroup(groupName, newName)) {
            context.sendMessage(MessageFactory.success("Group renamed from '$groupName' to '$newName'."))
        } else {
            context.sendMessage(MessageFactory.error("Could not rename group. Check if it exists or if new name is taken."))
        }
    }
}