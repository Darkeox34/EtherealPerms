package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class ReloadConfigCommand : CommandBase("reloadconfig", "etherealperms.command.reloadconfig.desc") {

    init {
        requirePermission("etherealperms.reloadconfig")
    }

    override fun executeSync(context: CommandContext) {
        EtherealPerms.instance.permissionManager.loadData()
        context.sendMessage(MessageFactory.success("Configuration and data reloaded."))
    }
}