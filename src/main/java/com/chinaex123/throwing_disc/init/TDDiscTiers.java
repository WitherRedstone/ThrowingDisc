package com.chinaex123.throwing_disc.init;

import com.chinaex123.throwing_disc.api.DiscTier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.common.Tags;

public final class TDDiscTiers {
    public static final DiscTier COPPER = new DiscTier(
            "copper", 2.0F, 128, 6, Rarity.COMMON, false,
            () -> Tags.Items.INGOTS_COPPER, Tiers.STONE
    );

    public static final DiscTier IRON = new DiscTier(
            "iron", 4.0F, 256, 10, Rarity.COMMON, false,
            () -> Tags.Items.INGOTS_IRON, Tiers.IRON
    );

    public static final DiscTier GOLD = new DiscTier(
            "golden", 4.0F, 512, 28, Rarity.COMMON, false,
            () -> Tags.Items.INGOTS_GOLD, Tiers.GOLD
    );

    public static final DiscTier DIAMOND = new DiscTier(
            "diamond", 6.0F, 1024, 15, Rarity.COMMON, false,
            () -> Tags.Items.GEMS_DIAMOND, Tiers.DIAMOND
    );

    public static final DiscTier NETHERITE = new DiscTier(
            "netherite", 8.0F, 2048, 20, Rarity.RARE, true,
            () -> Tags.Items.INGOTS_NETHERITE, Tiers.NETHERITE
    );

    private TDDiscTiers() {
    }
}
