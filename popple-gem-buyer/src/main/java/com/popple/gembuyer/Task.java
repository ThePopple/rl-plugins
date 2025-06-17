package com.popple.gembuyer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;

import javax.inject.Inject;


@Slf4j
public abstract class Task {

    @Inject
    public Client client;

    @Inject
    public GemBuyerConfig config;

    public abstract boolean validate();

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }




    public void onGameTick(GameTick event) {
        return;
    }

}
