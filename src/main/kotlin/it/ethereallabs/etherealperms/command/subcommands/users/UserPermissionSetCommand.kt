package it.ethereallabs.etherealperms.command.subcommands.users

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import it.ethereallabs.etherealperms.permissions.models.Node
import kotlinx.coroutines.launch

class UserPermissionSetCommand : CommandBase("set", "etherealperms.command.user.permission.set.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val nodeArg = withRequiredArg("node", "Permission node", ArgTypes.STRING)
    private val valueArg = withOptionalArg("value", "true or false", ArgTypes.BOOLEAN)
    private val durationArg = withOptionalArg("duration", "Duration (e.g. 1d2h or timestamp)", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.permission.set")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val nodeKey = nodeArg.get(context)
        val value = if (valueArg.provided(context)) valueArg.get(context) else true
        val durationInput = if (durationArg.provided(context)) durationArg.get(context) else null

        val expiry = if (durationInput != null) {
            it.ethereallabs.etherealperms.command.utils.CommandUtils.parseDuration(durationInput)
        } else null

        val manager = EtherealPerms.permissionManager

        EtherealPerms.storage.storageScope.launch {
            try {
                val user = manager.loadUser(player.uuid, player.username)

                user.nodes.removeIf { it.key.equals(nodeKey, ignoreCase = true) }
                user.nodes.add(Node(nodeKey, value, expiry))

                manager.saveData()

                Universe.get().worlds.values.random().execute {
                    val durationMsg = if (expiry != null) " for " + it.ethereallabs.etherealperms.command.utils.CommandUtils.formatRemainingTime(expiry) else ""
                    context.sendMessage(MessageFactory.success("Set permission '$nodeKey' to '$value'${durationMsg} for user '${player.username}'."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Failed to set permission for user: ${e.message}"))
                }
            }
        }
    }
}