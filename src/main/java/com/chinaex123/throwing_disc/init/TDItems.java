package com.chinaex123.throwing_disc.init;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.api.ThrowingDiscAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface TDItems {
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(ThrowingDisc.MODID);

    static void registerBuiltins() {
        ThrowingDiscAPI.registerAll(
                TDDiscTiers.COPPER,
                TDDiscTiers.IRON,
                TDDiscTiers.GOLD,
                TDDiscTiers.DIAMOND,
                TDDiscTiers.NETHERITE
        );
    }

    static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
