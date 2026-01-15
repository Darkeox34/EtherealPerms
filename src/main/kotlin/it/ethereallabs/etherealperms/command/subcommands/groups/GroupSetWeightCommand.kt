package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class GroupSetWeightCommand : CommandBase("setweight", "etherealperms.command.group.setweight.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val weightArg = withRequiredArg("weight", "New weight", ArgTypes.INTEGER)

    init {
        requirePermission("etherealperms.group.setweight")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val weight = weightArg.get(context)
        val manager = EtherealPerms.permissionManager
        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }

        EtherealPerms.storage.storageScope.launch {
            try {
                group.weight = weight
                manager.saveData()

                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Weight for group '${group.name}' set to $weight."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to save weight: ${e.message}"))
                }
            }
        }
    }
}