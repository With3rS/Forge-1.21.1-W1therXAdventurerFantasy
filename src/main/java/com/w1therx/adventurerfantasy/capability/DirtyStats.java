package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class DirtyStats implements IDirtyStats {

    private final Map<StatType, Boolean> DirtyStatMap = new EnumMap<>(StatType.class);

    public DirtyStats() {
        StatType[] statList = StatType.values();

        for (StatType stat : Arrays.stream(statList).toList()) {
            setDirtyStat(stat, false);
        }
    }

    @Override
    public Map<StatType, Boolean> getDirtyStatMap() {
        return DirtyStatMap;
    }

    @Override
    public void setDirtyStat (StatType stat, Boolean value) {
        DirtyStatMap.put(stat, value);
    }

    @Override
    public Boolean getDirtyStat(StatType stat) {
        return DirtyStatMap.getOrDefault(stat, false);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag DirtyStatMapTag = new CompoundTag();
        for (StatType stat : StatType.values()) {
            DirtyStatMapTag.putBoolean(stat.name(), getDirtyStat(stat));
        }
        tag.put("DirtyStats", DirtyStatMapTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt instanceof CompoundTag tag) {
            CompoundTag DirtyStatMap = tag.getCompound("DirtyStats");
            for (StatType stat : StatType.values()) {
                setDirtyStat(stat, DirtyStatMap.getBoolean(stat.name()));
            }
        }

    }
}
