package com.popple.gemcutter.tasks;

import com.popple.gemcutter.Task;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Widgets;

@Slf4j
public class CutGems extends Task {

    @Override
    public boolean validate() {
        return Inventory.contains(ItemID.CHISEL)
                && Inventory.contains(config.gem().getID())
                && !client.getLocalPlayer().isAnimating();
    }

    @Override
    public void onGameTick(GameTick event) {
        Item chisel = Inventory.getFirst(ItemID.CHISEL);
        Item gem = Inventory.getFirst(config.gem().getID());

        chisel.useOn(gem);

        Widget makeWidget = Widgets.get(270, 14);
        if (Widgets.isVisible(makeWidget)) {
            makeWidget.interact("Make");
        }
    }
}
