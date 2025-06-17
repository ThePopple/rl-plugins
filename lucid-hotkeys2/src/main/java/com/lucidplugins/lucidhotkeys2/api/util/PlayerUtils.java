package com.lucidplugins.lucidhotkeys2.api.util;

import net.runelite.api.Player;
import net.unethicalite.api.entities.Players;

import java.util.List;
import java.util.function.Predicate;

public class PlayerUtils
{
    public static void interactPlayer(String name, String action)
    {
        Players.getNearest(name).interact(action);
    }

    public static Player getNearest(String name)
    {
        return Players.getNearest(player -> player.getName().contains(name));
    }

    public static Player getNearest(Predicate<Player> filter)
    {
        return Players.getNearest(filter);
    }

    public static List<Player> getAll(Predicate<Player> filter)
    {
        return Players.getAll(filter);
    }
}
