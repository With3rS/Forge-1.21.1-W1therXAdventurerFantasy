package com.w1therx.adventurerfantasy.effect.general;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record StatusEffectInstanceEntry(int duration, double amplifier, int stacks, int maxStacks, UUID applier, CompoundTag data, boolean isInitialised) {}
