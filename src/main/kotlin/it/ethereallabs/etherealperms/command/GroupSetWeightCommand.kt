package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory

class GroupSetWeightCommand : CommandBase("setweight", "etherealperms.command.group.setweight.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val weightArg = withRequiredArg("weight", "New weight", ArgTypes.INTEGER)

    init {
        requirePermission("etherealperms.group.setweight")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val weight = weightArg.get(context)
        val manager = EtherealPerms.instance.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        group.weight = weight
        manager.saveData()
        context.sendMessage(MessageFactory.success("Weight for group '${group.name}' set to $weight."))
    }
}