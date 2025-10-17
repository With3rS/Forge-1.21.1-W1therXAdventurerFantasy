package com.w1therx.adventurerfantasy.capability;

import net.minecraft.core.Direction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;


public class IndicatorStatsProvider implements ICapabilityProvider {

        public static final Capability<IIndicatorStats> INDICATOR_STATS = CapabilityManager.get(new CapabilityToken<>() {});
    private final IndicatorStats stats = new IndicatorStats();
    private final LazyOptional<IIndicatorStats> optional = LazyOptional.of(() -> stats);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap ==   INDICATOR_STATS ? optional.cast() : LazyOptional.empty();
    }
}
