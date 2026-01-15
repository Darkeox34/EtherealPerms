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
        EtherealPerms.permissionManager.reloadData()
        EtherealPerms.storage.reloadConfigs()
        context.sendMessage(MessageFactory.success("Configuration and data reloaded."))
    }
}