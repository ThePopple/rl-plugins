package com.popple.mole.tasks

import com.popple.mole.Tiles
import com.popple.mole.api.Task
import lombok.extern.slf4j.Slf4j
import net.runelite.api.NpcID
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameTick
import net.unethicalite.api.entities.NPCs
import net.unethicalite.api.movement.Movement
import net.unethicalite.api.utils.MessageUtils

@Slf4j
class FindMole : Task() {
    private var prevLoc: WorldPoint? = null

    override fun validate(): Boolean {
        return client.hasHintArrow() &&
                NPCs.getNearest(NpcID.GIANT_MOLE) == null
    }

    override fun onGameTick(event: GameTick?): Int {
        overlay.state = "Finding Mole"

        if (client.localPlayer.isMoving) return 0

        var pathLoc: WorldPoint
        var playerLoc: WorldPoint = client.localPlayer.worldLocation

        try {
            val moleLoc: WorldPoint = client.hintArrowPoint
            val pathTo: List<WorldPoint> = playerLoc.pathTo(client, moleLoc)
            pathLoc = pathTo[pathTo.size / 2]

        } catch (_: Exception) {
            playerLoc = client.localPlayer.worldLocation
            if (config.debugMode()) {
                MessageUtils.addMessage("In exception")
                MessageUtils.addMessage("playerLoc: $playerLoc")
            }

            pathLoc = enumValues<Tiles>().map { it.location }
                .sortedBy { playerLoc.distanceTo(it) }
                .first { playerLoc.distanceTo(it) >= 15 }
        }


        if (config.debugMode()) {
            MessageUtils.addMessage("prevLoc: $prevLoc")
            MessageUtils.addMessage("pathLoc: $pathLoc")
        }

        Movement.walk(pathLoc)


        return 0
    }
}