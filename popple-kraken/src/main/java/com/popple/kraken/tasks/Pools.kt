package com.popple.kraken.tasks

import com.popple.kraken.api.Task
import lombok.extern.slf4j.Slf4j
import net.runelite.api.NPC
import net.runelite.api.NpcID
import net.runelite.api.events.GameTick
import net.unethicalite.api.entities.NPCs

@Slf4j
class Pools : Task() {

    private var tentPools: MutableList<NPC>? = null

    override fun validate(): Boolean {
        val valid: Boolean = NPCs.getNearest(NpcID.WHIRLPOOL_5534) != null
        if (!valid) state.poolsDone = true

        return valid && !state.poolsDone
    }

    override fun onGameTick(event: GameTick?): Int {
        state.stateString = "Disturbing pools"
        if (tentPools == null) {
            tentPools = NPCs.getAll(NpcID.WHIRLPOOL_5534).toMutableList()
        }

        if (tentPools!!.isNotEmpty()) {
            tentPools!![0].interact("Disturb")

            return if (tentPools!!.size == 1) {
                0
            } else {
                tentPools!!.removeAt(0)
                1
            }
        } else {
            tentPools = null
            state.poolsDone = true
        }

        return 0
    }
}