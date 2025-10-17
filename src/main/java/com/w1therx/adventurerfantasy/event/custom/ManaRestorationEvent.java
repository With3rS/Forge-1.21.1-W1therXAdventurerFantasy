package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class ManaRestorationEvent extends Event {
    private final LivingEntity target;
    private final double restoreAmount;

    public ManaRestorationEvent( LivingEntity target, double restoreAmount) {
        this.target = target;
        this.restoreAmount = restoreAmount;
    }

    public LivingEntity getTarget()
    {
        return target;
    }
    public double getRestoreAmount()
    {
        return restoreAmount;
    }
}
