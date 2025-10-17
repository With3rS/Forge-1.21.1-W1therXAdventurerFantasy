package com.w1therx.adventurerfantasy.event;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import com.w1therx.adventurerfantasy.effect.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;


@Mod.EventBusSubscriber(modid = AdventurerFantasy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)

public class EffectEvents {
    private static final Logger log = LoggerFactory.getLogger(EffectEvents.class);

    @SubscribeEvent
    public static void onEffectRemoval (MobEffectEvent.Remove event) {
        if (event.getEntity().level().isClientSide || event.getEffectInstance() == null) return;
        MobEffect effect = event.getEffect();
        LivingEntity entity = event.getEntity();
        if (effect == ModEffects.CAUTION_EFFECT.get()) {
            int amp = event.getEffectInstance().getAmplifier();
            LazyOptional<IAddStats> statsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!statsL.isPresent()) return;
            IAddStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            stats.setAddStat(StatType.KNOCKBACK_RES, stats.getAddStat(StatType.KNOCKBACK_RES) - 0.5);
            stats.setAddStat(StatType.ARMOR, stats.getAddStat(StatType.ARMOR) - 0.5 * amp);
        }
    }

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

            independentNewStats.deserializeNBT(independentOldStats.serializeNBT());
            baseNewStats.deserializeNBT(baseOldStats.serializeNBT());
            addNewStats.deserializeNBT(addOldStats.serializeNBT());
            multNewStats.deserializeNBT(multOldStats.serializeNBT());

            StatType[] statList = StatType.values();
            for (StatType stat : Arrays.stream(statList).toList()) {
                dirtyStats.setDirtyStat(stat, true);
            }

            oldPlayer.invalidateCaps();
        } else {
            LazyOptional<IIndependentStats> independentNewStatsL = newPlayer.getCapability(ModCapabilities.INDEPENDENT_STATS);
            LazyOptional<IBaseStats> baseNewStatsL = newPlayer.getCapability(ModCapabilities.BASE_STATS);
            LazyOptional<IAddStats> addNewStatsL = newPlayer.getCapability(ModCapabilities.ADD_STATS);
            LazyOptional<IMultStats> multNewStatsL = newPlayer.getCapability(ModCapabilities.MULT_STATS);
            LazyOptional<IDirtyStats> dirtyStatsL = newPlayer.getCapability(ModCapabilities.DIRTY_STATS);

            if ((!independentNewStatsL.isPresent()) || (!baseNewStatsL.isPresent()) || (!addNewStatsL.isPresent()) || (!multNewStatsL.isPresent()) || (!dirtyStatsL.isPresent()))
                return;

            IIndependentStats independentNewStats = independentNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IBaseStats baseNewStats = baseNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IAddStats addNewStats = addNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IMultStats multNewStats = multNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IDirtyStats dirtyStats = dirtyStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            if (ModEffects.CAUTION_EFFECT.getKey() != null) {
                Holder<MobEffect> cautionHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ModEffects.CAUTION_EFFECT.getKey());
                if (newPlayer.hasEffect(cautionHolder)) {
                    int amp = Objects.requireNonNull(newPlayer.getEffect(cautionHolder)).getAmplifier();
                    addNewStats.setAddStat(StatType.KNOCKBACK_RES, addNewStats.getAddStat(StatType.KNOCKBACK_RES) + 0.5);
                    addNewStats.setAddStat(StatType.ARMOR, addNewStats.getAddStat(StatType.ARMOR) + 0.5 * amp);

                    dirtyStats.setDirtyStat(StatType.KNOCKBACK_RES, true);
                    dirtyStats.setDirtyStat(StatType.ARMOR, true);
                }
            }
        }
    }
}
