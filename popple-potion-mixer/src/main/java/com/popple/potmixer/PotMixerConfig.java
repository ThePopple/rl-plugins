package com.popple.potmixer;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("popple-potion-mixer")
public interface PotMixerConfig extends Config {
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
            keyName = "primary",
            name = "Primary ID",
            description = "The primary to use",
            position = 0
    )
    default String primary() {
        return "";
    }

    @ConfigItem(
            keyName = "secondary",
            name = "Secondary ID",
            description = "The secondary to use",
            position = 0
    )
    default String secondary() {
        return "";
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