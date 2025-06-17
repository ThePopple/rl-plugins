package com.popple.runecrafter

import com.google.inject.Provides
import com.popple.runecrafter.tasks.BankRunes
import com.popple.runecrafter.tasks.CraftRunes
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.events.ConfigButtonClicked
import net.runelite.api.events.GameTick
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.unethicalite.api.utils.MessageUtils
import org.pf4j.Extension
import java.util.logging.Logger
import javax.inject.Inject

@PluginDescriptor(
    name = "<html><font color=\"#ff0000\">Popple's runecrafter</font></html>",
    description = "What it says on the tin.",
    enabledByDefault = false
)
@Extension
class RunecrafterPlugin : Plugin() {
    @Inject
    lateinit var client: Client

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var config: RunecrafterConfig

    private val log: Logger = Logger.getLogger(name)

    @Provides
    fun getConfig(configManager: ConfigManager): RunecrafterConfig {
        return configManager.getConfig(RunecrafterConfig::class.java)
    }

    private val tasks = TaskSet()

    override fun startUp() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialising runecrafter :)")
        }
    }

    @Subscribe
    private fun onConfigButtonPressed(configButtonClicked: ConfigButtonClicked) {
        if (configButtonClicked.group.equals("popple-runecraft", ignoreCase = true)) {
            setRunning(!config.running())
            loadTasks()

            if (config.running()) {
                MessageUtils.addMessage("Started runecrafting.")
                log.info("----")
                log.info(tasks.toString())
                log.info("----")
            } else {
                MessageUtils.addMessage("Stopped runecrafting.")
            }
        }
    }

    @Subscribe
    private fun onGameTick(event: GameTick) {
        if (!config.running()) return

        val player = client.localPlayer
        if (player == null || client.gameState != GameState.LOGGED_IN) return

        val task = tasks.validTask

        if (task != null) {
            log.info(task.taskDescription)
            task.onGameTick(event)
        } else {
            log.info("No tasks.")
            log.info(tasks.size.toString())
        }
    }


    private fun loadTasks() {
        tasks.clear()
        tasks.addAll(
            injector.getInstance(BankRunes::class.java),
            injector.getInstance(CraftRunes::class.java)
        )
    }

    override fun shutDown() {
        log.info("$name stopped")
        setRunning(false)
    }

    private fun setRunning(running: Boolean) {
        configManager.setConfiguration(
            "popple-runecraft",
            "running",
            running
        )
    }
}