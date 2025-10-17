package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.DmgInstanceType;
import com.w1therx.adventurerfantasy.common.enums.ElementType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IFinalStats {

    Map<StatType, Double> getFinalStatMap();
    void setFinalStat(StatType stat, double value);
    double getFinalStat(StatType stat);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
