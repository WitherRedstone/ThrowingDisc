package com.chinaex123.throwing_disc.init;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.api.ThrowingDiscAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface TDCreativeTabs {
    DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThrowingDisc.MODID);

    DeferredHolder<CreativeModeTab, CreativeModeTab> THROWING_DISC_TAB =
            CREATIVE_MODE_TABS.register("throwing_disc", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ThrowingDisc.MODID))
                    .icon(() -> new ItemStack(TDDiscTiers.DIAMOND.item().get()))
                    .displayItems((parameters, output) ->
                            ThrowingDiscAPI.tiers().forEach(tier -> output.accept(tier.item().get())))
                    .build());

    static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
