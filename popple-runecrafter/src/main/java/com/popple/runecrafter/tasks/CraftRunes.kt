package com.popple.runecrafter.tasks

import com.popple.runecrafter.Task
import lombok.extern.slf4j.Slf4j
import net.runelite.api.ItemID
import net.runelite.api.ObjectID
import net.runelite.api.events.GameTick
import net.unethicalite.api.entities.TileObjects
import net.unethicalite.api.items.Equipment
import net.unethicalite.api.items.Inventory

@Slf4j
class CraftRunes : Task() {
    override fun validate(): Boolean {
        val tpItemCheck = if (config.craftCape()) {
            Inventory.contains(ItemID.CRAFTING_CAPE, ItemID.CRAFTING_CAPET) || Equipment.contains(
                ItemID.CRAFTING_CAPE,
                ItemID.CRAFTING_CAPET
            )
        } else {
            Inventory.contains(ItemID.TELEPORT_TO_HOUSE)
        }

        return Inventory.contains(ItemID.PURE_ESSENCE)
                && Inventory.contains(ItemID.CRYSTAL_OF_ECHOES)
                && tpItemCheck

    }

    override fun onGameTick(event: GameTick?) {
        val altar = TileObjects.getNearest(ObjectID.ALTAR_29631)

        if (altar == null) {
            returnTeleport()
        } else {
            altar.interact("Craft-rune")
        }
    }

    private fun returnTeleport() {
        if (Inventory.contains(ItemID.CRYSTAL_OF_ECHOES)) {
            Inventory.getFirst(ItemID.CRYSTAL_OF_ECHOES).interact("Teleport-back")
        }
    }
}
