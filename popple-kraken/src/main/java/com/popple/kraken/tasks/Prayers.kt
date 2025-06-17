package com.popple.kraken.tasks

import com.popple.kraken.api.Task
import lombok.extern.slf4j.Slf4j
import net.runelite.api.events.GameTick
import net.unethicalite.api.widgets.Prayers
import net.unethicalite.client.Static

@Slf4j
class Prayers : Task() {
    override fun validate(): Boolean {
        return config.quickPrayers()
    }

    override fun onGameTick(event: GameTick?): Int {
        if (Prayers.isQuickPrayerEnabled()) {
            Prayers.toggleQuickPrayer(!Prayers.isQuickPrayerEnabled())
            Static.getClientThread().invokeLater(Runnable {
                Prayers.toggleQuickPrayer(!Prayers.isQuickPrayerEnabled())
            })
        } else {
            Prayers.toggleQuickPrayer(!Prayers.isQuickPrayerEnabled())
        }
        return 0
    }
}