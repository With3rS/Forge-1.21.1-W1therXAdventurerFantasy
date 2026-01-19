package com.w1therx.adventurerfantasy.event;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


@Mod.EventBusSubscriber(modid = AdventurerFantasy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)

public class RespawnEvents {
    private static final Logger log = LoggerFactory.getLogger(RespawnEvents.class);

    @SubscribeEvent
    public static void onPlayerCopy(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        if (newPlayer.level().isClientSide()) return;

        if (event.isWasDeath()) {

            newPlayer.removeTag("dying");
            newPlayer.removeTag("capsToDeserialize");
            oldPlayer.reviveCaps();
            LazyOptional<IIndependentStats> independentOldStatsL = oldPlayer.getCapability(ModCapabilities.INDEPENDENT_STATS);
            LazyOptional<IBaseStats> baseOldStatsL = oldPlayer.getCapability(ModCapabilities.BASE_STATS);
            LazyOptional<IAddStats> addOldStatsL = oldPlayer.getCapability(ModCapabilities.ADD_STATS);
            LazyOptional<IMultStats> multOldStatsL = oldPlayer.getCapability(ModCapabilities.MULT_STATS);
            LazyOptional<IFinalStats> finalOldStatsL = oldPlayer.getCapability(ModCapabilities.FINAL_STATS);

            LazyOptional<IIndependentStats> independentNewStatsL = newPlayer.getCapability(ModCapabilities.INDEPENDENT_STATS);
            LazyOptional<IBaseStats> baseNewStatsL = newPlayer.getCapability(ModCapabilities.BASE_STATS);
            LazyOptional<IAddStats> addNewStatsL = newPlayer.getCapability(ModCapabilities.ADD_STATS);
            LazyOptional<IMultStats> multNewStatsL = newPlayer.getCapability(ModCapabilities.MULT_STATS);
            LazyOptional<IFinalStats> finalNewStatsL = newPlayer.getCapability(ModCapabilities.FINAL_STATS);
            LazyOptional<IDirtyStats> dirtyStatsL = newPlayer.getCapability(ModCapabilities.DIRTY_STATS);

            if ((!independentOldStatsL.isPresent()) || (!baseOldStatsL.isPresent()) || (!addOldStatsL.isPresent()) || (!multOldStatsL.isPresent()) || (!finalOldStatsL.isPresent()) || (!independentNewStatsL.isPresent()) || (!baseNewStatsL.isPresent()) || (!addNewStatsL.isPresent()) || (!multNewStatsL.isPresent()) || (!finalNewStatsL.isPresent()) || (!dirtyStatsL.isPresent()))
                return;

            IIndependentStats independentOldStats = independentOldStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IBaseStats baseOldStats = baseOldStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IAddStats addOldStats = addOldStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IMultStats multOldStats = multOldStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            IIndependentStats independentNewStats = independentNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IBaseStats baseNewStats = baseNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IAddStats addNewStats = addNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IMultStats multNewStats = multNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IDirtyStats dirtyStats = dirtyStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            independentNewStats.deserializeNBT(independentOldStats.serializeNBT(), newPlayer);
            baseNewStats.deserializeNBT(baseOldStats.serializeNBT());
            addNewStats.deserializeNBT(addOldStats.serializeNBT());
            multNewStats.deserializeNBT(multOldStats.serializeNBT());

            StatType[] statList = StatType.values();
            for (StatType stat : Arrays.stream(statList).toList()) {
                dirtyStats.setDirtyStat(stat, true);
            }

            independentNewStats.setIndependentStat(IndependentStatType.HEALTH, baseNewStats.getBaseStat(StatType.MAX_HEALTH));
            oldPlayer.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn (PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI ->
                    player.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF -> {
                        statsI.setIndependentStat(IndependentStatType.HEALTH, statsF.getFinalStat(StatType.MAX_HEALTH));
                    }));
        }
    }
}
