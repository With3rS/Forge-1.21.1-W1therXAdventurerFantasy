package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class AttackCustomEvent extends Event {
    private final LivingEntity attacker;

    public AttackCustomEvent(LivingEntity attacker) {
        this.attacker = attacker;
    }
    public LivingEntity getAttacker()
    {
        return attacker;
    }
}
