package com.lucidplugins.lucidhotkeys2.api.util;

import net.runelite.api.*;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.client.Static;

public class CombatUtils
{
    public static Prayer prayerForName(String name)
    {
        String p = name.toUpperCase().replaceAll(" ", "_");
        for (Prayer prayer : Prayer.values())
        {
            if (prayer.name().equals(p))
            {
                return prayer;
            }
        }
        return null;
    }

    public static Skill skillForName(String name)
    {
        for (Skill skill : Skill.values())
        {
            if (skill.name().equalsIgnoreCase(name))
            {
                return skill;
            }
        }
        return null;
    }

    public static void togglePrayer(Prayer prayer)
    {
        if (Static.getClient() == null || (Static.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 && !Static.getClient().isPrayerActive(prayer)))
        {
            return;
        }

        Prayers.toggle(prayer);
    }

    public static void activatePrayer(Prayer prayer)
    {
        if (Static.getClient() == null || Static.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 || Static.getClient().isPrayerActive(prayer))
        {
            return;
        }

        Prayers.toggle(prayer);
    }

    public static void deactivatePrayer(Prayer prayer)
    {
        if (Static.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 || !Static.getClient().isPrayerActive(prayer))
        {
            return;
        }

        Prayers.toggle(prayer);
    }

    public static int getSpecEnergy()
    {
        return Vars.getVarp(300) / 10;
    }

    public static void toggleQuickPrayers()
    {
        if (Static.getClient() == null || Static.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0)
        {
            return;
        }

        Prayers.toggleQuickPrayer(!Prayers.isQuickPrayerEnabled());
    }

    public static void activateQuickPrayers()
    {
        if (Static.getClient() == null || Static.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0)
        {
            return;
        }

        if (!Prayers.isQuickPrayerEnabled())
        {
            Prayers.toggleQuickPrayer(true);
        }
    }

    public static boolean isQuickPrayersEnabled()
    {
        return Static.getClient().getVarbitValue(Varbits.QUICK_PRAYER) == 1;
    }

    public static boolean isSpecEnabled()
    {
        return Vars.getVarp(VarPlayer.SPECIAL_ATTACK_ENABLED.getId()) == 1;
    }

    public static void toggleSpec()
    {
        Combat.toggleSpec();
    }
}
