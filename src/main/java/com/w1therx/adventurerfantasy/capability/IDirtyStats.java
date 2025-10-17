package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface IDirtyStats {

    Map<StatType, Boolean> getDirtyStatMap();
    void setDirtyStat(StatType stat, Boolean value);
    Boolean getDirtyStat(StatType stat);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
