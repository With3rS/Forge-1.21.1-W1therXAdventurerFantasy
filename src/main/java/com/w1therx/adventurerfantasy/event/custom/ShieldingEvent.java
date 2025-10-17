package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class ShieldingEvent extends Event {
    private final LivingEntity shieldProvider;
    private final LivingEntity target;
    private final double shieldStrength;
    private final int shieldDuration;

    public ShieldingEvent(LivingEntity shieldProvider, LivingEntity target, double shieldStrength, int shieldDuration) {
        this.shieldProvider = shieldProvider;
        this.target = target;
        this.shieldStrength = shieldStrength;
        this.shieldDuration = shieldDuration;
    }
    public LivingEntity getShieldProvider()
    {
        return shieldProvider;
    }
    public LivingEntity getTarget()
    {
        return target;
    }
    public double getShieldStrength()
    {
        return shieldStrength;
    }
    public int getShieldDuration() {
        return shieldDuration;
    }
}
