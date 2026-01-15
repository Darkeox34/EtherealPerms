package it.ethereallabs.etherealperms.command.subcommands

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.universe.Universe
import it.ethereallabs.etherealperms.EtherealPerms
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import kotlinx.coroutines.launch

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
        EtherealPerms.storage.storageScope.launch {
            try {
                EtherealPerms.storage.sync()

                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Data synchronized from database."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Sync failed: ${e.message}"))
                }
                e.printStackTrace()
            }
        }
    }
}

private class SyncUploadCommand : CommandBase("upload", "etherealperms.command.sync.upload.desc") {

    init {
        requirePermission("etherealperms.sync.upload")
    }

    override fun executeSync(context: CommandContext) {
        EtherealPerms.storage.storageScope.launch {
            try {
                EtherealPerms.storage.syncUpload()

                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.success("Local data uploaded to database."))
                }
            } catch (e: Exception) {
                Universe.get().worlds.values.random().execute {
                    context.sendMessage(MessageFactory.error("Upload failed: ${e.message}"))
                }
                e.printStackTrace()
            }
        }
    }
}
