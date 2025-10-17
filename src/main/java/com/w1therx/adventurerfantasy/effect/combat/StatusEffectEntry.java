package com.w1therx.adventurerfantasy.effect.combat;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;

public record StatusEffectEntry(RegistryObject<MobEffect> effect, int duration, int amplifier, double baseChance) {}
