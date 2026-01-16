package de.noel.testui.tutorial.level3

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import it.ethereallabs.etherealperms.ui.MainPage


class EditorCommand : AbstractPlayerCommand(
    "editor",
    "Opens User Interface",
    false
) {

    override fun execute(
        ctx: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World
    ) {
        val player = store.getComponent(ref, Player.getComponentType())

        /*val page = MainPage(
            playerRef = playerRef,
            playersOnline = 42,
            questCount = 7,
            uptime = "3h 24m"
        )*/

        val page = MainPage(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction)

        player?.pageManager?.openCustomPage(ref, store, page)
    }
}