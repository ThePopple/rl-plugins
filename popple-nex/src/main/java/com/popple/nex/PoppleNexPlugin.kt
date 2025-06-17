package com.popple.nex

import com.google.inject.Provides
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

@PluginDescriptor(name = "Popple Nex")
@Extension
class PoppleNexPlugin : Plugin() {
    @Inject
    lateinit var config: PoppleNexConfig

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var client: Client

    private val log = Logger.getLogger(name)

    private val fightHandler = FightHandler()


    @Provides
    fun getConfig(configManager: ConfigManager): PoppleNexConfig? {
        return configManager.getConfig(PoppleNexConfig::class.java)
    }

    override fun startUp() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialised Nex bot.")
        }
    }

    override fun shutDown() {
        if (client.gameState == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Disabled Nex bot.")
        }
    }

    @Subscribe
    private fun onConfigButtonPressed(configButtonClicked: ConfigButtonClicked) {
        if (configButtonClicked.group.equals("popple-nex", ignoreCase = true)) {
            setRunning(!config.running())

            if (config.running()) {
                MessageUtils.addMessage("Nex bot started.")
            } else {
                MessageUtils.addMessage("Nex bot stopped.")
            }
        }
    }

    @Subscribe
    private fun onGameTick(event: GameTick) {
        if (!config.running()) return
        fightHandler.process()
    }


    private fun setRunning(running: Boolean) {
        configManager.setConfiguration(
                "popple-nex",
                "running",
                running
        )
    }
}