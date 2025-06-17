package com.popple.mole.api

import com.popple.mole.PoppleMoleConfig
import com.popple.mole.PoppleMoleOverlay
import lombok.extern.slf4j.Slf4j
import net.runelite.api.Client
import net.runelite.api.events.GameTick
import net.runelite.client.config.ConfigManager
import javax.inject.Inject

@Slf4j
abstract class Task {
    @Inject
    lateinit var config: PoppleMoleConfig

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var client: Client

    @Inject
    lateinit var overlay: PoppleMoleOverlay

    abstract fun validate(): Boolean

    abstract fun onGameTick(event: GameTick?): Int

    val taskDescription: String
        get() = this.javaClass.simpleName
}
