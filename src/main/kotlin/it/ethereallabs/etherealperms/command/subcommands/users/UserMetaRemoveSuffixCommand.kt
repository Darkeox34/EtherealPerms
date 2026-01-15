package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class UserMetaRemoveSuffixCommand : CommandBase("removesuffix", "etherealperms.command.user.meta.removesuffix.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Suffix priority", ArgTypes.INTEGER)
    private val suffixArg = withOptionalArg("suffix", "Suffix", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.removesuffix")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val suffix = suffixArg.get(context)
        val manager = EtherealPerms.permissionManager

        val user = manager.loadUser(player.uuid, player.username)

        val searchKey = if (suffix != null) {
            "suffix.$priority.$suffix"
        } else {
            "suffix.$priority."
        }

        val removed = user.nodes.removeIf { node ->
            if (suffix != null) {
                node.key == searchKey
            } else {
                node.key.startsWith(searchKey)
            }
        }

        if (removed) {
            manager.saveData()
            val msg = if (suffix != null) "suffix '$suffix'" else "all suffixes"
            context.sendMessage(MessageFactory.success("Removed $msg for user '${user.username}' at priority $priority."))
        } else {
            context.sendMessage(MessageFactory.error("No matching suffix found for user '${user.username}' at priority $priority."))
        }
    }
}