package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface IAddStats {

    Map<StatType, Double> getAddStatMap();
    void setAddStat(StatType stat, double value);
    double getAddStat(StatType stat);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);

}
