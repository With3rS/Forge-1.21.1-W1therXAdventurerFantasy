package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface IMultStats {

    Map<StatType, Double> getMultStatMap();
    void setMultStat(StatType stat, double value);
    double getMultStat(StatType stat);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
