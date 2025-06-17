package com.popple.runecrafter.tasks

import com.popple.runecrafter.Task
import com.popple.runecrafter.api.Banks
import lombok.extern.slf4j.Slf4j
import net.runelite.api.ItemID
import net.runelite.api.events.GameTick
import net.unethicalite.api.items.Bank
import net.unethicalite.api.items.Equipment
import net.unethicalite.api.items.Inventory
import net.unethicalite.api.utils.MessageUtils
import java.util.function.Consumer

@Slf4j
class BankRunes : Task() {
    private val runes: List<Int> = listOf(
        ItemID.AIR_RUNE,
        ItemID.MIND_RUNE,
        ItemID.WATER_RUNE,
        ItemID.EARTH_RUNE,
        ItemID.FIRE_RUNE,
        ItemID.BODY_RUNE,
        ItemID.COSMIC_RUNE,
        ItemID.CHAOS_RUNE,
        ItemID.NATURE_RUNE,
        ItemID.LAW_RUNE,
        ItemID.DEATH_RUNE,
        ItemID.ASTRAL_RUNE,
        ItemID.BLOOD_RUNE,
        ItemID.SOUL_RUNE,
        ItemID.WRATH_RUNE
    )

    override fun validate(): Boolean {
        return !Inventory.contains(ItemID.PURE_ESSENCE) || !Inventory.contains(ItemID.CRYSTAL_OF_ECHOES)
    }

    override fun onGameTick(event: GameTick?) {
        val bank = Banks.findNearest()

        if (bank == null) {
            if (!client.localPlayer.isAnimating) teleportHome()
        } else {
            if (!Bank.isOpen()) {
                bank.interact(if (bank.name.contains("booth")) "Bank" else "Use")
            } else {
                if (!Bank.contains(ItemID.PURE_ESSENCE)) {
                    MessageUtils.addMessage("No essence found, stopping execution...")
                    setRunning(false)
                    return
                } else if (config.craftCape() && !Bank.contains(
                        ItemID.CRAFTING_CAPE,
                        ItemID.CRAFTING_CAPET
                    ) && !Inventory.contains(
                        ItemID.CRAFTING_CAPE,
                        ItemID.CRAFTING_CAPET
                    ) && !Equipment.contains(ItemID.CRAFTING_CAPE, ItemID.CRAFTING_CAPET)
                ) {
                    MessageUtils.addMessage("No Crafting cape found, stopping execution...")
                    setRunning(false)
                    return
                } else if (!Bank.contains(ItemID.TELEPORT_TO_HOUSE) && !Inventory.contains(ItemID.TELEPORT_TO_HOUSE)) {
                    MessageUtils.addMessage("No house tabs found, stopping execution...")
                    setRunning(false)
                    return
                }

                runes.forEach(Consumer { ids: Int? ->
                    Bank.depositAll(
                        ids!!
                    )
                })

                Bank.withdrawAll(ItemID.TELEPORT_TO_HOUSE, Bank.WithdrawMode.ITEM)
                Bank.withdraw(ItemID.CRYSTAL_OF_ECHOES, 1, Bank.WithdrawMode.ITEM)
                Bank.withdrawAll(ItemID.PURE_ESSENCE, Bank.WithdrawMode.ITEM)
                Bank.close()
            }
        }
    }

    private fun teleportHome() {
        if (config.craftCape()) {
            if (Inventory.contains(ItemID.CRAFTING_CAPE) || Equipment.contains(ItemID.CRAFTING_CAPE)) {
                Inventory.getFirst(ItemID.CRAFTING_CAPE)?.interact("Teleport")
                Equipment.getFirst(ItemID.CRAFTING_CAPE)?.interact("Teleport")
            } else if (Inventory.contains(ItemID.CRAFTING_CAPET) || Equipment.contains(ItemID.CRAFTING_CAPET)) {
                Inventory.getFirst(ItemID.CRAFTING_CAPET)?.interact("Teleport")
                Equipment.getFirst(ItemID.CRAFTING_CAPET)?.interact("Teleport")
            }
        } else {
            if (Inventory.contains(ItemID.TELEPORT_TO_HOUSE)) {
                Inventory.getFirst(ItemID.TELEPORT_TO_HOUSE).interact("Break")
            }
        }
    }
}
