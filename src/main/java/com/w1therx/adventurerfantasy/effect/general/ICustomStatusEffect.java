package com.w1therx.adventurerfantasy.effect.general;

import com.w1therx.adventurerfantasy.common.enums.ElementalReaction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface ICustomStatusEffect {

    void onInitialisation(LivingEntity entity);

    void onTick(LivingEntity entity);

    void onHurt(LivingEntity entity, LivingEntity target, double value);

    void onBeingHurt(LivingEntity entity, @Nullable LivingEntity attacker, double value);

    void onHeal(LivingEntity entity, LivingEntity target, double value);

    void onBeingHealed(LivingEntity entity, @Nullable LivingEntity healer, double value);

    void onProvidingShield(LivingEntity entity, LivingEntity target, double value);

    void onReceivingShield(LivingEntity entity, @Nullable LivingEntity provider, double value);

    void onHealthConsumption(LivingEntity entity, double value);

    void onManaRestore(LivingEntity entity, double value);

    void onEffectApplication(LivingEntity entity, LivingEntity target, MobEffect effect);

    void onEffectReception(LivingEntity entity, @Nullable LivingEntity applier, MobEffect effect);

    void onThisEffectBeingResisted(LivingEntity entity, LivingEntity target);

    void onThisEffectResist(LivingEntity entity, @Nullable LivingEntity applier);

    void onEffectBeingResisted(LivingEntity entity, LivingEntity target, MobEffect effect);

    void onEffectResist(LivingEntity entity, @Nullable LivingEntity applier, MobEffect effect);

    void onJump(LivingEntity entity);

    void onManaConsume(LivingEntity entity, double amount);

    void onShieldBreak(LivingEntity entity);

    void onShieldDecay(LivingEntity entity);

    void onThisEffectStacksReachMax(LivingEntity entity);

    void onOtherEffectStacksReachMax(LivingEntity entity, MobEffect effect);

    void onBoundByLife(LivingEntity entity, @Nullable LivingEntity applier, double value);

    void onBindByLife(LivingEntity entity, LivingEntity target, double value);

    void onBondOfLifeDispel(LivingEntity entity);

    void onUseUltimate(LivingEntity entity);

    void onCrit(LivingEntity entity, LivingEntity target);

    void onReceivingCrit(LivingEntity entity, @Nullable LivingEntity attacker);

    void onOtherDispel(LivingEntity entity, MobEffect effect);

    void onAttack(LivingEntity entity);

    void onDispel(LivingEntity entity);

    void onDeath(LivingEntity entity);

    void onKill(LivingEntity entity, LivingEntity target);

    void onDodge(LivingEntity entity, @Nullable LivingEntity attacker);

    void onAttackMiss(LivingEntity entity, LivingEntity missedTarget);

    void onReactionTrigger(LivingEntity entity, @Nullable LivingEntity triggerer2, LivingEntity target, ElementalReaction reaction);

    void onReactionTriggered(LivingEntity entity, @Nullable LivingEntity triggerer1, @Nullable LivingEntity triggerer2, ElementalReaction reaction);

    void onDeathDefiance(LivingEntity entity);

    void onFall(LivingEntity entity, double distance);
}