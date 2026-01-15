package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch

class GroupMetaAddSuffixCommand : CommandBase("addsuffix", "etherealperms.command.group.meta.addsuffix.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val priorityArg = withRequiredArg("priority", "Suffix priority", ArgTypes.INTEGER)
    private val suffixArg = withRequiredArg("suffix", "Suffix string", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.meta.addsuffix")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val priority = priorityArg.get(context)
        val suffix = suffixArg.get(context)
        val manager = EtherealPerms.permissionManager

        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        EtherealPerms.storage.storageScope.launch {
            try {
                group.nodes.add(Node("suffix.$priority.$suffix"))
                manager.saveData()
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(
                        MessageFactory.success("Added suffix '$suffix' with priority $priority to group '${group.name}'.")
                    )
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to save suffix: ${e.message}"))
                }
            }
        }
    }
}