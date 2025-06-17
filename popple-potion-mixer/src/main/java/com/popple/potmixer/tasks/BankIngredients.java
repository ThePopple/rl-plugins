package com.popple.potmixer.tasks;

import com.popple.potmixer.Task;
import com.popple.potmixer.api.Banks;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.TileObject;
import net.runelite.api.events.GameTick;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.utils.MessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BankIngredients extends Task {

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

        return !Inventory.contains(primaryID) || !Inventory.contains(secondaryID);
    }

    @Override
    public void onGameTick(GameTick event) {
        if (!config.running()) return;

        TileObject bank = Banks.findNearest();

        if (bank != null) {
            if (!Bank.isOpen()) {
                bank.interact("Bank");
            } else {

                HashMap<String, Integer> IDs = parseIds();
                Integer primaryID = IDs.get("primary");
                Integer secondaryID = IDs.get("secondary");

                if (!Bank.contains(primaryID) && !Inventory.contains(primaryID)) {
                    MessageUtils.addMessage("Unable to find primary, stopping execution.");
                    setRunning(false);
                    return;
                } else if (!Bank.contains(secondaryID) && !Inventory.contains(secondaryID)) {
                    MessageUtils.addMessage("Unable to find secondary, stopping execution.");
                    setRunning(false);
                    return;
                }

                List<Integer> itemsToDeposit = Inventory.getAll().stream()
                        .filter(item -> item.getId() != primaryID && item.getId() != secondaryID)
                        .map(Item::getId)
                        .collect(Collectors.toList());

                itemsToDeposit.forEach(Bank::depositAll);

                if (client.getItemDefinition(secondaryID).isStackable()) {
                    Bank.withdrawAll(secondaryID, Bank.WithdrawMode.ITEM);
                    Bank.withdrawAll(primaryID, Bank.WithdrawMode.ITEM);

                } else {
                    Bank.withdraw(primaryID, 14, Bank.WithdrawMode.ITEM);
                    Bank.withdraw(secondaryID, 14, Bank.WithdrawMode.ITEM);

                }


                Bank.close();
            }
        } else {
            MessageUtils.addMessage("Can't find a bank.");
        }
    }
}
