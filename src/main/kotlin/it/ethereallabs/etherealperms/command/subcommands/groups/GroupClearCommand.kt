package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class GroupClearCommand : CommandBase("clear", "etherealperms.command.group.clear.desc") {
    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.clear")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = context.get(groupArg)
        val manager = EtherealPerms.permissionManager

        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
            return
        }

        EtherealPerms.storage.storageScope.launch {
            try {
                group.nodes.clear()
                manager.saveData()
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Cleared all nodes for group '${group.name}'."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to save group data: ${e.message}"))
                }
            }
        }
    }
}