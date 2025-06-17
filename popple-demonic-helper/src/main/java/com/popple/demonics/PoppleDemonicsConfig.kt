package com.popple.demonics

import net.runelite.client.config.Button
import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("popple-demonic-helper")
interface PoppleDemonicsConfig : Config {

    @ConfigItem(keyName = "startBtn", name = "Start/Stop", description = "Start/stop the script", position = 150)
    fun startButton(): Button? {
        return Button()
    }

    @ConfigItem(keyName = "primaryWeapon", name = "Primary Weapon", description = "")
    fun primaryWeapon(): String {
        return ""
    }
    @ConfigItem(keyName = "primaryWeaponStyle", name = "Primary Weapon Style", description = "")
    fun primaryWeaponStyle(): CmbStyle {
        return CmbStyle.MELEE
    }

    @ConfigItem(keyName = "secondaryWeapon", name = "Secondary Weapon", description = "")
    fun secondaryWeapon(): String {
        return ""
    }

    @ConfigItem(keyName = "secondaryWeaponStyle", name = "Secondary Weapon Style", description = "")
    fun secondaryWeaponStyle(): CmbStyle {
        return CmbStyle.RANGED
    }
}