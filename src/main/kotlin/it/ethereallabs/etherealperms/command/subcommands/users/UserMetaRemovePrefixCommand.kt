package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class UserMetaRemovePrefixCommand : CommandBase("removeprefix", "etherealperms.command.user.meta.removeprefix.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withRequiredArg("prefix", "Prefix", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.removeprefix")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context)
        val manager = EtherealPerms.permissionManager

        val user = manager.loadUser(player.uuid, player.username)

        val removed = user.nodes.removeIf {
            it.key.startsWith("prefix.$priority.$prefix") ||
                    it.key.startsWith("prefix_color.$priority.$prefix") ||
                    it.key.startsWith("prefix_format.$priority.$prefix")
        }

        if (removed) {
            manager.saveData()
            context.sendMessage(MessageFactory.success("Removed prefix $prefix for user '${player.username}' at priority $priority."))
        } else {
            context.sendMessage(MessageFactory.error("No prefix found $prefix for user '${player.username}' at priority $priority."))
        }
    }
}