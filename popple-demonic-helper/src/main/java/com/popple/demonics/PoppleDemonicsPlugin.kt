package com.popple.demonics

import com.google.inject.Provides
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.HeadIcon
import net.runelite.api.NPC
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.ConfigButtonClicked
import net.runelite.api.events.GameTick
import net.runelite.api.events.GraphicsObjectCreated
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.unethicalite.api.items.Inventory
import net.unethicalite.api.movement.Movement
import net.unethicalite.api.utils.MessageUtils
import org.pf4j.Extension
import java.util.logging.Logger
import javax.inject.Inject


@PluginDescriptor(name = "Popple's Demonic helper")
@Extension
class PoppleDemonicsPlugin : Plugin() {

    @Inject
    lateinit var config: PoppleDemonicsConfig

    @Inject
    lateinit var client: Client

    private var running: Boolean = false;

    private val log = Logger.getLogger(name)

    override fun startUp() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialised Demonic Gorilla helper")
        }
    }

    override fun shutDown() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Disabled Demonic Gorilla helper")
        }
    }

    @Provides
    fun getConfig(configManager: ConfigManager): PoppleDemonicsConfig? {
        return configManager.getConfig(PoppleDemonicsConfig::class.java)
    }

    @Subscribe
    private fun onConfigButtonPressed(configButtonClicked: ConfigButtonClicked) {
        if (configButtonClicked.group.equals("popple-demonic-helper", ignoreCase = true)) {
            running = !running;

            if (running) {
                MessageUtils.addMessage("Demonic Gorilla helper started.")
            } else {
                MessageUtils.addMessage("Demonic Gorilla helper stopped.")
            }
        }
    }

    @Subscribe
    private fun onGameTick(event: GameTick) {
        val localPlayer = client.localPlayer
        if (!localPlayer.interacting?.name.equals("Demonic Gorilla", ignoreCase = true)) return


        when ((localPlayer.interacting as NPC).composition.overheadIcon) {
            HeadIcon.MELEE -> {
                if (config.primaryWeaponStyle() != CmbStyle.MELEE) {
                    Inventory.getFirst(config.primaryWeapon()).interact("Wield")
                } else {
                    Inventory.getFirst(config.secondaryWeapon()).interact("Wield")
                }
            }

            HeadIcon.RANGED -> {
                if (config.primaryWeaponStyle() != CmbStyle.RANGED) {
                    Inventory.getFirst(config.primaryWeapon()).interact("Wield")
                } else {
                    Inventory.getFirst(config.secondaryWeapon()).interact("Wield")
                }
            }

            HeadIcon.MAGIC -> {
                if (config.primaryWeaponStyle() != CmbStyle.MAGIC) {
                    Inventory.getFirst(config.primaryWeapon()).interact("Wield")
                } else {
                    Inventory.getFirst(config.secondaryWeapon()).interact("Wield")
                }
            }

            else -> {}
        }


    }

    @Subscribe
    private fun onGraphicsObjectCreated(event: GraphicsObjectCreated) {
        if (event.graphicsObject.id != 305) return

        if (event.graphicsObject.location == client.localPlayer.localLocation) {
            Movement.walkTo(getFirstAdjacentEmptyTile(client.localPlayer.worldLocation))
        }

    }

    private fun getFirstAdjacentEmptyTile(targetTile: WorldPoint): WorldPoint? {
        val worldArea = WorldArea(targetTile, 1, 1)

        val directions = arrayOf(
            intArrayOf(0, 1),   // North
            intArrayOf(1, 0),   // East
            intArrayOf(0, -1),  // South
            intArrayOf(-1, 0)   // West
        )

        for (direction in directions) {
            val dx = direction[0]
            val dy = direction[1]
            val adjacentTile = targetTile.dx(dx).dy(dy)

            if (worldArea.canTravelInDirection(client.topLevelWorldView, dx, dy) { isTileEmptyOfNPC(adjacentTile) }) {
                return adjacentTile
            }
        }

        return null // No empty adjacent tile found
    }

    private fun isTileEmptyOfNPC(tile: WorldPoint): Boolean {
        // Get all NPCs in the scene
        for (npc in client.npcs) {
            if (npc.worldLocation == tile) {
                return false // Tile is occupied by an NPC
            }
        }
        return true // No NPC occupies this tile
    }
}