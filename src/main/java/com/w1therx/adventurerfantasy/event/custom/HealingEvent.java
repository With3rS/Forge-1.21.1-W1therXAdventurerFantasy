package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class HealingEvent extends Event {
    private final LivingEntity healer;
    private final LivingEntity target;
    private final double healAmount;

    public HealingEvent(LivingEntity healer, LivingEntity target, double healAmount) {
        this.healer = healer;
        this.target = target;
        this.healAmount = healAmount;
    }
    public LivingEntity getHealer()
    {
        return healer;
    }
    public LivingEntity getTarget()
    {
        return target;
    }
    public double getHealAmount()
    {
        return healAmount;
    }
}
