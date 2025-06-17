package com.popple.potmixer.tasks;

import com.popple.potmixer.Task;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Widgets;

import java.util.HashMap;

@Slf4j
public class MixPotions extends Task {

    private boolean makingPots = false;

    @Override
    public boolean validate() {
        HashMap<String, Integer> IDs = parseIds();
        Integer primaryID = IDs.get("primary");
        Integer secondaryID = IDs.get("secondary");

        if (primaryID == null) {
            MessageUtils.addMessage("Invalid primary ID provided, stopping execution.");
            setRunning(false);
            return false;
        }

        if (secondaryID == null) {
            MessageUtils.addMessage("Invalid secondary ID provided, stopping execution.");
            setRunning(false);
            return false;
        }

        boolean valid = Inventory.contains(primaryID) && Inventory.contains(secondaryID);
        if (!valid) {
            makingPots = false;
        }


        return valid && !makingPots;
    }

    @Override
    public void onGameTick(GameTick event) {
        if (client.getLocalPlayer().isAnimating() || client.getLocalPlayer().isMoving()) return;

        HashMap<String, Integer> IDs = parseIds();
        Integer primaryID = IDs.get("primary");
        Integer secondaryID = IDs.get("secondary");

        Item primary = Inventory.getFirst(primaryID);
        Item secondary = Inventory.getFirst(secondaryID);

        primary.useOn(secondary);

        Widget makeWidget = Widgets.get(270, 14);
        if (Widgets.isVisible(makeWidget)) {
            makeWidget.interact("Make");
            makingPots = true;
        }
    }
}
