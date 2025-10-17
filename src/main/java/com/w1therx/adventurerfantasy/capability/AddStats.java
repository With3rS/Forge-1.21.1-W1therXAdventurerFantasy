package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class AddStats implements IAddStats {

    private final Map<StatType, Double> AddStatMap = new EnumMap<>(StatType.class);

    public AddStats() {
        StatType[] statList = StatType.values();

        for (StatType stat : Arrays.stream(statList).toList()) {
            setAddStat(stat, 0);
        }
    }

    @Override
    public Map<StatType, Double> getAddStatMap() {
        return AddStatMap;
    }

    @Override
    public void setAddStat (StatType stat, double value) {
        AddStatMap.put(stat, value);
    }

    @Override
    public double getAddStat(StatType stat) {
        return AddStatMap.getOrDefault(stat, 0D);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag AddStatMapTag = new CompoundTag();
        for (StatType stat : StatType.values()) {
            AddStatMapTag.putDouble(stat.toString(), getAddStat(stat));
        }
        tag.put("AddStats", AddStatMapTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            CompoundTag AddStatMap = tag.getCompound("AddStats");
            for (StatType stat : StatType.values()) {
                setAddStat(stat, AddStatMap.getDouble(stat.toString()));
            }
        }

    }


}
