package com.popple.gemcutter.tasks;

import com.popple.gemcutter.Task;
import com.popple.gemcutter.api.Banks;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.TileObject;
import net.runelite.api.events.GameTick;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.utils.MessageUtils;

@Slf4j
public class BankGems extends Task {

    @Override
    public boolean validate() {
        return Inventory.getFreeSlots() <= 0 ||
                (!Inventory.contains(ItemID.CHISEL) || !Inventory.contains(config.gem().getID()));
    }

    @Override
    public void onGameTick(GameTick event) {
        TileObject bank = Banks.Companion.findNearest();

        if (bank != null) {
            if (!Bank.isOpen()) {
                bank.interact("Bank");
            } else {
                Bank.depositAll(config.gem().getCutID());
                if (!Inventory.contains(ItemID.CHISEL)) {
                    Bank.depositInventory();
                    if (!Bank.contains(ItemID.CHISEL)) {
                        MessageUtils.addMessage("You don't have a chisel... Stopping execution.");
                        setRunning(false);
                        return;
                    } else {
                        Bank.withdraw(ItemID.CHISEL, 1, Bank.WithdrawMode.ITEM);
                    }
                }

                if (Bank.contains(config.gem().getID())) {
                    Bank.withdrawAll(config.gem().getID(), Bank.WithdrawMode.ITEM);
                } else {
                    MessageUtils.addMessage("Can't find any gems to cut, stopping execution.");
                    setRunning(false);
                }
            }
        } else {
            MessageUtils.addMessage("Can't find a bank.");
        }
    }
}
