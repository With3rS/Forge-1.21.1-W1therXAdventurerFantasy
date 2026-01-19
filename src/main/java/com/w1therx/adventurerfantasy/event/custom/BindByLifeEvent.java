package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class BindByLifeEvent extends Event {
    private final LivingEntity target;
    private final LivingEntity applier;
    private final double bondOfLife;

    public BindByLifeEvent(LivingEntity target, @Nullable LivingEntity applier, double  bondOfLife) {
        this.target = target;
        this.bondOfLife = bondOfLife;
        this.applier = applier;
    }
    public LivingEntity getTarget()
    {
        return target;
    }
    public LivingEntity getApplier() {
        return applier;
    }
    public double getBondOfLife()
    {
        return bondOfLife;
    }
}
