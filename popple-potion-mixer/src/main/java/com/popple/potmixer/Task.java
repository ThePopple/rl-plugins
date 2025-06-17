package com.popple.potmixer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.util.HashMap;


@Slf4j
public abstract class Task {

    @Inject
    public Client client;

    @Inject
    public PotMixerConfig config;

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
                "popple-potion-mixer",
                "running",
                running
        );
    }

    public HashMap<String, Integer> parseIds() {
        Integer primaryID, secondaryID;
        HashMap<String, Integer> IDs = new HashMap<>();
        try {
            primaryID = Integer.parseInt(config.primary());

        } catch (NumberFormatException ex) {
            primaryID = null;
        }

        try {
            secondaryID = Integer.parseInt(config.secondary());
        } catch (NumberFormatException ex) {
            secondaryID = null;
        }

        IDs.put("primary", primaryID);
        IDs.put("secondary", secondaryID);

        return IDs;
    }

}
