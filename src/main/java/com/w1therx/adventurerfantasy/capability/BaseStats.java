package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class BaseStats implements IBaseStats {

    private final Map<StatType, Double> BaseStatMap = new EnumMap<>(StatType.class);

    public BaseStats() {
        StatType[] statList = StatType.values();

        for (StatType stat : Arrays.stream(statList).toList()) {
            setBaseStat(stat, stat.getBaseValue());
        }
    }

    @Override
    public Map<StatType, Double> getBaseStatMap() {
        return BaseStatMap;
    }

    @Override
    public void setBaseStat (StatType stat, double value) {
        BaseStatMap.put(stat, value);
    }

    @Override
    public double getBaseStat(StatType stat) {
        return BaseStatMap.getOrDefault(stat, 0D);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag BaseStatMapTag = new CompoundTag();
        for (StatType stat : StatType.values()) {
            BaseStatMapTag.putDouble(stat.toString(), getBaseStat(stat));
        }
        tag.put("BaseStats", BaseStatMapTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            CompoundTag BaseStatMap = tag.getCompound("BaseStats");
            for (StatType stat : StatType.values()) {
                setBaseStat(stat, BaseStatMap.getDouble(stat.toString()));
            }
        }

    }
}
