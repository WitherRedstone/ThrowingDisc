package com.chinaex123.throwing_disc.init;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.entity.DiscProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface TDEntities {
    DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ThrowingDisc.MODID);

    DeferredHolder<EntityType<?>, EntityType<DiscProjectile>> DISC_ENTITY =
            ENTITY_TYPES.register("disc_entity", () -> EntityType.Builder
                    .<DiscProjectile>of(DiscProjectile::new, MobCategory.MISC)
                    .sized(0.55F, 0.25F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("disc_entity"));

    static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
