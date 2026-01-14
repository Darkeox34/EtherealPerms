package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node

class GroupMetaSetCommand : CommandBase("set", "etherealperms.command.group.meta.set.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val keyArg = withRequiredArg("key", "Meta key", ArgTypes.STRING)
    private val valueArg = withRequiredArg("value", "Meta value", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.set")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val key = keyArg.get(context)
        val value = valueArg.get(context)
        val manager = EtherealPerms.instance.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        group.nodes.add(Node("meta.$key.$value"))
        manager.saveData()
        context.sendMessage(MessageFactory.success("Meta '$key' set to '$value' for group '${group.name}'."))
    }
}