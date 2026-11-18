package com.chinaex123.throwing_disc.data;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.init.TDEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEnchantmentTagsProvider extends EnchantmentTagsProvider {

    public ModEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                      @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ThrowingDisc.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 附魔台可获得。
        tag(EnchantmentTags.IN_ENCHANTING_TABLE).add(
                TDEnchantments.IMPALING,
                TDEnchantments.RAMMING,
                TDEnchantments.AERODYNAMICS,
                TDEnchantments.EXPANSION,
                TDEnchantments.RICOCHET,
                TDEnchantments.MAGNETIC_RETURN,
                TDEnchantments.PRESERVATION
        );
        // 非宝藏附魔。
        tag(EnchantmentTags.NON_TREASURE).add(
                TDEnchantments.IMPALING,
                TDEnchantments.RAMMING,
                TDEnchantments.AERODYNAMICS,
                TDEnchantments.EXPANSION,
                TDEnchantments.RICOCHET,
                TDEnchantments.MAGNETIC_RETURN,
                TDEnchantments.PRESERVATION
        );
        // 村民可以出售对应的附魔书。
        tag(EnchantmentTags.TRADEABLE).add(
                TDEnchantments.IMPALING,
                TDEnchantments.RAMMING,
                TDEnchantments.AERODYNAMICS,
                TDEnchantments.EXPANSION,
                TDEnchantments.RICOCHET,
                TDEnchantments.MAGNETIC_RETURN,
                TDEnchantments.PRESERVATION
        );
    }
}
