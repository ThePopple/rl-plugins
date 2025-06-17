package com.popple.mole

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
class PoppleMoleOverlay @Inject constructor(private val client: Client, private val plugin: PoppleMolePlugin, private val config: PoppleMoleConfig) : Overlay() {
    private val panelComponent = PanelComponent()
    var state: String = "test"
    var killcount: Int = 0

    init {
        position = OverlayPosition.BOTTOM_LEFT
        panelComponent.preferredSize = Dimension(200, 100)
    }

    override fun render(graphics: Graphics2D): Dimension? {
        val lines: List<LayoutableRenderableEntity> = listOf(
            TitleComponent.builder()
                .color(Color.RED)
                .text("Popple's Giant Mole bot")
                .build(),
            LineComponent.builder()
                .left("State:")
                .right(state)
                .build(),
            LineComponent.builder()
                .left("Kills:")
                .right(killcount.toString())
                .build(),
        )

        panelComponent.children.clear()
        lines.forEach { panelComponent.children.add(it) }

        return panelComponent.render(graphics)
    }
}
