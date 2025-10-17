package com.w1therx.adventurerfantasy.effect;

import com.w1therx.adventurerfantasy.capability.IAddStats;
import com.w1therx.adventurerfantasy.capability.IDirtyStats;
import com.w1therx.adventurerfantasy.capability.IIndicatorStats;
import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Objects;

public class CautionEffect extends MobEffect {
    public CautionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (ModEffects.CAUTION_EFFECT.getKey() != null) {
            Holder<MobEffect> cautionHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ModEffects.CAUTION_EFFECT.getKey());
            if (pLivingEntity.hasEffect(cautionHolder)) {
                if (Objects.requireNonNull(pLivingEntity.getEffect(cautionHolder)).getDuration() <= 1) {
                LazyOptional<IAddStats> statsL = pLivingEntity.getCapability(ModCapabilities.ADD_STATS);
                if (!statsL.isPresent()) return super.applyEffectTick(pLivingEntity, pAmplifier);
                IAddStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                LazyOptional<IDirtyStats> dirtyL = pLivingEntity.getCapability(ModCapabilities.DIRTY_STATS);
                if (!dirtyL.isPresent()) return super.applyEffectTick(pLivingEntity, pAmplifier);
                IDirtyStats dirty = dirtyL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                int amp = Objects.requireNonNull(pLivingEntity.getEffect(cautionHolder)).getAmplifier();
                stats.setAddStat(StatType.KNOCKBACK_RES, stats.getAddStat(StatType.KNOCKBACK_RES) - 0.5);
                stats.setAddStat(StatType.ARMOR, stats.getAddStat(StatType.ARMOR) - 0.5 * amp);

                dirty.setDirtyStat(StatType.KNOCKBACK_RES, true);
                dirty.setDirtyStat(StatType.ARMOR, true);
                }
            }
        }
        return super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void onEffectStarted (LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) return;
        LazyOptional<IAddStats> statsL = pLivingEntity.getCapability(ModCapabilities.ADD_STATS);
        if (!statsL.isPresent()) return;
        IAddStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        LazyOptional<IDirtyStats> dirtyL = pLivingEntity.getCapability(ModCapabilities.DIRTY_STATS);
        if (!dirtyL.isPresent()) return;
        IDirtyStats dirty = dirtyL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        stats.setAddStat(StatType.KNOCKBACK_RES, stats.getAddStat(StatType.KNOCKBACK_RES) + 0.5);
        stats.setAddStat(StatType.ARMOR, stats.getAddStat(StatType.ARMOR) + 0.5 * pAmplifier);

        dirty.setDirtyStat(StatType.KNOCKBACK_RES, true);
        dirty.setDirtyStat(StatType.ARMOR, true);

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
