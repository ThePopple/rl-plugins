package com.popple.kraken

import com.google.inject.Provides
import com.popple.kraken.api.Task
import com.popple.kraken.api.TaskSet
import com.popple.kraken.tasks.FightKraken
import com.popple.kraken.tasks.Pools
import com.popple.kraken.tasks.Prayers
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.NpcID
import net.runelite.api.events.*
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.overlay.OverlayManager
import net.unethicalite.api.entities.NPCs
import net.unethicalite.api.utils.MessageUtils
import org.pf4j.Extension
import java.util.logging.Logger
import javax.inject.Inject

@PluginDescriptor(name = "Popple Kraken")
@Extension
class PoppleKrakenPlugin : Plugin() {

    @Inject
    private lateinit var state: State

    @Inject
    lateinit var config: PoppleKrakenConfig

    @Inject
    lateinit var client: Client

    @Inject
    lateinit var overlayManager: OverlayManager

    @Inject
    lateinit var overlay: PoppleKrakenOverlay

    private val tasks: TaskSet = TaskSet()

    private var running: Boolean = false;

    private var tickDelay: Int = 0;

    override fun startUp() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialised Kraken bot.")
            state.reset()
        }
    }

    override fun shutDown() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Disabled Kraken bot.")
            state.reset()
        }
    }

    private val log = Logger.getLogger(name)

    @Provides
    fun getConfig(configManager: ConfigManager): PoppleKrakenConfig? {
        return configManager.getConfig(PoppleKrakenConfig::class.java)
    }

    @Subscribe
    private fun onGameTick(event: GameTick) {
        if (!running || client.localPlayer == null || client.gameState != GameState.LOGGED_IN) return

        val validTasks: List<Task> = tasks.getValidTasks();
        if (delay(validTasks)) return

        if (validTasks.isEmpty() && NPCs.getNearest(NpcID.KRAKEN) == null) {
            state.stateString = "Idle"
        } else if (
            validTasks.size == 1 &&
            validTasks[0].taskDescription == "Prayers" &&
            !client.localPlayer.isAnimating &&
            NPCs.getNearest(NpcID.KRAKEN) == null
        ) {
            state.stateString = "Idle"
        }

        validTasks.forEach {
            tickDelay += it.onGameTick(event)
        }
    }

    @Subscribe
    private fun onActorDeath(event: ActorDeath) {
        if (config.debugMode()) MessageUtils.addMessage("Actor death: ${event.actor.name}")

        if (event.actor is NPC && event.actor.name == "Kraken") {
            state.poolsDone = false
            state.killCount += 1
        }
    }

    @Subscribe
    private fun onNpcSpawn(event: NpcSpawned) {
        if (config.debugMode()) MessageUtils.addMessage("NPC spawn: ${event.actor.name}")

        if (event.actor is NPC && event.actor.name == "Whirlpool") {
            state.poolsDone = false
        }
    }

    @Subscribe
    private fun onConfigButtonPressed(configButtonClicked: ConfigButtonClicked) {
        if (configButtonClicked.group.equals("popple-kraken", ignoreCase = true)) {
            running = !running;

            if (running) {
                MessageUtils.addMessage("Kraken bot started.")
                overlayManager.add(overlay)

                tasks.addAll(
                    injector.getInstance(Prayers::class.java),
                    injector.getInstance(Pools::class.java),
                    injector.getInstance(FightKraken::class.java),
                )
            } else {
                MessageUtils.addMessage("Kraken bot stopped.")
                overlayManager.remove(overlay)
                tasks.clear()
                net.unethicalite.api.widgets.Prayers.disableAll()
            }
        }
    }

    @Subscribe
    private fun onChatMessage(message: ChatMessage) {
        if (message.message.contains("There was no response", ignoreCase = true)) {
            state.poolsDone = false
        }
    }

    private fun delay(validTasks: List<Task>): Boolean {
        when (tickDelay) {
            0 -> return false
            -1 -> {
                running = false
                return true
            }

            else -> {
                val prayerTask: Task = validTasks.first {
                    it.taskDescription == "Prayers"
                }

                if (prayerTask.validate()) {
                    prayerTask.onGameTick(null)
                }

                tickDelay--
                return true
            }
        }
    }
}