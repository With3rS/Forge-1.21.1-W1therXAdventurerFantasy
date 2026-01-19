package com.w1therx.adventurerfantasy.effect.general;

import net.minecraft.world.effect.MobEffect;

public record StatusEffectEntry(MobEffect effect, int duration, double amplifier, int stacks, int maxStacks, double baseChance) {}
