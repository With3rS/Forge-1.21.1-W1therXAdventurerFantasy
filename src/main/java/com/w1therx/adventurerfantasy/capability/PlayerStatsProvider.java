package com.w1therx.adventurerfantasy.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;


public class PlayerStatsProvider implements ICapabilityProvider {

        public static final Capability<IPlayerStats> PLAYER_STATS = CapabilityManager.get(new CapabilityToken<>() {});
    private final PlayerStats stats = new PlayerStats();
    private final LazyOptional<IPlayerStats> optional = LazyOptional.of(() -> stats);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap ==   PLAYER_STATS ? optional.cast() : LazyOptional.empty();
    }
}
