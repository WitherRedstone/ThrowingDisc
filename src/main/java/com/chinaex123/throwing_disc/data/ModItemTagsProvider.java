package com.chinaex123.throwing_disc.data;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.api.DiscTier;
import com.chinaex123.throwing_disc.api.ThrowingDiscAPI;
import com.chinaex123.throwing_disc.init.TDDiscTiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, ThrowingDisc.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        var discTag = tag(ThrowingDiscAPI.discTag());
        var commonTag = tag(ThrowingDiscAPI.commonDiscTag());
        var durabilityTag = tag(ThrowingDiscAPI.durabilityTag());
        var vanishingTag = tag(ThrowingDiscAPI.vanishingTag());
        var fireAspectTag = tag(ThrowingDiscAPI.fireAspectTag());

        for (DiscTier tier : ThrowingDiscAPI.tiers()) {
            ThrowingDiscAPI.addToAllTags(discTag, durabilityTag, vanishingTag, fireAspectTag, tier);
            ThrowingDiscAPI.addToTags(commonTag, tier);
        }

        tag(ItemTags.PIGLIN_LOVED).add(TDDiscTiers.GOLD.item().get());
    }
}
