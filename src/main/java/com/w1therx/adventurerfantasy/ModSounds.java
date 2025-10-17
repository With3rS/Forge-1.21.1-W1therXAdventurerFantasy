package com.w1therx.adventurerfantasy;

import com.w1therx.adventurerfantasy.effect.ContaminatedEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AdventurerFantasy.MOD_ID);

    public static final RegistryObject<SoundEvent> MELT_SOUND = register("reaction.melt");
    public static final RegistryObject<SoundEvent> VAPORISE_SOUND = register("reaction.vaporise");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("adventurerfantasy", name);
        return SOUND_EVENTS.register(name, ()-> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
