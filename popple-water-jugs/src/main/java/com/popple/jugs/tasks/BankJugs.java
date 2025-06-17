package com.popple.jugs.tasks;

import com.popple.jugs.Task;
import com.popple.jugs.api.util.InventoryUtils;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;

@Slf4j
public class BankJugs extends Task {

    private static final WorldPoint BANK_TILE = new WorldPoint(3309, 3120, 0);

    @Override
    public boolean validate() {
        return InventoryUtils.getFreeSlots() <= 0 || !InventoryUtils.contains("Coins");
    }

    @Override
    public void onGameTick(GameTick event) {
        TileObject chest = TileObjects.getFirstAt(BANK_TILE, x -> x.hasAction("Use", "Collect"));

        if (chest != null) {
            if (!Bank.isOpen()) {
                chest.interact("Use");
            } else {
                Bank.depositAll(ItemID.JUG_OF_WATER);
                Bank.withdrawAll(ItemID.COINS_995, Bank.WithdrawMode.ITEM);
                Bank.close();
            }
        }
    }
}
