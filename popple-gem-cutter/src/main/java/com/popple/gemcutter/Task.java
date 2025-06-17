package com.popple.gemcutter;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;


@Slf4j
public abstract class Task {

    @Inject
    public Client client;

    @Inject
    public GemCutterConfig config;

    @Inject
    public ConfigManager configManager;

    public abstract boolean validate();

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }


    public void onGameTick(GameTick event) {
        return;
    }

    public void setRunning(boolean running) {
        configManager.setConfiguration(
                "popple-gem-cutter",
                "running",
                running
        );
    }

}
