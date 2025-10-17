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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;


public class AddStatsProvider implements ICapabilityProvider {

        public static final Capability<IAddStats> ADD_STATS = CapabilityManager.get(new CapabilityToken<>() {});
    private final AddStats stats = new AddStats();
    private final LazyOptional<IAddStats> optional = LazyOptional.of(() -> stats);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap ==   ADD_STATS ? optional.cast() : LazyOptional.empty();
    }
}
