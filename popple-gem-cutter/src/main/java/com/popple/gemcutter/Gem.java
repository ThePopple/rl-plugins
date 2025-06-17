package com.popple.gemcutter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Gem {
    SAPPHIRE(ItemID.UNCUT_SAPPHIRE, ItemID.SAPPHIRE),
    EMERALD(ItemID.UNCUT_EMERALD, ItemID.EMERALD),
    RUBY(ItemID.UNCUT_RUBY, ItemID.RUBY),
    DIAMOND(ItemID.UNCUT_DIAMOND, ItemID.DIAMOND);

    private final int ID;
    private final int cutID;
}
