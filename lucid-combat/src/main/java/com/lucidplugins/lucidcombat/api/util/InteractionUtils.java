package com.lucidplugins.lucidcombat.api.util;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.client.Static;

import java.util.List;
import java.util.function.Predicate;

public class InteractionUtils
{
    public static void useItemOnWallObject(Client client, Item item, WallObject wallObject)
    {
        final ItemComposition itemComposition = client.getItemComposition(item.getId());
        final ObjectComposition objectComposition = client.getObjectDefinition(wallObject.getId());
        client.invokeMenuAction("Use", "<col=ff9040>" + itemComposition.getName() + "</col>", 0, MenuAction.WIDGET_TARGET.getId(), item.getSlot(), InventoryUtils.calculateWidgetId(client, item), item.getId(), -1);
        client.invokeMenuAction("Use", "<col=ff9040>" + itemComposition.getName() + "</col><col=ffffff> -> <col=ffff>" + objectComposition.getName(), wallObject.getId(), MenuAction.WIDGET_TARGET_ON_GAME_OBJECT.getId(), wallObject.getLocalLocation().getSceneX(), wallObject.getLocalLocation().getSceneY(), -1, -1);
    }

    public static void useItemOnNPC(Item item, NPC npc)
    {
        if (item != null && npc != null)
        {
            item.useOn(npc);
        }
    }

    public static boolean sleep(Client client, long ms)
    {
        if (client.isClientThread())
        {
            return false;
        }
        else
        {
            try
            {
                Thread.sleep(ms);
                return true;
            }
            catch (InterruptedException var3)
            {
                return false;
            }
        }
    }

    public static void walk(WorldPoint worldPoint)
    {
        Movement.walk(worldPoint);
    }

    public static boolean tileItemNameExistsWithinDistance(String name, int distance)
    {
        TileItem item = TileItems.getNearest(tileItem -> tileItem.getName().toLowerCase().contains(name.toLowerCase()));

        if (item != null && distanceTo2DHypotenuse(item.getWorldLocation(), Static.getClient().getLocalPlayer().getWorldLocation()) <= distance)
        {
            return true;
        }

        return false;
    }

    public static List<Tile> getTiles(Predicate<Tile> filter)
    {
        return Tiles.getAll(filter);
    }

    public static TileItem nearestTileItem(Predicate<TileItem> filter)
    {
        return TileItems.getNearest(filter);
    }

    public static List<TileItem> getAllTileItems(Predicate<TileItem> filter)
    {
        return TileItems.getAll(filter);
    }

    public static boolean tileItemIdExistsWithinDistance(int itemId, int distance)
    {
        TileItem item = TileItems.getNearest(itemId);

        if (item != null && distanceTo2DHypotenuse(item.getWorldLocation(), Static.getClient().getLocalPlayer().getWorldLocation()) <= distance)
        {
            return true;
        }

        return false;
    }

    public static void interactWithTileItem(int itemId, String action)
    {
        TileItem item = TileItems.getNearest(itemId);

        if (item != null)
        {
            item.interact(action);
        }
    }

    public static void interactWithTileItem(TileItem tileItem, String action)
    {
        if (tileItem != null)
        {
            tileItem.interact(action);
        }
    }

    public static void interactWithTileItem(String name, String action)
    {
        TileItem item = TileItems.getNearest(tileItem -> tileItem.getName().toLowerCase().contains(name.toLowerCase()));

        if (item != null)
        {
            item.interact(action);
        }
    }

    public static float distanceTo2DHypotenuse(WorldPoint main, WorldPoint other)
    {
        return (float)Math.hypot((double)(main.getX() - other.getX()), (double)(main.getY() - other.getY()));
    }

    public static boolean isWalkable(WorldPoint point)
    {
        return Reachable.isWalkable(point);
    }
}
