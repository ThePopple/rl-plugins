package com.popple.gembuyer;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("popple-gem-buyer")
public interface GemBuyerConfig extends Config {
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
            name = "Gem to buy",
            description = "The type of gem to buy",
            position = 0
    )
    default Gem gem() {
        return Gem.DIAMOND;
    }
}