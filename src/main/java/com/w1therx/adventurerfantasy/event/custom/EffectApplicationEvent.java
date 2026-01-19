package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class EffectApplicationEvent extends Event {
    private final LivingEntity caster;
    private final LivingEntity target;
    private final MobEffect effect;
    private final int effectDuration;
    private final double effectChance;
    private final double effectAmp;
    private final int stacks;
    private final int maxStacks;
    private final boolean isCertain;

    public EffectApplicationEvent(@Nullable LivingEntity caster, LivingEntity target, MobEffect effect, int effectDuration, double effectChance, double effectAmp, int stacks, int maxStacks, boolean isCertain) {
        this.caster = caster;
        this.target = target;
        this.effect = effect;
        this.effectDuration = effectDuration;
        this.effectChance = effectChance;
        this.effectAmp = effectAmp;
        this.stacks = stacks;
        this.maxStacks = maxStacks;
        this.isCertain = isCertain;
    }
    public LivingEntity getCaster()
    {
        return caster;
    }
    public LivingEntity getTarget()
    {
        return target;
    }
    public MobEffect getEffect()
    {
        return effect;
    }
    public int getEffectDuration()
    {
        return effectDuration;
    }
    public double getEffectChance()
    {
        return effectChance;
    }
    public double getEffectAmp()
    {
        return effectAmp;
    }
    public int getStacks() {
        return stacks;
    }
    public int getMaxStacks() {
        return maxStacks;
    }
    public boolean getIsCertain() {
        return isCertain;
    }
}
