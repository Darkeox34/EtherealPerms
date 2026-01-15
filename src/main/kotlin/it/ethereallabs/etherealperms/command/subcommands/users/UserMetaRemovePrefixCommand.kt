package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class UserMetaRemovePrefixCommand : CommandBase("removeprefix", "etherealperms.command.user.meta.removeprefix.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withOptionalArg("prefix", "Prefix", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.removeprefix")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context)
        val manager = EtherealPerms.permissionManager

        val user = manager.loadUser(player.uuid, player.username)

        val searchKey = if (prefix != null) {
            "prefix.$priority.$prefix"
        } else {
            "prefix.$priority."
        }

        val removed = user.nodes.removeIf { node ->
            if (prefix != null) {
                node.key == searchKey
            } else {
                node.key.startsWith(searchKey)
            }
        }

        if (removed) {
            manager.saveData()
            val msg = if (prefix != null) "prefix '$prefix'" else "all prefixes"
            context.sendMessage(MessageFactory.success("Removed $msg for user '${user.username}' at priority $priority."))
        } else {
            context.sendMessage(MessageFactory.error("No matching prefix found for user '${user.username}' at priority $priority."))
        }
    }
}