package com.popple.hydra

import com.google.inject.Provides
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.ConfigButtonClicked
import net.runelite.api.events.GameTick
import net.runelite.api.events.NpcSpawned
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.overlay.OverlayManager
import net.unethicalite.api.entities.NPCs
import net.unethicalite.api.movement.Movement
import net.unethicalite.api.scene.Tiles
import net.unethicalite.api.utils.MessageUtils
import org.pf4j.Extension
import java.util.logging.Logger
import javax.inject.Inject
import kotlin.math.abs

@PluginDescriptor(name = "<html><font color=\"#12fd33\">Popple's Hydra bot</font></html>")
@Extension
class PoppleHydraPlugin : Plugin() {

    @Inject
    lateinit var config: PoppleHydraConfig

    @Inject
    lateinit var client: Client

    @Inject
    lateinit var overlayManager: OverlayManager

    @Inject
    lateinit var overlay: PoppleHydraOverlay

    private var running: Boolean = false

    override fun startUp() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialised Hydra bot.")
        }
    }

    override fun shutDown() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Disabled Hydra bot.")
            overlayManager.remove(overlay)
        }
    }

    @Subscribe
    private fun onGameTick(event: GameTick) {
        if (!running || client.localPlayer == null || client.gameState != GameState.LOGGED_IN) return
        val hydra = NPCs.getNearest("Alchemical Hydra") ?: return
        val hydraTiles = getHydraTiles(hydra)
        if (hydra.interacting == null) return

        if (client.localPlayer.worldLocation in hydraTiles) {
            moveToSafeTile()
        } else {
            hydra.interact("Attack")
        }
    }

    @Subscribe
    private fun onNPCSpawn(event: NpcSpawned) {
        if (event.actor.name.equals("Alchemical Hydra", ignoreCase = true) && running) {
            MessageUtils.addMessage("Fart")
            NPCs.getNearest("Alchemical Hydra").interact("Attack")
        }
    }

    private fun getHydraTiles(hydra: NPC): ArrayList<WorldPoint> {
        val size = hydra.composition.size + 8
        val startLoc = hydra.worldLocation.dx(-4).dy(-4)
        val unsafeTiles = arrayListOf(
            startLoc
        )

        for (x in 0 until size) {
            for (y in 0 until size) {
                unsafeTiles.add(startLoc.dx(x).dy(y))
            }
        }

        return unsafeTiles
    }

    private fun moveToSafeTile() {
        val closestSafeTile = findClosestSafeTile(client.localPlayer.worldLocation, 10)
        Movement.walk(closestSafeTile)
    }

    private fun findClosestSafeTile(startTile: WorldPoint, distance: Int): WorldPoint? {
        for (d in 1..distance) {
            val adjacentTiles = getAdjacentTiles(startTile, d)
            for (adjacentTile in adjacentTiles) {
                if (!isUnsafeTile(adjacentTile) && isTileWalkable(adjacentTile)) {
                    return adjacentTile
                }
            }
        }
        return null
    }

    private fun isUnsafeTile(tile: WorldPoint): Boolean {
        val hydraTiles = getHydraTiles(NPCs.getNearest("Alchemical Hydra"))

        if (tile in hydraTiles) {
            return true
        }

        return false
    }

    @Suppress("NAME_SHADOWING")
    private fun isTileWalkable(tile: WorldPoint): Boolean {
        val path = client.localPlayer.worldLocation.pathTo(client, tile)

        val t = Tiles.getAt(tile)
        val flags = client.collisionMaps!![client.plane].flags
        val movementFlags = MovementFlag.getSetFlags(flags[t.sceneLocation.x][t.sceneLocation.y])

        if (movementFlags.isNotEmpty()) return false

        for (it in path) {
            val t = Tiles.getAt(it)
            val flags = client.collisionMaps!![client.plane].flags
            val movementFlags = MovementFlag.getSetFlags(flags[t.sceneLocation.x][t.sceneLocation.y])

            if (movementFlags.isNotEmpty()) return false
        }



        return true
    }

    private fun getAdjacentTiles(tile: WorldPoint, distance: Int): List<WorldPoint> {
        val tiles = mutableListOf<WorldPoint>()
        for (dx in -distance..distance) {
            for (dy in -distance..distance) {
                if (abs(dx) + abs(dy) <= distance) {
                    tiles.add(tile.dx(dx).dy(dy))
                }
            }
        }
        return tiles
    }

    @Subscribe
    private fun onConfigButtonPressed(configButtonClicked: ConfigButtonClicked) {
        if (configButtonClicked.group.equals("popple-hydra", ignoreCase = true)) {
            running = !running;

            if (running) {
                MessageUtils.addMessage("Hydra bot started.")
                overlayManager.add(overlay)
            } else {
                MessageUtils.addMessage("Hydra bot stopped.")
                overlayManager.remove(overlay)
                net.unethicalite.api.widgets.Prayers.disableAll()
            }
        }
    }

    private val log = Logger.getLogger(name)

    @Provides
    fun getConfig(configManager: ConfigManager): PoppleHydraConfig? {
        return configManager.getConfig(PoppleHydraConfig::class.java)
    }
}