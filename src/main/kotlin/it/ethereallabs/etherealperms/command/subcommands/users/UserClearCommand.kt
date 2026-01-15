package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class UserClearCommand : CommandBase("clear", "etherealperms.command.user.clear.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)

    init {
        requirePermission("etherealperms.user.clear")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val manager = EtherealPerms.permissionManager

        EtherealPerms.storage.storageScope.launch {
            try {
                val user = manager.loadUser(player.uuid, player.username)

                user.nodes.clear()
                manager.saveData()

                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Cleared all nodes for user '${player.username}'."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to clear nodes for user '${player.username}': ${e.message}"))
                }
            }
        }
    }
}