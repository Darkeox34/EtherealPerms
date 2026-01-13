package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory

class CreateGroupCommand : CommandBase("creategroup", "etherealperms.command.creategroup.desc") {

    private val groupNameArg = withRequiredArg("name", "The name of the group to create", ArgTypes.STRING)
    private val weightArg = withOptionalArg("weight", "Group weight", ArgTypes.INTEGER)
    private val displayNameArg = withOptionalArg("displayname", "Group display name", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.creategroup")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupNameArg.get(context)
        val weight = if (weightArg.provided(context)) weightArg.get(context) else 0
        val displayName = if (displayNameArg.provided(context)) displayNameArg.get(context) else null

        val manager = EtherealPerms.instance.permissionManager

        val group = manager.createGroup(groupName)
        group?.weight = weight
        group?.displayName = displayName
        manager.saveData()

        if (group != null) {
            context.sendMessage(MessageFactory.success("Group '$groupName' created successfully."))
        } else {
            context.sendMessage(MessageFactory.error("Group '$groupName' already exists."))
        }
    }
}