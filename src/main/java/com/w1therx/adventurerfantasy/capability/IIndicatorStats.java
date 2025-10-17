package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.DmgInstanceType;
import com.w1therx.adventurerfantasy.common.enums.ElementType;
import net.minecraft.nbt.CompoundTag;


import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IIndicatorStats {
    int getAge();
    void setAge(int value);

    int getLifetime();
    void setLifetime(int value);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}
