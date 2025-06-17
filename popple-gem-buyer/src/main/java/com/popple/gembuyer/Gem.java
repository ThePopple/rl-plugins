package com.popple.gembuyer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Gem {
    SAPPHIRE(ItemID.UNCUT_SAPPHIRE),
    EMERALD(ItemID.UNCUT_EMERALD),
    RUBY(ItemID.UNCUT_RUBY),
    DIAMOND(ItemID.UNCUT_DIAMOND);

    private final int ID;
}
