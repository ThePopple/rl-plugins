package com.popple.examplekotlin

import com.google.inject.Provides
import net.runelite.client.config.ConfigManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import org.pf4j.Extension
import java.util.logging.Logger

@PluginDescriptor(name = "Example Kotlin Plugin")
@Extension
class ExampleKotlinPlugin : Plugin() {

    override fun startUp() {
        super.startUp()
    }

    override fun shutDown() {
        super.shutDown()
    }

    private val log = Logger.getLogger(name)

    @Provides
    fun getConfig(configManager: ConfigManager): ExampleKotlinConfig? {
        return configManager.getConfig<ExampleKotlinConfig>(ExampleKotlinConfig::class.java)
    }
}