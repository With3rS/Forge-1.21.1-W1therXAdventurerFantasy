package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.DmgInstanceType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class FinalStats implements IFinalStats {

    private final Map<StatType, Double> FinalStatMap = new EnumMap<>(StatType.class);

    public FinalStats() {
        StatType[] statList = StatType.values();

        for (StatType stat : Arrays.stream(statList).toList()) {
            setFinalStat(stat, stat.getBaseValue());
        }
    }

    @Override
    public Map<StatType, Double> getFinalStatMap() {
        return FinalStatMap;
    }

    @Override
    public void setFinalStat (StatType stat, double value) {
        FinalStatMap.put(stat, value);
    }

    @Override
    public double getFinalStat(StatType stat) {
        return FinalStatMap.getOrDefault(stat, 0D);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag FinalStatMapTag = new CompoundTag();
        for (StatType stat : StatType.values()) {
            FinalStatMapTag.putDouble(stat.name(), getFinalStat(stat));
        }
        tag.put("FinalStats", FinalStatMapTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            CompoundTag FinalStatMap = tag.getCompound("FinalStats");
            for (StatType stat : StatType.values()) {
                setFinalStat(stat, FinalStatMap.getDouble(stat.name()));
            }
        }

    }
}
