package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class BindByLifeEvent extends Event {
    private final LivingEntity target;
    private final double bondOfLife;

    public BindByLifeEvent(LivingEntity target, double  bondOfLife) {
        this.target = target;
        this.bondOfLife = bondOfLife;
    }
    public LivingEntity getTarget()
    {
        return target;
    }
    public double getBondOfLife()
    {
        return bondOfLife;
    }
}
