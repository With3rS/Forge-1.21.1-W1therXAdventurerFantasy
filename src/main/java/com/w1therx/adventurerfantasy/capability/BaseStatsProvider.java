package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;


public class BaseStatsProvider implements ICapabilityProvider {

        public static final Capability<IBaseStats> BASE_STATS = CapabilityManager.get(new CapabilityToken<>() {});
    private final BaseStats stats = new BaseStats();
    private final LazyOptional<IBaseStats> optional = LazyOptional.of(() -> stats);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap ==   BASE_STATS ? optional.cast() : LazyOptional.empty();
    }
}
