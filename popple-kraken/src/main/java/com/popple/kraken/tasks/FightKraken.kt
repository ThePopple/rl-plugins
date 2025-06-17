package com.popple.kraken.tasks

import com.popple.kraken.api.Task
import lombok.extern.slf4j.Slf4j
import net.runelite.api.NPC
import net.runelite.api.NpcID
import net.runelite.api.events.GameTick
import net.unethicalite.api.entities.NPCs

@Slf4j
class FightKraken : Task() {


    override fun validate(): Boolean {
        val kraken: NPC? = NPCs.getNearest(NpcID.KRAKEN)
        val interactingWithKrak: Boolean = client.localPlayer.interacting == kraken

        if (interactingWithKrak) state.stateString = "Fighting Kraken"

        val valid: Boolean = (kraken != null || NPCs.getNearest(NpcID.WHIRLPOOL_496) != null)
                && state.poolsDone
                && !interactingWithKrak

        if (valid && NPCs.getNearest(NpcID.WHIRLPOOL_5534) == null) state.poolsDone = true

        return valid
    }

    override fun onGameTick(event: GameTick?): Int {
        if (NPCs.getNearest(NpcID.WHIRLPOOL_496) == null) {
            state.stateString = "Fighting Kraken"
            NPCs.getNearest(NpcID.KRAKEN).interact("Attack")
            return 0
        } else {
            state.stateString = "Disturbing Kraken"
            NPCs.getNearest(NpcID.WHIRLPOOL_496).interact("Disturb")
            return 2
        }
    }
}