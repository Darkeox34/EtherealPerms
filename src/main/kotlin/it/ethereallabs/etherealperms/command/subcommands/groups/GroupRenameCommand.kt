package it.ethereallabs.etherealperms.command.subcommands.groups

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class GroupRenameCommand : CommandBase("rename", "etherealperms.command.group.rename.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)
    private val newNameArg = withRequiredArg("newname", "New name", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.rename")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val newName = newNameArg.get(context)
        val manager = EtherealPerms.permissionManager

        EtherealPerms.storage.storageScope.launch {
            try {
                val success = manager.renameGroup(groupName, newName)

                Universe.get().worlds.values.random().execute {
                    if (success) {
                        context.sendMessage(MessageFactory.success("Group renamed from '$groupName' to '$newName'."))
                    } else {
                        context.sendMessage(MessageFactory.error("Could not rename group. Check if it exists or if the new name is already taken."))
                    }
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("An error occurred during renaming: ${e.message}"))
                }
            }
        }
    }
}