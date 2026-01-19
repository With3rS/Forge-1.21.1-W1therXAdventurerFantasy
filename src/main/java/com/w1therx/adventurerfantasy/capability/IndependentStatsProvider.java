package com.w1therx.adventurerfantasy.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;


public class IndependentStatsProvider implements ICapabilityProvider {

        public static final Capability<IIndependentStats> INDEPENDENT_STATS = CapabilityManager.get(new CapabilityToken<>() {});
    private final IndependentStats stats = new IndependentStats();
    private final LazyOptional<IIndependentStats> optional = LazyOptional.of(() -> stats);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap ==   INDEPENDENT_STATS ? optional.cast() : LazyOptional.empty();
    }
}
