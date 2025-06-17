package com.popple.jugs;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;

import javax.inject.Inject;


@Slf4j
public abstract class Task {
    public Task() {
    }

    @Inject
    public Client client;

    public abstract boolean validate();

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }


    public void onGameTick(GameTick event) {
        return;
    }

}
