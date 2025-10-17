package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class HurtCustomEvent extends Event {
    private final LivingEntity attacker;
    private final LivingEntity target;
    private final double amount;

    public HurtCustomEvent(LivingEntity attacker, LivingEntity target, double amount) {
        this.attacker = attacker;
        this.target = target;
        this.amount = amount;
    }
    public LivingEntity getAttacker()
    {
        return attacker;
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
