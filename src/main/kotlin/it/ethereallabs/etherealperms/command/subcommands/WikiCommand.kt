package it.ethereallabs.etherealperms.command.subcommands

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase
import com.hypixel.hytale.server.core.entity.entities.Player
import it.ethereallabs.etherealperms.command.utils.MessageFactory
import java.awt.Color

class WikiCommand : CommandBase("wiki", "etherealperms.command.wiki.desc") {

    init {
        requirePermission("etherealperms.wiki")
    }

    override fun executeSync(context: CommandContext) {
        context.sendMessage(
            MessageFactory.success("Click here to visit our Wiki").link("https://ethereallabs.it/etherealperms-wiki").color(
            Color.GREEN))
    }
}