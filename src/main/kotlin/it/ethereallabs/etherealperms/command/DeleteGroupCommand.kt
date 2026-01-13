package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory

class DeleteGroupCommand : CommandBase("deletegroup", "etherealperms.command.deletegroup.desc") {

    private val groupNameArg = withRequiredArg("name", "The name of the group to delete", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.deletegroup")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupNameArg.get(context)
        val success = EtherealPerms.instance.permissionManager.deleteGroup(groupName)

        if (success) {
            context.sendMessage(MessageFactory.success("Group '$groupName' deleted."))
        } else {
            context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
        }
    }
}