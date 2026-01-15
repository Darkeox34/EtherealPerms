package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch

class UserMetaAddPrefixCommand : CommandBase("addprefix", "etherealperms.command.user.meta.addprefix.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val priorityArg = withRequiredArg("priority", "Prefix priority", ArgTypes.INTEGER)
    private val prefixArg = withRequiredArg("prefix", "Prefix string", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.meta.addprefix")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val priority = priorityArg.get(context)
        val prefix = prefixArg.get(context)
        val manager = EtherealPerms.permissionManager

        EtherealPerms.storage.storageScope.launch {
            try {
                val user = manager.loadUser(player.uuid, player.username)

                user.nodes.add(Node("prefix.$priority.$prefix"))

                manager.saveData()

                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Added prefix '$prefix' with priority $priority to user '${player.username}'."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to add prefix to user: ${e.message}"))
                }
            }
        }
    }
}