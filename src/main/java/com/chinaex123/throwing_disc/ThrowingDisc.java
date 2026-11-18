package com.chinaex123.throwing_disc;

import com.chinaex123.throwing_disc.init.TDItems;
import com.chinaex123.throwing_disc.init.TDCreativeTabs;
import com.chinaex123.throwing_disc.init.TDEntities;
import com.chinaex123.throwing_disc.init.TDSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ThrowingDisc.MODID)
public class ThrowingDisc {
    public static final String MODID = "throwing_disc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ThrowingDisc(IEventBus modEventBus, ModContainer modContainer) {
        TDItems.registerBuiltins();
        TDItems.register(modEventBus);
        TDEntities.register(modEventBus);
        TDSounds.register(modEventBus);
        TDCreativeTabs.register(modEventBus);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
