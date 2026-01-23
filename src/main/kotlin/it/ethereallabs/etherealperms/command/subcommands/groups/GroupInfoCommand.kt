package it.ethereallabs.etherealperms.commands.group

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch
import java.awt.Color

class GroupInfoCommand : CommandBase("info", "etherealperms.command.group.info.desc") {

    private val groupArg = withRequiredArg("group", "Target group", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.group.info")
    }

    override fun executeSync(context: CommandContext) {
        val groupName = groupArg.get(context)
        val manager = EtherealPerms.permissionManager

        val group = manager.getGroup(groupName)

        if (group == null) {
            context.sendMessage(MessageFactory.error("Group not found."))
            return
        }
        EtherealPerms.storage.storageScope.launch {
            try {
                val members = manager.getUsersWithGroup(groupName)
                val count = members.size
                val displayMembers = members.take(10).joinToString(", ")
                val suffix = if (count > 10) " ... and ${count - 10} more" else ""
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.info("Group: ${group.name}"))
                    context.sendMessage(MessageFactory.info("Weight: ${group.weight}"))
                    context.sendMessage(MessageFactory.info("Nodes(${group.nodes.size}):"))

                    for (node in group.nodes) {
                        context.sendMessage(Message.raw("- ${node.key} ").color(Color.YELLOW).insert(Message.raw("(${node.value})").color(Color.CYAN)))
                    }

                    context.sendMessage(MessageFactory.info("Members ($count): $displayMembers$suffix"))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Error retrieving group members: ${e.message}"))
                }
            }
        }
    }
}