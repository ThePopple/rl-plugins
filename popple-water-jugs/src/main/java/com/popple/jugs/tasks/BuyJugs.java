package com.popple.jugs.tasks;

import com.popple.jugs.Task;
import com.popple.jugs.api.util.InventoryUtils;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Shop;
import net.unethicalite.api.utils.MessageUtils;

@Slf4j
public class BuyJugs extends Task {

    @Override
    public boolean validate() {
        return InventoryUtils.getFreeSlots() >= 1 && InventoryUtils.contains("Coins");
    }

    @Override
    public void onGameTick(GameTick event) {
        NPC shantay = NPCs.getNearest("Shantay");

        if (shantay != null) {

            if (!Shop.isOpen()) {
                shantay.interact("Trade");
            } else {
                Shop.buyFifty(ItemID.JUG_OF_WATER);
                Shop.buyFifty(ItemID.JUG_OF_WATER);
                Shop.buyFifty(ItemID.JUG_OF_WATER);
            }
        } else {
            MessageUtils.addMessage("Couldn't find Shantay.");

        }
    }
}
