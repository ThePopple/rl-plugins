package com.popple.kraken

import net.runelite.client.config.Button
import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("popple-kraken")
interface PoppleKrakenConfig : Config {

    @ConfigItem(keyName = "startBtn", name = "Start/Stop", description = "Start/stop the script", position = 150)
    fun startButton(): Button? {
        return Button()
    }

    @ConfigItem(
        keyName = "prayers",
        name = "1 tick flick quick prayers",
        description = "1 tick flicks selected quick prayers",
        position = 0
    )
    fun quickPrayers(): Boolean {
        return false
    }

    @ConfigItem(keyName = "debug", name = "debug mode", description = "You probably don't need this", position = 140)
    fun debugMode(): Boolean {
        return false
    }

}