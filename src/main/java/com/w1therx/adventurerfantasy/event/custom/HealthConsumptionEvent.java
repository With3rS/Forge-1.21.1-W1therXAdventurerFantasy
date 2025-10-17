package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class HealthConsumptionEvent extends Event {
    private final LivingEntity target;
    private final double amount;

    public HealthConsumptionEvent(LivingEntity target, double amount) {
        this.target = target;
        this.amount = amount;
    }
    public LivingEntity getTarget()
    {
        return target;
    }
    public double getAmount()
    {
        return amount;
    }
}
