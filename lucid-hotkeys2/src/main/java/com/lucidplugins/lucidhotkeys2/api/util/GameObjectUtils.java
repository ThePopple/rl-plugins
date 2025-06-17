package com.lucidplugins.lucidhotkeys2.api.util;

import net.runelite.api.*;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.client.Static;

import java.util.Arrays;
import java.util.function.Predicate;

public class GameObjectUtils
{
    public static void interact(TileObject object, String action)
    {
        if (object.hasAction(action))
        {
            object.interact(action);
        }
    }

    public static boolean hasAction(int objectId, String action)
    {
        if (Static.getClient() == null)
        {
            return false;
        }

        ObjectComposition composition = Static.getClient().getObjectDefinition(objectId);

        if (composition == null)
        {
            return false;
        }

        return Arrays.stream(composition.getActions()).anyMatch(s -> s != null && s.equalsIgnoreCase(action));
    }

    public static TileObject nearest(String name)
    {
        return TileObjects.getNearest(name);
    }

    public static TileObject nearest(int id)
    {
        return TileObjects.getNearest(id);
    }

    public static TileObject nearest(Predicate<TileObject> filter)
    {
        return TileObjects.getNearest(filter);
    }
}
