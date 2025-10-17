package com.w1therx.adventurerfantasy.event.custom;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class DeathHandlerEvent extends Event {
    private final LivingEntity target;
    private final DamageSource cause;
    private final LivingEntity killer;

    public DeathHandlerEvent(LivingEntity target, @Nullable DamageSource cause, @Nullable LivingEntity killer) {
        this.cause = cause;
        this.target = target;
        this.killer = killer;
    }
    public DamageSource getCause()
    {
        return cause;
    }
    public @Nullable LivingEntity getTarget()
    {
        return target;
    }
    public @Nullable LivingEntity getKiller()
    {
        return killer;
    }
}
