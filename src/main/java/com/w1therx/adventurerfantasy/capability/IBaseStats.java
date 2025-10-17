package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface IBaseStats {

    Map<StatType, Double> getBaseStatMap();
    void setBaseStat(StatType stat, double value);
    double getBaseStat(StatType stat);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
