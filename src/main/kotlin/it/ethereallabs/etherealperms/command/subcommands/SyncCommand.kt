package it.ethereallabs.etherealperms.command.subcommands

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory

class SyncCommand : AbstractCommandCollection("sync", "etherealperms.command.sync.desc") {

    init {
        requirePermission("etherealperms.sync")
        addSubCommand(SyncRunCommand())
        addSubCommand(SyncUploadCommand())
    }
}

private class SyncRunCommand : CommandBase("run", "etherealperms.command.sync.run.desc") {
    init {
        requirePermission("etherealperms.sync.run")
    }

    override fun executeSync(context: CommandContext) {
        EtherealPerms.storage.sync()
        context.sendMessage(MessageFactory.success("Data synchronized from database."))
    }
}

private class SyncUploadCommand : CommandBase("upload", "etherealperms.command.sync.upload.desc") {

    init {
        requirePermission("etherealperms.sync.upload")
    }

    override fun executeSync(context: CommandContext) {
        EtherealPerms.storage.syncUpload()
        context.sendMessage(MessageFactory.success("Local data uploaded to database."))
    }
}
