package com.w1therx.adventurerfantasy.capability;

import com.w1therx.adventurerfantasy.common.enums.DmgInstanceType;
import com.w1therx.adventurerfantasy.common.enums.ElementType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;


public class FinalStatsProvider implements ICapabilityProvider {

        public static final Capability<IFinalStats> FINAL_STATS = CapabilityManager.get(new CapabilityToken<>() {});
    private final FinalStats stats = new FinalStats();
    private final LazyOptional<IFinalStats> optional = LazyOptional.of(() -> stats);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap ==   FINAL_STATS ? optional.cast() : LazyOptional.empty();
    }
}
