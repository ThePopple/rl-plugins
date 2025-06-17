package com.popple.hydra

import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.Perspective
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayPosition
import java.awt.*
import javax.inject.Inject


class PoppleHydraOverlay @Inject constructor(
    private val client: Client
) : Overlay() {

    init {
        position = OverlayPosition.DYNAMIC
    }

    override fun render(graphics: Graphics2D): Dimension? {
        for (npc in client.npcs) {
            // Replace with the condition to find your specific NPC
            if (npc.name.equals("Alchemical Hydra", ignoreCase = true)) {
                renderPoly(graphics, Color.RED, Color.RED, 100, 25, getPolygon(npc, npc.composition.size + 8), 1.0, true)
            }
        }
        return null
    }

    private fun getPolygon(npc: NPC, size: Int): Polygon? {
        var lp = LocalPoint.fromWorld(client, WorldPoint(npc.worldLocation.x - 4, npc.worldLocation.y - 4, npc.worldLocation.plane))

        if (lp != null) {
            lp = LocalPoint(lp.x + size * 128 / 2 - 64, lp.y + size * 128 / 2 - 64)
            return Perspective.getCanvasTileAreaPoly(client, lp, size)
        }

        return null
    }

    private fun renderPoly(
        graphics: Graphics2D,
        outlineColor: Color,
        fillColor: Color,
        lineAlpha: Int,
        fillAlpha: Int,
        polygon: Shape?,
        width: Double,
        antiAlias: Boolean
    ) {
        if (polygon != null) {
            graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                if (antiAlias) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
            )
            graphics.color = Color(outlineColor.red, outlineColor.green, outlineColor.blue, lineAlpha)
            graphics.stroke = BasicStroke(width.toFloat())
            graphics.draw(polygon)
            graphics.color = Color(fillColor.red, fillColor.green, fillColor.blue, fillAlpha)
            graphics.fill(polygon)
        }
    }
}
