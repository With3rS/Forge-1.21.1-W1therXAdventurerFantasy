package com.w1therx.adventurerfantasy.capability;


import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class ModCapabilities {
    public static Capability<IBaseStats> BASE_STATS = null;
    public static Capability<IAddStats> ADD_STATS = null;
    public static Capability<IMultStats> MULT_STATS = null;
    public static Capability<IFinalStats> FINAL_STATS = null;
    public static Capability<IDirtyStats> DIRTY_STATS = null;
    public static Capability<IIndicatorStats> INDICATOR_STATS = null;
    public static Capability<IIndependentStats> INDEPENDENT_STATS = null;
    public static Capability<IPlayerStats> PLAYER_STATS = null;

    public static void register() {
        BASE_STATS = CapabilityManager.get(new CapabilityToken<>(){});
        ADD_STATS = CapabilityManager.get(new CapabilityToken<>(){});
        MULT_STATS = CapabilityManager.get(new CapabilityToken<>(){});
        FINAL_STATS = CapabilityManager.get(new CapabilityToken<>(){});
        DIRTY_STATS = CapabilityManager.get(new CapabilityToken<>() {});
        INDICATOR_STATS = CapabilityManager.get(new CapabilityToken<>(){});
        INDEPENDENT_STATS = CapabilityManager.get(new CapabilityToken<>(){});
        PLAYER_STATS = CapabilityManager.get(new CapabilityToken<>() {});
    }
}
