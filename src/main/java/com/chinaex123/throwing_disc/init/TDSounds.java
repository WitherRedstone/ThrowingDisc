package com.chinaex123.throwing_disc.init;

import com.chinaex123.throwing_disc.ThrowingDisc;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface TDSounds {
    DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ThrowingDisc.MODID);

    DeferredHolder<SoundEvent, SoundEvent> DISC_THROW = register("item.disc.throw");
    DeferredHolder<SoundEvent, SoundEvent> DISC_BOUNCE = register("item.disc.bounce");
    DeferredHolder<SoundEvent, SoundEvent> DISC_RETURN = register("item.disc.return");
    DeferredHolder<SoundEvent, SoundEvent> DISC_CATCH = register("item.disc.catch");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ThrowingDisc.id(name)));
    }

    static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
