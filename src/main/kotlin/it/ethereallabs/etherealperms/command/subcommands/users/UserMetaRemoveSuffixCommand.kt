package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class UserMetaRemoveSuffixCommand : CommandBase("removesuffix", "etherealperms.command.user.meta.removesuffix.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Suffix priority", ArgTypes.INTEGER)
    private val suffixArg = withRequiredArg("suffix", "Suffix", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.removesuffix")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val suffix = suffixArg.get(context)
        val manager = EtherealPerms.instance.permissionManager

        val user = manager.loadUser(player.uuid, player.username)
        
        val removed = user.nodes.removeIf { 
            it.key.startsWith("suffix.$priority.$suffix") ||
            it.key.startsWith("suffix_color.$priority.$suffix") ||
            it.key.startsWith("suffix_format.$priority.$suffix")
        }

        if (removed) {
            manager.saveData()
            context.sendMessage(MessageFactory.success("Removed suffix $suffix for user '${player.username}' at priority $priority."))
        } else {
            context.sendMessage(MessageFactory.error("No suffix found $suffix for user '${player.username}' at priority $priority."))
        }
    }
}