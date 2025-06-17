package com.popple.runecrafter

import lombok.extern.slf4j.Slf4j
import net.runelite.api.Client
import net.runelite.api.events.GameTick
import net.runelite.client.config.ConfigManager
import javax.inject.Inject

@Slf4j
abstract class Task {
    @Inject
    lateinit var client: Client

    @Inject
    lateinit var config: RunecrafterConfig

    @Inject
    lateinit var configManager: ConfigManager

    abstract fun validate(): Boolean

    val taskDescription: String
        get() = this.javaClass.simpleName


    open fun onGameTick(event: GameTick?) {
        return
    }

    fun setRunning(running: Boolean) {
        configManager.setConfiguration(
            "popple-runecraft",
            "running",
            running
        )
    }
}
