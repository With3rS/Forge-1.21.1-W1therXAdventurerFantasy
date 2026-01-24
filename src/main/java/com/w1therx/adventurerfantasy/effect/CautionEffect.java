package com.w1therx.adventurerfantasy.effect;

import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.common.enums.ElementalReaction;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import com.w1therx.adventurerfantasy.effect.general.ICustomStatusEffect;
import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectInstanceEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CautionEffect extends MobEffect implements ICustomStatusEffect {
    public CautionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        return super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void onEffectStarted (@NotNull LivingEntity pLivingEntity, int pAmplifier) {
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public String effectDescription(LivingEntity entity) {
        LazyOptional<IIndependentStats> optional = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (optional.isPresent()) {
            IIndependentStats stats = optional.orElseThrow(() -> new IllegalStateException("Failed an impossible-to-fail capability check"));
            StatusEffectInstanceEntry entry = stats.getActiveEffectData(ModEffects.CAUTION_EFFECT.get());
            CompoundTag data = entry.data();
            double knockbackRes = 0;
            if (data.contains("knockback_res"))  {
                knockbackRes = data.getDouble("knockback_res") * 100;
            }
            return "Increases defense by " + entry.amplifier() + " points and knockback resistance by " + knockbackRes + "%.";
        } else return "";
    }


    @Override
    public void onInitialisation(LivingEntity entity){
        if (entity.level().isClientSide) return;
        LazyOptional<IAddStats> statsL = entity.getCapability(ModCapabilities.ADD_STATS);
        if (!statsL.isPresent()) return;
        IAddStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        LazyOptional<IDirtyStats> dirtyL = entity.getCapability(ModCapabilities.DIRTY_STATS);
        if (!dirtyL.isPresent()) return;
        IDirtyStats dirty = dirtyL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        LazyOptional<IIndependentStats> independentL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!independentL.isPresent()) return;
        IIndependentStats independent = independentL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        if (independent.getActiveEffectList().containsKey(ModEffects.CAUTION_EFFECT.get())) {
            StatusEffectInstanceEntry effectData = independent.getActiveEffectData(ModEffects.CAUTION_EFFECT.get());

            double amp = effectData.amplifier();
            double knockbackRes = 0;


            if (effectData.data().contains("knockback_res")) {
             knockbackRes = effectData.data().getDouble("knockback_res");
            }

            stats.setAddStat(StatType.KNOCKBACK_RES, stats.getAddStat(StatType.KNOCKBACK_RES) + knockbackRes);
            stats.setAddStat(StatType.ARMOR, stats.getAddStat(StatType.ARMOR) + amp);

            dirty.setDirtyStat(StatType.KNOCKBACK_RES, true);
            dirty.setDirtyStat(StatType.ARMOR, true);
        }
    }

    @Override
    public void onTick(LivingEntity entity) {
    }

    @Override
    public void onHurt(LivingEntity entity, LivingEntity target, double value) {

    }

    @Override
    public void onBeingHurt(LivingEntity entity, @Nullable LivingEntity attacker, double value) {

    }

    @Override
    public void onHeal(LivingEntity entity, LivingEntity target, double value) {

    }

    @Override
    public void onBeingHealed(LivingEntity entity, @Nullable LivingEntity healer, double value) {

    }

    @Override
    public void onProvidingShield(LivingEntity entity, LivingEntity target, double value) {

    }

    @Override
    public void onReceivingShield(LivingEntity entity, @Nullable LivingEntity provider, double value) {

    }

    @Override
    public void onHealthConsumption(LivingEntity entity, double value) {

    }

    @Override
    public void onManaRestore(LivingEntity entity, double value) {

    }

    @Override
    public void onEffectApplication(LivingEntity entity, LivingEntity target, MobEffect effect) {

    }

    @Override
    public void onEffectReception(LivingEntity entity, @Nullable LivingEntity applier, MobEffect effect) {

    }

    @Override
    public void onThisEffectBeingResisted(LivingEntity entity, LivingEntity target) {

    }

    @Override
    public void onThisEffectResist(LivingEntity entity, @Nullable LivingEntity applier) {

    }

    @Override
    public void onEffectBeingResisted(LivingEntity entity, LivingEntity target, MobEffect effect) {

    }

    @Override
    public void onEffectResist(LivingEntity entity, @Nullable LivingEntity applier, MobEffect effect) {

    }

    @Override
    public void onJump(LivingEntity entity) {

    }

    @Override
    public void onManaConsume(LivingEntity entity, double amount) {

    }

    @Override
    public void onShieldBreak(LivingEntity entity) {

    }

    @Override
    public void onShieldDecay(LivingEntity entity) {

    }

    @Override
    public void onThisEffectStacksReachMax(LivingEntity entity) {

    }

    @Override
    public void onOtherEffectStacksReachMax(LivingEntity entity, MobEffect effect) {

    }

    @Override
    public void onBoundByLife(LivingEntity entity, @Nullable LivingEntity applier, double value) {

    }

    @Override
    public void onBindByLife(LivingEntity entity, LivingEntity target, double value) {

    }

    @Override
    public void onBondOfLifeDispel(LivingEntity entity) {

    }

    @Override
    public void onUseUltimate(LivingEntity entity) {

    }

    @Override
    public void onCrit(LivingEntity entity, LivingEntity target) {

    }

    @Override
    public void onReceivingCrit(LivingEntity entity, @Nullable LivingEntity attacker) {

    }

    @Override
    public void onOtherDispel(LivingEntity entity, MobEffect effect) {

    }

    @Override
    public void onAttack(LivingEntity entity){}

    @Override
    public void onDispel(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        System.out.println("[DEBUG] Caution effect is being dispelled");
        LazyOptional<IAddStats> statsL = entity.getCapability(ModCapabilities.ADD_STATS);
        if (!statsL.isPresent()) return;
        IAddStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        LazyOptional<IDirtyStats> dirtyL = entity.getCapability(ModCapabilities.DIRTY_STATS);
        if (!dirtyL.isPresent()) return;
        IDirtyStats dirty = dirtyL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        LazyOptional<IIndependentStats> independentL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!independentL.isPresent()) return;
        IIndependentStats independent = independentL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        if (independent.getActiveEffectList().containsKey(ModEffects.CAUTION_EFFECT.get())) {
            StatusEffectInstanceEntry effectData = independent.getActiveEffectData(ModEffects.CAUTION_EFFECT.get());

            double amp = effectData.amplifier();
            int stacks = effectData.stacks();
            double knockbackRes = 0;


            if (effectData.data().contains("knockback_res")) {
                knockbackRes = effectData.data().getDouble("knockback_res");
            }

            stats.setAddStat(StatType.KNOCKBACK_RES, stats.getAddStat(StatType.KNOCKBACK_RES) - knockbackRes);
            stats.setAddStat(StatType.ARMOR, stats.getAddStat(StatType.ARMOR) - amp);

            dirty.setDirtyStat(StatType.KNOCKBACK_RES, true);
            dirty.setDirtyStat(StatType.ARMOR, true);
        }
    }

    @Override
    public void onDeath(LivingEntity entity) {

    }

    @Override
    public void onKill(LivingEntity entity, LivingEntity target) {

    }

    @Override
    public void onDodge(LivingEntity entity, @Nullable LivingEntity attacker) {

    }

    @Override
    public void onAttackMiss(LivingEntity entity, LivingEntity missedTarget) {

    }

    @Override
    public void onReactionTrigger(LivingEntity entity, @Nullable LivingEntity triggerer2, LivingEntity target, ElementalReaction reaction) {

    }

    @Override
    public void onReactionTriggered(LivingEntity entity, @Nullable LivingEntity triggerer1, @Nullable LivingEntity triggerer2, ElementalReaction reaction) {

    }

    @Override
    public void onDeathDefiance(LivingEntity entity) {

    }

    @Override
    public void onFall(LivingEntity entity, double distance) {

    }
}
