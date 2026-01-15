package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class DeleteGroupCommand : CommandBase("deletegroup", "etherealperms.command.deletegroup.desc") {

    private val groupNameArg = withRequiredArg("name", "The name of the group to delete", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.deletegroup")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupNameArg.get(context)
        val manager = EtherealPerms.permissionManager
        EtherealPerms.storage.storageScope.launch {
            try {
                val success = manager.deleteGroup(groupName)
                if (success) {
                    manager.saveData()
                }
                Universe.get().worlds.values.random().execute {
                    if (success) {
                        context.sendMessage(MessageFactory.success("Group '$groupName' deleted successfully."))
                    } else {
                        context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
                    }
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("An error occurred while deleting the group: ${e.message}"))
                }
            }
        }
    }
}