package com.w1therx.adventurerfantasy.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class IndicatorStats implements IIndicatorStats {

    private int Lifetime = -1;
    private int Age = 0;


    @Override
    public int getLifetime() { return Lifetime; }

    @Override
    public void setLifetime(int value) { Lifetime = value; }

    @Override
    public int getAge() { return Age; }

    @Override
    public void setAge(int value) { Age = value; }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Lifetime", getLifetime());
        tag.putInt("Age", getAge());

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            setLifetime(tag.getInt("Lifetime"));
            setAge(tag.getInt("Age"));
        }

    }

}
