package com.popple.gembuyer.tasks;

import com.popple.gembuyer.Task;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.TileObject;
import net.runelite.api.events.GameTick;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.utils.MessageUtils;

@Slf4j
public class BankGems extends Task {

    @Override
    public boolean validate() {
        return Inventory.getFreeSlots() <= 0 || !Inventory.contains("Tokkul");
    }

    @Override
    public void onGameTick(GameTick event) {
        if (NPCs.getNearest("TzHaar-Hur-Rin") != null && !client.getLocalPlayer().isAnimating()) {
            teleportHome();
            return;
        }

        TileObject bank = TileObjects.getNearest(ObjectID.BANK_BOOTH_10583);

        if (bank != null) {
            if (!Bank.isOpen()) {
                bank.interact("Bank");
            } else {
                Bank.depositAll(config.gem().getID());
            }
        } else {
            MessageUtils.addMessage("Can't find a bank.");
        }
    }

    private void teleportHome() {
        if (Inventory.contains(ItemID.TELEPORT_TO_HOUSE)) {
            Inventory.getFirst(ItemID.TELEPORT_TO_HOUSE).interact("Break");
        }
    }
}
