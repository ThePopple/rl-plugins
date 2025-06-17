package com.popple.hydra

import net.runelite.client.config.Button
import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("popple-hydra")
interface PoppleHydraConfig : Config {

    @ConfigItem(keyName = "startBtn", name = "Start/Stop", description = "Start/stop the script", position = 150)
    fun startButton(): Button? {
        return Button()
    }

    @ConfigItem(keyName = "running", name = "running", description = "", hidden = true)
    fun running(): Boolean {
        return false
    }

}