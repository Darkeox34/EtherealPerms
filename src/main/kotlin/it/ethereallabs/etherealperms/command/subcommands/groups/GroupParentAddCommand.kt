package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch

class GroupParentAddCommand : CommandBase("add", "etherealperms.command.group.parent.add.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val parentArg = withRequiredArg("parent", "Parent group to add", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.parent.add")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val parentName = parentArg.get(context)
        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }
        EtherealPerms.storage.storageScope.launch {
            try {
                val parentNode = "group.$parentName"
                group.nodes.add(Node(parentNode))
                manager.saveData()
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(
                        MessageFactory.success("Added parent '$parentName' to group '${group.name}'.")
                    )
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to save inheritance: ${e.message}"))
                }
            }
        }
    }
}