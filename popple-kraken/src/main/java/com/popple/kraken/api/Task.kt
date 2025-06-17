package com.popple.kraken.api

import com.popple.kraken.PoppleKrakenConfig
import com.popple.kraken.State
import lombok.extern.slf4j.Slf4j
import net.runelite.api.Client
import net.runelite.api.events.GameTick
import javax.inject.Inject

@Slf4j
abstract class Task {
    @Inject
    lateinit var config: PoppleKrakenConfig

    @Inject
    lateinit var client: Client

    @Inject
    lateinit var state: State

    abstract fun validate(): Boolean

    abstract fun onGameTick(event: GameTick?): Int

    val taskDescription: String
        get() = this.javaClass.simpleName
}
