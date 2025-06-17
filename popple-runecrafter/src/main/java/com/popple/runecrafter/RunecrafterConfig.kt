package com.popple.runecrafter

import net.runelite.client.config.Button
import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("popple-runecraft")
interface RunecrafterConfig : Config {
    @ConfigItem(keyName = "startBtn", name = "Start/Stop", description = "Start/stop the script", position = 150)
    fun startButton(): Button {
        return Button()
    }

    @ConfigItem(keyName = "running", name = "running", description = "", hidden = true)
    fun running(): Boolean {
        return false
    }

    @ConfigItem(keyName = "craftCape", name = "Use Crafting cape", description = "Bank at crafting guild")
    fun craftCape(): Boolean {
        return false
    }
}