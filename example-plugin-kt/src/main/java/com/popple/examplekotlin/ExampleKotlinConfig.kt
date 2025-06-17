package com.popple.examplekotlin

import net.runelite.client.config.Button
import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("example-kotlin-plugin")
interface ExampleKotlinConfig : Config {

    @ConfigItem(keyName = "startBtn", name = "Start/Stop", description = "Start/stop the script", position = 150)
    fun startButton(): Button? {
        return Button()
    }

    @ConfigItem(keyName = "running", name = "running", description = "", hidden = true)
    fun running(): Boolean {
        return false
    }

}