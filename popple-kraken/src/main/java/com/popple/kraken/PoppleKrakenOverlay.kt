package com.popple.kraken

import net.runelite.api.Client
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.PanelComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PoppleKrakenOverlay @Inject constructor(
    private val client: Client,
    private val plugin: PoppleKrakenPlugin,
    private val config: PoppleKrakenConfig
) : Overlay() {

    @Inject
    private lateinit var state: State

    private val panelComponent = PanelComponent()

    init {
        position = OverlayPosition.BOTTOM_LEFT
        panelComponent.preferredSize = Dimension(200, 100)
    }

    override fun render(graphics: Graphics2D): Dimension? {
        val lines: List<LayoutableRenderableEntity> = listOf(
            TitleComponent.builder()
                .color(Color.RED)
                .text("Popple's Kraken bot")
                .build(),
            LineComponent.builder()
                .left("State:")
                .right(state.stateString)
                .build(),
            LineComponent.builder()
                .left("Kills:")
                .right(state.killCount.toString())
                .build(),
        )

        val debugLines: List<LayoutableRenderableEntity?> = listOf(
            LineComponent.builder()
                .left("poolsDone:")
                .right(state.poolsDone.toString())
                .build(),
            LineComponent.builder()
                .left("Interacting with:")
                .right((client.localPlayer.interacting ?: "null").toString())
                .build(),
            if (client.localPlayer.interacting != null) {
                LineComponent.builder()
                    .left("Interacting with tag:")
                    .right(client.localPlayer.interacting.tag.toString())
                    .build()
            } else {LineComponent.builder().build()},
            if (client.localPlayer.interacting != null) {
                LineComponent.builder()
                    .left("Interacting with hash:")
                    .right(client.localPlayer.interacting.hash.toString())
                    .build()
            } else {LineComponent.builder().build()},
        )
        panelComponent.children.clear()

        lines.forEach { panelComponent.children.add(it) }
        if (config.debugMode()) debugLines.forEach { panelComponent.children.add(it) }

        return panelComponent.render(graphics)
    }
}
