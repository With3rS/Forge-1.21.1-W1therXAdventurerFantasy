package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.RegistryObject;

public class EffectCastingEvent extends Event {
    private final LivingEntity caster;
    private final LivingEntity target;
    private final RegistryObject<MobEffect> effect;
    private final int effectDuration;
    private final double effectChance;
    private final int effectAmp;

    public EffectCastingEvent(LivingEntity caster, LivingEntity target, RegistryObject<MobEffect> effect, int effectDuration, double effectChance, int effectAmp) {
        this.caster = caster;
        this.target = target;
        this.effect = effect;
        this.effectDuration = effectDuration;
        this.effectChance = effectChance;
        this.effectAmp = effectAmp;
    }
    public LivingEntity getCaster()
    {
        return caster;
    }
    public LivingEntity getTarget()
    {
        return target;
    }
    public RegistryObject<MobEffect> getEffect()
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
    public int getEffectAmp()
    {
        return effectAmp;
    }
}
