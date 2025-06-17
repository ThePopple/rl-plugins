package com.popple.gemcutter;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("popple-gem-cutter")
public interface GemCutterConfig extends Config {
    @ConfigItem(
            keyName = "startBtn",
            name = "Start/Stop",
            description = "Start/stop the script",
            position = 150
    )
    default Button startButton() {
        return new Button();
    }

    @ConfigItem(
            keyName = "gem",
            name = "Gem to cut",
            description = "The type of gem to cut",
            position = 0
    )
    default Gem gem() {
        return Gem.DIAMOND;
    }

    @ConfigItem(
            keyName = "running",
            name = "running",
            description = "",
            hidden = true
    )
    default boolean running() {
        return false;
    }
}