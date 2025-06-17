package com.popple.mole

import com.google.inject.Provides
import com.popple.mole.api.Task
import com.popple.mole.api.TaskSet
import com.popple.mole.tasks.FightMole
import com.popple.mole.tasks.FindMole
import com.popple.mole.tasks.Prayers
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.ConfigButtonClicked
import net.runelite.api.events.GameTick
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.overlay.OverlayManager
import net.unethicalite.api.utils.MessageUtils
import org.pf4j.Extension
import javax.inject.Inject


@PluginDescriptor(name = "Popple Mole")
@Extension
class PoppleMolePlugin : Plugin() {
    @Inject
    lateinit var config: PoppleMoleConfig

    @Inject
    lateinit var client: Client

    @Inject
    lateinit var overlayManager: OverlayManager

    @Inject
    lateinit var overlay: PoppleMoleOverlay

    private val tasks: TaskSet = TaskSet()

    private var running: Boolean = false;

    private var tickDelay: Int = 0;

    @Provides
    fun getConfig(configManager: ConfigManager): PoppleMoleConfig? {
        return configManager.getConfig(PoppleMoleConfig::class.java)
    }

    override fun startUp() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialised Mole bot.")
        }
    }

    override fun shutDown() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Disabled Mole bot.")
        }
    }

    private fun TaskSet.getPrayerTask(): Task {
        return taskList.first { it.taskDescription == "Prayers" }
    }

    @Subscribe
    private fun onGameTick(event: GameTick) {
        if (!running || client.localPlayer == null || client.gameState != GameState.LOGGED_IN) return

        val prayerTask: Task = tasks.getPrayerTask()

        if (prayerTask.validate()) {
            prayerTask.onGameTick(null)
        }

        if (delay()) return

        val validTasks: List<Task> = tasks.getValidTasks();

        if (config.debugMode()) {
            MessageUtils.addMessage("validTasks.size: ${validTasks.size}")
            MessageUtils.addMessage("validTasks[0].taskDescription: ${validTasks[0].taskDescription}")
        }

        if (validTasks.isEmpty()) {
            overlay.state = "Idle"
            return
        } else if (
            validTasks.size == 1 &&
            validTasks[0].taskDescription == "Prayers" &&
            !client.localPlayer.isAnimating
        ) {
            overlay.state = "Idle"
            return
        }

        validTasks.forEach {
            if (it.taskDescription != "Prayers") {
                if (config.debugMode()) MessageUtils.addMessage(it.taskDescription)
                tickDelay += it.onGameTick(event)
            }
        }
    }

    private fun delay(): Boolean {
        when (tickDelay) {
            0 -> return false
            -1 -> {
                running = false
                return true
            }

            else -> {
                tickDelay--
                return true
            }
        }
    }

    @Subscribe
    private fun onConfigButtonPressed(configButtonClicked: ConfigButtonClicked) {
        if (configButtonClicked.group.equals("popple-mole", ignoreCase = true)) {
            running = !running;

            if (running) {
                MessageUtils.addMessage("Mole bot started.")
                overlayManager.add(overlay)

                tasks.addAll(
                    injector.getInstance(Prayers::class.java),
                    injector.getInstance(FightMole::class.java),
                    injector.getInstance(FindMole::class.java),
                )
            } else {
                MessageUtils.addMessage("Mole bot stopped.")
                overlayManager.remove(overlay)
                tasks.clear()
                net.unethicalite.api.widgets.Prayers.disableAll()
            }
        }
    }

    @Subscribe
    private fun onActorDeath(event: ActorDeath) {
        if (config.debugMode()) MessageUtils.addMessage("Actor death: ${event.actor.name}")

        if (event.actor is NPC && event.actor.name == "Giant Mole") {
            overlay.killcount += 1
            client.clearHintArrow()
        }
    }
}
