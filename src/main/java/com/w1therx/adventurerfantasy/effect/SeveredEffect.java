package com.w1therx.adventurerfantasy.effect;

import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import com.w1therx.adventurerfantasy.common.enums.ElementalReaction;
import com.w1therx.adventurerfantasy.effect.general.ICustomStatusEffect;
import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectInstanceEntry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SeveredEffect extends MobEffect  implements ICustomStatusEffect {
    public SeveredEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        return super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void onInitialisation(LivingEntity entity) {
        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
            if (statsI.getActiveEffectList().containsKey(ModEffects.SEVERED_EFFECT.get())) {
                StatusEffectInstanceEntry entry = statsI.getActiveEffectData(ModEffects.SEVERED_EFFECT.get());
                CompoundTag data = entry.data();
                if (!data.contains("ticks")) {
                    data.putInt("ticks", 0);
                }
                statsI.getActiveEffectList().replace(ModEffects.SEVERED_EFFECT.get(), new StatusEffectInstanceEntry(entry.duration(), entry.amplifier(), entry.stacks(), entry.maxStacks(), entry.applier(), data, entry.isInitialised()));
            }
        });
    }

    @Override
    public void onTick(LivingEntity entity) {
        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI-> {
            if (statsI.getActiveEffectList().containsKey(ModEffects.SEVERED_EFFECT.get())) {
                StatusEffectInstanceEntry entry = statsI.getActiveEffectData(ModEffects.SEVERED_EFFECT.get());
                if (entry.data().contains("ticks")) {
                    CompoundTag data = entry.data();
                    int ticks = entry.data().getInt("ticks");
                    if (ticks >= 4) {
                        if (entity.level() instanceof ServerLevel level) {
                            level.sendParticles(ParticleTypes.ENCHANTED_HIT, entity.getX(), entity.getY() + 0.5, entity.getZ(), 1, Math.random() * 0.8, Math.random()*1.1, Math.random() * 0.8, Math.random() * 0.5 + 0.1);
                            data.remove("ticks");
                            data.putInt("ticks", 0);
                            statsI.getActiveEffectList().replace(ModEffects.SEVERED_EFFECT.get(), new StatusEffectInstanceEntry(entry.duration(), entry.amplifier(), entry.stacks(), entry.maxStacks(), entry.applier(), data, entry.isInitialised()));
                        }
                    } else {
                        data.remove("ticks");
                        data.putInt("ticks", ticks + 1);
                        statsI.getActiveEffectList().replace(ModEffects.SEVERED_EFFECT.get(), new StatusEffectInstanceEntry(entry.duration(), entry.amplifier(), entry.stacks(), entry.maxStacks(), entry.applier(), data, entry.isInitialised()));
                    }
                }
            }
        });
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
    public void onAttack(LivingEntity entity) {

    }

    @Override
    public void onDispel(LivingEntity entity) {

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
