package com.popple.jugs;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("popple-water-jugs")
public interface JugConfig extends Config {
    @ConfigItem(
            keyName = "startBtn",
            name = "Start/Stop",
            description = "Start/stop the script",
            position = 150
    )
    default Button startButton() {
        return new Button();
    }
}
