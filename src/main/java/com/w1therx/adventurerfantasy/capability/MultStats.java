package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class MultStats implements IMultStats {

    private final Map<StatType, Double> MultStatMap = new EnumMap<>(StatType.class);

    public MultStats() {
        StatType[] statList = StatType.values();

        for (StatType stat : Arrays.stream(statList).toList()) {
            setMultStat(stat, 1);
        }
    }

    @Override
    public Map<StatType, Double> getMultStatMap() {
        return MultStatMap;
    }

    @Override
    public void setMultStat (StatType stat, double value) {
        MultStatMap.put(stat, value);
    }

    @Override
    public double getMultStat(StatType stat) {
        return MultStatMap.getOrDefault(stat, 0D);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag MultStatMapTag = new CompoundTag();
        for (StatType stat : StatType.values()) {
            MultStatMapTag.putDouble(stat.name(), getMultStat(stat));
        }
        tag.put("MultStats", MultStatMapTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            CompoundTag MultStatMap = tag.getCompound("MultStats");
            for (StatType stat : StatType.values()) {
               setMultStat(stat, MultStatMap.getDouble(stat.name()));
            }
        }

    }
}
