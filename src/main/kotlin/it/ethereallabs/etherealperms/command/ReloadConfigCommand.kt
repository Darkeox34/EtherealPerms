package it.ethereallabs.etherealperms.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.EtherealPerms.Companion.storage
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

class ReloadConfigCommand : CommandBase("reloadconfig", "etherealperms.command.reloadconfig.desc") {

    init {
        requirePermission("etherealperms.reloadconfig")
    }

    override fun executeSync(context: CommandContext) {
        storage.storageScope.launch {
            EtherealPerms.permissionManager.reloadData()
        }
        storage.reloadConfigs()
        context.sendMessage(MessageFactory.success("Configuration and data reloaded."))
    }
}