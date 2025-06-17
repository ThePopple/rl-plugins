package com.popple.mole.tasks

import com.popple.mole.api.Task
import lombok.extern.slf4j.Slf4j
import net.runelite.api.NpcID
import net.runelite.api.events.GameTick
import net.unethicalite.api.entities.NPCs

@Slf4j
class FightMole : Task() {
    override fun validate(): Boolean {
        return NPCs.getNearest(NpcID.GIANT_MOLE) != null
    }

    override fun onGameTick(event: GameTick?): Int {
        overlay.state = "Fighting Mole"
        if (client.localPlayer.isAnimating) return 1

        NPCs.getNearest(NpcID.GIANT_MOLE).interact("Attack")

        return 0
    }
}