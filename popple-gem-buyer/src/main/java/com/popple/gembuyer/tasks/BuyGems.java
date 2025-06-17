package com.popple.gembuyer.tasks;

import com.popple.gembuyer.Task;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;

@Slf4j
public class BuyGems extends Task {

    @Override
    public boolean validate() {
        return Inventory.getFreeSlots() >= 1 && Inventory.contains("Tokkul");
    }

    @Override
    public void onGameTick(GameTick event) {
        NPC geezer = NPCs.getNearest("TzHaar-Hur-Rin");

        if (geezer != null) {

            if (!Shop.isOpen()) {
                geezer.interact("Trade");
            } else {
                for (int i = 0; i < 14; i++) {
                    Shop.buyFifty(config.gem().getID());
                }
            }


        } else {
            if (Inventory.contains(ItemID.CRYSTAL_OF_ECHOES)) {
                Inventory.getFirst(ItemID.CRYSTAL_OF_ECHOES).interact("Teleport-back");
            }
        }

    }
}
