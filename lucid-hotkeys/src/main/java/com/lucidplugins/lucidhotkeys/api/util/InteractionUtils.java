package com.lucidplugins.lucidhotkeys.api.util;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.client.Static;
import java.util.function.Predicate;

public class InteractionUtils
{
    public static boolean isRunEnabled()
    {
        return Movement.isRunEnabled();
    }

    public static void toggleRun()
    {
        Movement.toggleRun();
    }

    public static int getRunEnergy()
    {
        return Movement.getRunEnergy();
    }


    public static boolean isWidgetHidden(int parentId, int childId, int grandchildId)
    {
        Widget target = Static.getClient().getWidget(parentId, childId);
        if (grandchildId != -1)
        {
            if (target == null || target.isSelfHidden())
            {
                return true;
            }

            Widget subTarget = target.getChild(grandchildId);
            if (subTarget != null)
            {
                return subTarget.isSelfHidden();
            }
        }

        if (target != null)
        {
            return target.isSelfHidden();
        }

        return true;
    }

    public static boolean isWidgetHidden(int parentId, int childId)
    {
        return isWidgetHidden(parentId, childId, -1);
    }

    public static void widgetInteract(int parentId, int childId, int grandchildId, String action)
    {
        Widget target = Static.getClient().getWidget(parentId, childId);
        if (target != null && grandchildId != -1)
        {
            target = target.getChild(grandchildId);
        }

        if (target != null)
        {
            target.interact(action);
        }
    }

    public static void widgetInteract(int parentId, int childId, String action)
    {
        widgetInteract(parentId, childId, -1, action);
    }

    public static int getWidgetSpriteId(int parentId, int childId)
    {
        return getWidgetSpriteId(parentId, childId, -1);
    }

    public static int getWidgetSpriteId(int parentId, int childId, int grandchildId)
    {
        Widget target = Static.getClient().getWidget(parentId, childId);
        if (grandchildId != -1)
        {
            if (target == null || target.isSelfHidden())
            {
                return -1;
            }

            Widget subTarget = target.getChild(grandchildId);
            if (subTarget != null)
            {
                return subTarget.getSpriteId();
            }
        }

        if (target != null)
        {
            return target.getSpriteId();
        }

        return -1;
    }

    public static String getWidgetText(int parentId, int childId)
    {
        return getWidgetText(parentId, childId, -1);
    }

    public static String getWidgetText(int parentId, int childId, int grandchildId)
    {
        Widget target = Static.getClient().getWidget(parentId, childId);
        if (grandchildId != -1)
        {
            if (target == null || target.isSelfHidden())
            {
                return "null";
            }

            Widget subTarget = target.getChild(grandchildId);
            if (subTarget != null)
            {
                return subTarget.getText() != null ? subTarget.getText() : "null";
            }
            else
            {
                return "null";
            }
        }

        if (target != null)
        {
            return target.getText() != null ? target.getText() : "null";
        }

        return "null";
    }

    public static void queueResumePause(int parentId, int childId, int subchildId)
    {
        WidgetPackets.queueResumePauseWidgetPacket(parentId << 16 | childId, subchildId);
    }

    public static void useItemOnTileObject(Item item, TileObject object)
    {
        item.useOn(object);
        Static.getClient().invokeMenuAction("Cancel", "", 0, 1006, 0, 0);
    }

    public static void useItemOnWallObject(Item item, WallObject wallObject)
    {
        final ItemComposition itemComposition = Static.getClient().getItemComposition(item.getId());
        final ObjectComposition objectComposition = Static.getClient().getObjectDefinition(wallObject.getId());
        Static.getClient().invokeMenuAction("Use", "<col=ff9040>" + itemComposition.getName() + "</col>", 0, MenuAction.WIDGET_TARGET.getId(), item.getSlot(), InventoryUtils.calculateWidgetId(item), item.getId(), -1);
        Static.getClient().invokeMenuAction("Use", "<col=ff9040>" + itemComposition.getName() + "</col><col=ffffff> -> <col=ffff>" + objectComposition.getName(), wallObject.getId(), MenuAction.WIDGET_TARGET_ON_GAME_OBJECT.getId(), wallObject.getLocalLocation().getSceneX(), wallObject.getLocalLocation().getSceneY(), -1, -1);
    }

    public static void useItemOnNPC(Item item, NPC npc)
    {
        if (item != null && npc != null)
        {
            item.useOn(npc);
        }
    }

    public static boolean isMoving()
    {
        return Static.getClient().getLocalPlayer().getPoseAnimation() != Static.getClient().getLocalPlayer().getIdlePoseAnimation();
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

    public static void interactWithTileItem(String name, String action)
    {
        TileItem item = TileItems.getNearest(tileItem -> tileItem.getName().toLowerCase().contains(name.toLowerCase()));

        if (item != null)
        {
            item.interact(action);
        }
    }

    public static void interactWithTileItem(TileItem item, String action)
    {
        if (item != null)
        {
            item.interact(action);
        }
    }

    public static float distanceTo2DHypotenuse(WorldPoint main, WorldPoint other)
    {
        return (float)Math.hypot((double)(main.getX() - other.getX()), (double)(main.getY() - other.getY()));
    }

    public static TileItem nearestTileItem(Predicate<TileItem> filter)
    {
        return TileItems.getNearest(filter);
    }

}
