package com.popple.hydra

import lombok.AllArgsConstructor
import lombok.Getter
import net.runelite.api.CollisionDataFlag
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * An enum that binds a name to each movement flag.
 *
 * @see CollisionDataFlag
 */
internal enum class MovementFlag(private val flag: Int) {
    BLOCK_MOVEMENT_NORTH_WEST(CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST),
    BLOCK_MOVEMENT_NORTH(CollisionDataFlag.BLOCK_MOVEMENT_NORTH),
    BLOCK_MOVEMENT_NORTH_EAST(CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST),
    BLOCK_MOVEMENT_EAST(CollisionDataFlag.BLOCK_MOVEMENT_EAST),
    BLOCK_MOVEMENT_SOUTH_EAST(CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST),
    BLOCK_MOVEMENT_SOUTH(CollisionDataFlag.BLOCK_MOVEMENT_SOUTH),
    BLOCK_MOVEMENT_SOUTH_WEST(CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST),
    BLOCK_MOVEMENT_WEST(CollisionDataFlag.BLOCK_MOVEMENT_WEST),

    BLOCK_MOVEMENT_OBJECT(CollisionDataFlag.BLOCK_MOVEMENT_OBJECT),
    BLOCK_MOVEMENT_FLOOR_DECORATION(CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION),
    BLOCK_MOVEMENT_FLOOR(CollisionDataFlag.BLOCK_MOVEMENT_FLOOR),
    BLOCK_MOVEMENT_FULL(CollisionDataFlag.BLOCK_MOVEMENT_FULL);


    companion object {
        /**
         * @param collisionData The tile collision flags.
         * @return The set of [MovementFlag]s that have been set.
         */
        fun getSetFlags(collisionData: Int): Set<MovementFlag> {
            return Arrays.stream(values())
                .filter { movementFlag: MovementFlag -> (movementFlag.flag and collisionData) != 0 }
                .collect(Collectors.toSet())
        }
    }
}