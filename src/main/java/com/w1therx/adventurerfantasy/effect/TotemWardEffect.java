package com.w1therx.adventurerfantasy.effect;

import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import com.w1therx.adventurerfantasy.common.enums.ElementalReaction;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import com.w1therx.adventurerfantasy.effect.general.ICustomStatusEffect;
import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import com.w1therx.adventurerfantasy.effect.general.StatusEffectInstanceEntry;
import com.w1therx.adventurerfantasy.event.custom.DeathHandlerEvent;
import com.w1therx.adventurerfantasy.event.custom.ShieldingEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TotemWardEffect extends MobEffect implements ICustomStatusEffect {
    public TotemWardEffect(MobEffectCategory pCategory, int pColor) {
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
    public String effectDescription(LivingEntity entity) {
        return "Allows to defy death once, restoring a certain amount of health and gaining a temporary shield and invulnerability.";
    }

    @Override
    public void onInitialisation(LivingEntity entity){
    }

    @Override
    public void onTick(LivingEntity entity) {}

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
        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
            entity.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF -> {
                MinecraftForge.EVENT_BUS.post(new ShieldingEvent(entity, entity, statsF.getFinalStat(StatType.MAX_HEALTH) * 0.4, 640));
            statsI.addActiveEffectEntry(ModEffects.BLESSING_OF_UNDYING_EFFECT.get(), new StatusEffectInstanceEntry((int) (5 * statsF.getFinalStat(StatType.INVULNERABLE_DURATION)), 0, 0, 0, entity.getUUID(), new CompoundTag(), false), entity);
            statsI.addActiveEffectEntry(MobEffects.FIRE_RESISTANCE.get(), new StatusEffectInstanceEntry(640, 1, 0, 0, entity.getUUID(), new CompoundTag(), false), entity);
           if (entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() == Items.TOTEM_OF_UNDYING) {
                entity.getItemBySlot(EquipmentSlot.MAINHAND).consume(1, entity);
            } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND).getItem() == Items.TOTEM_OF_UNDYING) {
                entity.getItemBySlot(EquipmentSlot.OFFHAND).consume(1, entity);
            } else {
                MinecraftForge.EVENT_BUS.post(new DeathHandlerEvent(entity, null, entity));
            }

            });
        });
    }

    @Override
    public void onFall(LivingEntity entity, double distance) {

    }
}
