package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class GroupPermissionUnsetCommand : CommandBase("unset", "etherealperms.command.group.permission.unset.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val nodeArg = withRequiredArg("node", "Permission node", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.permission.unset")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val nodeKey = nodeArg.get(context)
        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        EtherealPerms.storage.storageScope.launch {
            try {
                val removed = group.nodes.removeIf { it.key.equals(nodeKey, ignoreCase = true) }

                if (removed) {
                    manager.saveData()
                }

                Universe.get().worlds.values.random().execute {
                    if (removed) {
                        context.sendMessage(MessageFactory.success("Permission '$nodeKey' unset for group '${group.name}'."))
                    } else {
                        context.sendMessage(MessageFactory.error("Permission '$nodeKey' not found in group '${group.name}'."))
                    }
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to unset permission: ${e.message}"))
                }
            }
        }
    }
}