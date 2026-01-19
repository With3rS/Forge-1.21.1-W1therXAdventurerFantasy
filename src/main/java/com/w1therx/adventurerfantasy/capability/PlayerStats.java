package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.util.ModGeneralUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class PlayerStats implements IPlayerStats {

    private LivingEntity mount = null;
    private double mountHealth = 0;
    private double mountMaxHealth = 0;
    private int milkDrinkingTime = -1;

    @Override
    public void setMount(LivingEntity entity) {
        mount = entity;
    }

    @Override
    public LivingEntity getMount() {
        return mount;
    }

    @Override
    public void setMountHealth(double value) {
        mountHealth = value;
    }

    @Override
    public double getMountHealth() {return mountHealth;}

    @Override
    public void setMountMaxHealth(double value) {
        mountMaxHealth = value;
    }

    @Override
    public double getMountMaxHealth() {return mountMaxHealth;}

    @Override
    public void setMilkDrinkingTime(int value) { milkDrinkingTime = value; }

    @Override
    public int getMilkDrinkingTime() {return milkDrinkingTime;}

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (mount != null) {
            tag.putUUID("Mount", mount.getUUID());
        } else {
            tag.putUUID("Mount", ModGeneralUtils.EMPTY_UUID);
        }

        tag.putDouble("MountHealth", mountHealth);
        tag.putDouble("MountMaxHealth", mountMaxHealth);
        tag.putInt("MilkDrinkingTime", milkDrinkingTime);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt, LivingEntity entity) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            UUID mountUuid = tag.getUUID("Mount");
            if (mountUuid.equals(ModGeneralUtils.EMPTY_UUID)) {
                setMount(null);
            } else if (entity.level() instanceof ServerLevel level) {
                Entity mount = level.getEntity(mountUuid);
                setMount((LivingEntity) mount);
            } else {
                mount = null;
            }

            setMountHealth(tag.getDouble("MountHealth"));
            setMountMaxHealth(tag.getDouble("MountMaxHealth"));
            setMilkDrinkingTime(tag.getInt("MilkDrinkingTime"));
        }
    }
}
