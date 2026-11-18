package com.chinaex123.throwing_disc.data;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.data.enchantments.ModEnchantmentProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ThrowingDisc.MODID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));

        DatapackBuiltinEntriesProvider enchantments = new DatapackBuiltinEntriesProvider(
                packOutput,
                lookupProvider,
                ModEnchantmentProvider.DATA_BUILDER,
                Set.of(ThrowingDisc.MODID)
        );
        generator.addProvider(event.includeServer(), enchantments);
        CompletableFuture<HolderLookup.Provider> fullLookup = enchantments.getRegistryProvider();

        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, fullLookup,
                CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModEnchantmentTagsProvider(packOutput, fullLookup, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelsProvider(packOutput, event.getExistingFileHelper()));
    }
}
