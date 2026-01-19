package com.w1therx.adventurerfantasy.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public interface IPlayerStats {

    LivingEntity getMount();
    void setMount(LivingEntity mount);

    double getMountHealth();
    void setMountHealth(double value);

    double getMountMaxHealth();
    void setMountMaxHealth(double value);

    int getMilkDrinkingTime();
    void setMilkDrinkingTime(int value);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt, LivingEntity entity);
}
