package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.data.MessageFactory
import it.ethereallabs.etherealperms.data.Node

class UserParentAddCommand : CommandBase("add", "etherealperms.command.user.parent.add.desc") {

    private val playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF)
    private val groupArg = withRequiredArg("group", "Group to add", ArgTypes.STRING)

    init {
        requirePermission("etherealperms.user.parent.add")
    }

    override fun executeSync(context: CommandContext) {
        val player = playerArg.get(context)
        val groupName = groupArg.get(context)
        val manager = EtherealPerms.instance.permissionManager
        
        if (manager.getGroup(groupName) == null) {
            context.sendMessage(MessageFactory.error("Group '$groupName' does not exist."))
            return
        }

        val user = manager.loadUser(player.uuid, player.username)
        val parentNode = "group.$groupName"
        
        if (user.nodes.any { it.key.equals(parentNode, ignoreCase = true) }) {
             context.sendMessage(MessageFactory.error("User already has parent '$groupName'."))
             return
        }

        user.nodes.add(Node(parentNode))
        manager.saveData()
        context.sendMessage(MessageFactory.success("Added parent '$groupName' to user '${player.username}'."))
    }
}