package com.w1therx.adventurerfantasy.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.commands.ModCommands;
import com.w1therx.adventurerfantasy.commands.ModGameRules;
import com.w1therx.adventurerfantasy.common.enums.*;
import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import com.w1therx.adventurerfantasy.effect.general.*;
import com.w1therx.adventurerfantasy.event.custom.*;
import com.w1therx.adventurerfantasy.network.ModNetworking;
import com.w1therx.adventurerfantasy.network.packet.AdditionalJumpInputReceiver;
import com.w1therx.adventurerfantasy.network.packet.ClientStatReceiver;
import com.w1therx.adventurerfantasy.network.packet.DashInputReceiver;
import com.w1therx.adventurerfantasy.network.packet.InteractInputReceiver;
import com.w1therx.adventurerfantasy.util.ModGeneralUtils;
import com.w1therx.adventurerfantasy.util.ModTags;
import net.minecraft.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.lang.Math.max;


@Mod.EventBusSubscriber(modid = AdventurerFantasy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)


//Add: reaction-bound custom event-bound dmg calculator, DoT-bound custom-event-bound dmg calculator, aggro calculator,
// AI overriding, teams, ability for items/<gear>/effects to modify stats, GUIs


public class ModEvents {
    private static final Logger log = LoggerFactory.getLogger(ModEvents.class);
    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "final_stats"), new FinalStatsProvider());
            event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "base_stats"), new BaseStatsProvider());
            event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "add_stats"), new AddStatsProvider());
            event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "mult_stats"), new MultStatsProvider());
            event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "dirty_stats"), new DirtyStatsProvider());

            event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "independent_stats"), new IndependentStatsProvider());
            event.getObject().addTag("shouldSetMaxHP");

            if ((event.getObject() instanceof ArmorStand)) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "indicator_stats"), new IndicatorStatsProvider());
            }
            if (event.getObject() instanceof Player) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(AdventurerFantasy.MOD_ID, "player_stats"), new PlayerStatsProvider());
            }
        }
    }

    @SubscribeEvent
    public static void applyDamage(LivingHurtEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;

        LivingEntity target = event.getEntity();
        if (target.getTags().contains("dying")) {
            target.removeTag("dying");
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Projectile) {
            if (((Projectile) attacker).getOwner() == null) {
                attacker = null;
            } else {
                attacker = ((Projectile) attacker).getOwner();
            }
        } else {
            attacker = event.getSource().getEntity();
        }

        boolean triggerIFrames = false;

        double baseDmg = event.getAmount();
        if (baseDmg <= 0.01) return;
        double damage;
        boolean isCrit = false;
        ElementType elementType;
        DamageSource source;

        IFinalStats attackerStats = null;
        IIndependentStats independentAttackerStats = null;

        LazyOptional<IFinalStats> targetStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> independentTargetStatsL = target.getCapability(ModCapabilities.INDEPENDENT_STATS);

        if (!independentTargetStatsL.isPresent() || !targetStatsL.isPresent()) {
            return;
        }


        System.out.print("[DEBUG] Target's stats found. ");

        IFinalStats targetStats = targetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats independentTargetStats = independentTargetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        event.setAmount(0);

        int invulnerableTime = (int) independentTargetStats.getIndependentStat(IndependentStatType.INVULNERABLE_TIME);
        if (invulnerableTime >= 1 || independentTargetStats.getActiveEffectList().containsKey(ModEffects.BLESSING_OF_UNDYING_EFFECT.get())) {
            return;
        }

        if (targetStats.getFinalStat(StatType.DODGE_CHANCE) > 0) {
            double actualDodgeChance = Math.random();
            double dodgeChance = targetStats.getFinalStat(StatType.DODGE_CHANCE);

            if (actualDodgeChance < dodgeChance) {

                if (attacker instanceof LivingEntity livingAttacker) {
                    target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onDodge(target, livingAttacker);
                                    }
                                }
                            }
                        }
                    });

                    attacker.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onAttackMiss(livingAttacker, target);
                                    }
                                }
                            }}
                    });


                } else {
                    target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onDodge(target, null);
                                    }
                                }
                            }}
                    });
                }

                ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                armorStand.addTag("indicator");
                double xr = target.getX() + Math.random() - 0.5;
                double yr = target.getY() + 1 + Math.random();
                double zr = target.getX() + Math.random() - 0.5;
                armorStand.addTag("indicator");
                CompoundTag tag1 = new CompoundTag();
                tag1.putBoolean("Marker", true);
                armorStand.load(tag1);


                armorStand.getPersistentData().putString("adventurer_fantasy:name", "Dodged");
                armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xr);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yr);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zr);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.1);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 40);
                armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x6E8287);
                armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);


                armorStand.addTag("indicatorToInitialise");

                armorStand.setPos(xr, yr, zr);
                armorStand.isMarker();
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setNoBasePlate(true);
                armorStand.setShowArms(false);
                level.addFreshEntity(armorStand);

                independentTargetStats.setIndependentStat(IndependentStatType.INVULNERABLE_TIME, targetStats.getFinalStat(StatType.INVULNERABLE_DURATION));

                return;
            }
        }

        if (!(attacker instanceof LivingEntity)) {

            source = event.getSource();
            Holder<DamageType> type = source.typeHolder();

            triggerIFrames = true;
            double elementalRes;
            RegistryAccess registryAccess = level.registryAccess();
            Registry<DamageType> damageTypeRegistry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);

            if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.WITHER) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.WITHER_SKULL)) {
                elementType = ElementType.DECAY;
                elementalRes = targetStats.getFinalStat(StatType.DECAY_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.SONIC_BOOM)) {
                elementType = ElementType.ECHO;
                elementalRes = targetStats.getFinalStat(StatType.ECHO_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.STALAGMITE) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALLING_STALACTITE) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALLING_BLOCK) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALLING_ANVIL)) {
                elementType = ElementType.EARTH;
                elementalRes = targetStats.getFinalStat(StatType.EARTH_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.ON_FIRE) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.IN_FIRE) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.DRY_OUT) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.EXPLOSION) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FIREWORKS) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FIREBALL)) {
                elementType = ElementType.FIRE;
                elementalRes = targetStats.getFinalStat(StatType.FIRE_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FREEZE)) {
                elementType = ElementType.ICE;
                elementalRes = targetStats.getFinalStat(StatType.ICE_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.MAGIC) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.INDIRECT_MAGIC) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.DRAGON_BREATH)) {
                elementType = ElementType.IMAGINARY;
                elementalRes = targetStats.getFinalStat(StatType.IMAGINARY_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.LAVA) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.HOT_FLOOR)) {
                elementType = ElementType.LAVA;
                elementalRes = targetStats.getFinalStat(StatType.LAVA_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.LIGHTNING_BOLT)) {
                elementType = ElementType.LIGHTNING;
                elementalRes = targetStats.getFinalStat(StatType.LIGHTNING_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.GENERIC) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.BAD_RESPAWN_POINT) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.THORNS)) {
                elementType = ElementType.PHYSICAL;
                elementalRes = targetStats.getFinalStat(StatType.PHYSICAL_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.CACTUS) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.SWEET_BERRY_BUSH)) {
                elementType = ElementType.NATURE;
                elementalRes = targetStats.getFinalStat(StatType.PHYSICAL_DMG_RES);
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.STARVE) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.CRAMMING)) {
                double hp = targetStats.getFinalStat(StatType.MAX_HEALTH);
                triggerIFrames = false;
                baseDmg = hp/20;
                if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD) ||type == damageTypeRegistry.getHolderOrThrow(DamageTypes.CRAMMING)) {
                    baseDmg = baseDmg * 4;
                }
                elementType = ElementType.TRUE;
                elementalRes = 0;
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.DROWN)) {
                elementType = ElementType.TRUE;
                elementalRes = 0;
                double hp = targetStats.getFinalStat(StatType.MAX_HEALTH);
                baseDmg = hp/10;
                baseDmg = baseDmg * (1 - targetStats.getFinalStat(StatType.DROWNING_DMG_RES));
                triggerIFrames = false;
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALL) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FLY_INTO_WALL)) {
                elementType = ElementType.WIND;
                elementalRes = targetStats.getFinalStat(StatType.WIND_DMG_RES);
                baseDmg = baseDmg * (1 - targetStats.getFinalStat(StatType.FALL_DMG_RES));
                triggerIFrames = false;
            } else {
                elementType = ElementType.PHYSICAL;
                elementalRes = targetStats.getFinalStat(StatType.PHYSICAL_DMG_RES);
            }
            double allRes = targetStats.getFinalStat(StatType.ALL_DMG_RES);
            double armor = targetStats.getFinalStat(StatType.ARMOR);
            if (elementType == ElementType.TRUE) {
                damage = baseDmg;
            } else {
                damage = (baseDmg * (1 - allRes) * (1 - elementalRes) - armor / 4);
            }

            damage = max(damage, 0.0);
        } else {

            source = null;
            LazyOptional<IFinalStats> attackerStatsL = attacker.getCapability(ModCapabilities.FINAL_STATS);
            LazyOptional<IIndependentStats> independentAttackerStatsL = attacker.getCapability(ModCapabilities.INDEPENDENT_STATS);

            if (!attackerStatsL.isPresent() || !independentAttackerStatsL.isPresent()) {
                System.out.print("Couldn't find attacker's stats");
            }

            attackerStats = attackerStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            independentAttackerStats = independentAttackerStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            elementType = independentAttackerStats.getElementType();
            DmgInstanceType dmgType = independentAttackerStats.getDmgInstanceType();

            UUID attackerUUID = attacker.getUUID();
            Map<UUID, AggroData> aggroMap = independentTargetStats.getAggroMap();
            AggroData aggroData = aggroMap.get(attackerUUID);
            if (aggroData == null) {
                aggroData = new AggroData(0.02, 1);
                independentTargetStats.getAggroMap().put(attackerUUID, aggroData);
            } else {
                aggroData.setAttackedCount(aggroData.getAttackedCount() + 1);
            }
            if (elementType == ElementType.TRUE) {
                damage = baseDmg;
            } else {
                double allDmgAmp = attackerStats.getFinalStat(StatType.ALL_DMG_AMP);
                double armorPen = attackerStats.getFinalStat(StatType.ARMOR_PEN);
                double allTypeResIgnore = attackerStats.getFinalStat(StatType.ALL_TYPE_RES_IGNORE);

                double allRes = targetStats.getFinalStat(StatType.ALL_DMG_RES);
                double armor = targetStats.getFinalStat(StatType.ARMOR);

                double elementAmp;
                double elementRes;

                double instanceAmp;
                double instanceRes;

                if (elementType == ElementType.BLIGHT) {
                    elementRes = targetStats.getFinalStat(StatType.BLIGHT_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.BLIGHT_DMG_AMP);
                } else if (elementType == ElementType.DECAY) {
                    elementRes = targetStats.getFinalStat(StatType.DECAY_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.DECAY_DMG_AMP);
                } else if (elementType == ElementType.EARTH) {
                    elementRes = targetStats.getFinalStat(StatType.EARTH_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.EARTH_DMG_AMP);
                } else if (elementType == ElementType.ECHO) {
                    elementRes = targetStats.getFinalStat(StatType.ECHO_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.ECHO_DMG_AMP);
                } else if (elementType == ElementType.FIRE) {
                    elementRes = targetStats.getFinalStat(StatType.FIRE_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.FIRE_DMG_AMP);
                } else if (elementType == ElementType.ICE) {
                    elementRes = targetStats.getFinalStat(StatType.ICE_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.ICE_DMG_AMP);
                } else if (elementType == ElementType.IMAGINARY) {
                    elementRes = targetStats.getFinalStat(StatType.IMAGINARY_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.IMAGINARY_DMG_AMP);
                } else if (elementType == ElementType.LAVA) {
                    elementRes = targetStats.getFinalStat(StatType.LAVA_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.LAVA_DMG_AMP);
                } else if (elementType == ElementType.LIGHTNING) {
                    elementRes = targetStats.getFinalStat(StatType.LIGHTNING_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.LIGHTNING_DMG_AMP);
                } else if (elementType == ElementType.PHYSICAL) {
                    elementRes = targetStats.getFinalStat(StatType.PHYSICAL_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.PHYSICAL_DMG_AMP);
                } else if (elementType == ElementType.NATURE) {
                    elementRes = targetStats.getFinalStat(StatType.NATURE_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.NATURE_DMG_AMP);
                } else if (elementType == ElementType.VOID) {
                    elementRes = targetStats.getFinalStat(StatType.VOID_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.VOID_DMG_AMP);
                } else if (elementType == ElementType.WATER) {
                    elementRes = targetStats.getFinalStat(StatType.WATER_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.WATER_DMG_AMP);
                } else if (elementType == ElementType.WIND) {
                    elementRes = targetStats.getFinalStat(StatType.WIND_DMG_RES);
                    elementAmp = attackerStats.getFinalStat(StatType.WIND_DMG_AMP);
                } else {
                    elementRes = 0;
                    elementAmp = 1;
                }

                if (dmgType == DmgInstanceType.DELAYED) {
                    instanceRes = targetStats.getFinalStat(StatType.DELAYED_DMG_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.DELAYED_DMG_AMP);
                } else if (dmgType == DmgInstanceType.DIRECT) {
                    instanceRes = targetStats.getFinalStat(StatType.DIRECT_DMG_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.DIRECT_DMG_AMP);
                } else if (dmgType == DmgInstanceType.DOT) {
                    instanceRes = targetStats.getFinalStat(StatType.DOT_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.DOT_AMP);
                } else if (dmgType == DmgInstanceType.FOLLOW_UP) {
                    instanceRes = targetStats.getFinalStat(StatType.FOLLOW_UP_DMG_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.FOLLOW_UP_DMG_AMP);
                } else if (dmgType == DmgInstanceType.REACTION) {
                    instanceRes = targetStats.getFinalStat(StatType.REACTION_DMG_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.REACTION_DMG_AMP);
                } else if (dmgType == DmgInstanceType.RETALIATION) {
                    instanceRes = targetStats.getFinalStat(StatType.RETALIATION_DMG_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.RETALIATION_DMG_AMP);
                } else if (dmgType == DmgInstanceType.SUMMON) {
                    instanceRes = targetStats.getFinalStat(StatType.SUMMON_DMG_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.SUMMON_DMG_AMP);
                } else if (dmgType == DmgInstanceType.ULTIMATE) {
                    instanceRes = targetStats.getFinalStat(StatType.ULTIMATE_DMG_RES);
                    instanceAmp = attackerStats.getFinalStat(StatType.ULTIMATE_DMG_AMP);
                } else {
                    instanceRes = 0;
                    instanceAmp = 1;
                }


                double damageMult = (baseDmg * allDmgAmp * elementAmp * instanceAmp * (1 - allRes + allTypeResIgnore) * (1 - elementRes) * (1 - instanceRes));


                double actualCrit = Math.random();



                double critRate = attackerStats.getFinalStat(StatType.CRIT_RATE);
                if (actualCrit < critRate) {
                    double critDmg = attackerStats.getFinalStat(StatType.CRIT_DMG);
                    damageMult *= critDmg;
                    isCrit = true;

                    if (attacker instanceof LivingEntity finalAttacker2) {
                        target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                            if (!stats.getActiveEffectList().isEmpty()) {
                                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                    if (effectMap.containsKey(copiedEffect)) {
                                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                            ((ICustomStatusEffect) copiedEffect).onReceivingCrit(target, finalAttacker2);
                                        }
                                    }
                                }
                            }
                        });

                        attacker.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                            if (!stats.getActiveEffectList().isEmpty()) {
                                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                    if (effectMap.containsKey(copiedEffect)) {
                                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                            ((ICustomStatusEffect) copiedEffect).onCrit(finalAttacker2, target);
                                        }
                                    }
                                }
                            }
                        });

                    } else {
                        target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                            if (!stats.getActiveEffectList().isEmpty()) {
                                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                    if (effectMap.containsKey(copiedEffect)) {
                                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                            ((ICustomStatusEffect) copiedEffect).onReceivingCrit(target, null);
                                        }
                                    }
                                }}
                        });
                    }
                }


                damage = damageMult - (armor * (1 - armorPen)) / 4;
                damage = max(damage, 0.0);
                triggerIFrames = true;
                double lifestealPercentage = attackerStats.getFinalStat(StatType.LIFESTEAL);
                double attackerMaxHp = attackerStats.getFinalStat(StatType.MAX_HEALTH);
                if (lifestealPercentage > 0 && damage > 0 && attacker instanceof LivingEntity entity) {
                    double lifesteal = 0.2 * attackerMaxHp * (1 - Math.pow(Math.E, - lifestealPercentage * damage));
                    MinecraftForge.EVENT_BUS.post(new HealingEvent(entity, entity, lifesteal));

                }
            }
            if (damage > 0) {
                double knockbackStrength = attackerStats.getFinalStat(StatType.KNOCKBACK_STRENGTH_AMP);
                double knockbackRes = targetStats.getFinalStat(StatType.KNOCKBACK_RES);
                Vec3 direction = target.position().subtract(attacker.position()).normalize().scale(max(0, knockbackStrength * (1 - knockbackRes)));
                target.setDeltaMovement(target.getDeltaMovement().add(direction));
                target.hurtMarked = true;
            }

            List<StatusEffectEntry> EffectList = independentAttackerStats.getEffectList();
            for (StatusEffectEntry entry : EffectList) {
                MinecraftForge.EVENT_BUS.post(new EffectApplicationEvent((LivingEntity) attacker, target, entry.effect(), entry.duration(), entry.baseChance(), entry.amplifier(), entry.stacks(), entry.maxStacks(), false, new CompoundTag()));
            }
        }


        double finalDamage = damage;
        Entity finalAttacker = attacker;
        target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
            if (!stats.getActiveEffectList().isEmpty()) {
                Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                    if (effectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                            if (finalAttacker instanceof LivingEntity livingAttacker) {
                                ((ICustomStatusEffect) copiedEffect).onBeingHurt(target, livingAttacker, finalDamage);
                            } else {
                                ((ICustomStatusEffect) copiedEffect).onBeingHurt(target, null, finalDamage);
                            }
                        }}
                }}
        });

        if (attacker instanceof LivingEntity livingAttacker) {
            attacker.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                if (!stats.getActiveEffectList().isEmpty()) {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                        if (effectMap.containsKey(copiedEffect)) {
                            StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                            if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                ((ICustomStatusEffect) copiedEffect).onHurt(livingAttacker, target, finalDamage);
                            }}
                    }}
            });
        }

        double currentHP = independentTargetStats.getIndependentStat(IndependentStatType.HEALTH);
        double maxHealth = targetStats.getFinalStat(StatType.MAX_HEALTH);
        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();
        double shield = independentTargetStats.getIndependentStat(IndependentStatType.SHIELD);

        if (triggerIFrames) {
            int newInvulnerable = (int) targetStats.getFinalStat(StatType.INVULNERABLE_DURATION);
            independentTargetStats.setIndependentStat(IndependentStatType.INVULNERABLE_TIME, newInvulnerable);
            System.out.println("[DEBUG] Triggered iFrames for " + target.getName().getString());
        }


        if (elementType == ElementType.FIRE && independentTargetStats.getActiveEffectList().containsKey(ModEffects.WET_EFFECT.get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            if (!(attacker instanceof LivingEntity livingAttacker)) {
                damage = damage * 1.15;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);

                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 6 + 6), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.45);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, null, triggerer2, ElementalReaction.VAPORISE);
                                }
                            }
                        }}
                });
            } else {
                damage = damage * (1.15 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));

                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 6 + 6), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.45);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, livingAttacker, triggerer2, ElementalReaction.VAPORISE);
                                }
                            }
                        }}
                });
            }

            if (level instanceof ServerLevel serverLevel) {
                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.WET_EFFECT.get()).applier();

                LivingEntity triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);

                if (triggerer2 instanceof LivingEntity) {
                    Entity finalAttacker3 = attacker;
                    triggerer2.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, triggerer2, (LivingEntity) finalAttacker3, ElementalReaction.MELT);
                                    }
                                }
                            }}
                    });
                }
            }

            independentTargetStats.removeActiveEffect(ModEffects.WET_EFFECT.get(), target);


            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
            ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
            armorStand.addTag("indicator");

            double xr = x + Math.random() - 0.5;
            double yr = y + 1 + Math.random();
            double zr = z + Math.random() - 0.5;
            armorStand.addTag("indicator");
            CompoundTag tag1 = new CompoundTag();
            tag1.putBoolean("Marker", true);
            armorStand.load(tag1);
            armorStand.absMoveTo(x, y, z);

            armorStand.getPersistentData().putString("adventurer_fantasy:name", "Vaporise");
            armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.1);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
            armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 40);
            armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x8f172f);
            armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);

            armorStand.addTag("indicatorToInitialise");

            armorStand.setPos(x, y, z);
            armorStand.isMarker();
            armorStand.setInvisible(true);
            armorStand.setNoGravity(true);
            armorStand.setNoBasePlate(true);
            armorStand.setShowArms(false);
            level.addFreshEntity(armorStand);
        } else if (elementType == ElementType.WATER && independentTargetStats.getActiveEffectList().containsKey(ModEffects.BLAZING_EFFECT.get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            if (!(attacker instanceof LivingEntity livingAttacker)) {
                damage = damage * 1.2;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);

                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 6 + 6), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.45);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, null, triggerer2, ElementalReaction.VAPORISE);
                                }
                            }
                        }}
                });
            } else {
                damage = damage * (1.2 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));

                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 6 + 6), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.45);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, livingAttacker, triggerer2, ElementalReaction.VAPORISE);
                                }
                            }
                        }}
                });
            }

            if (level instanceof ServerLevel serverLevel) {
                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.BLAZING_EFFECT.get()).applier();

                LivingEntity triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);

                if (triggerer2 instanceof LivingEntity) {
                    Entity finalAttacker3 = attacker;
                    triggerer2.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, triggerer2, (LivingEntity) finalAttacker3, ElementalReaction.MELT);
                                    }
                                }
                            }}
                    });
                }
            }

            independentTargetStats.removeActiveEffect(ModEffects.BLAZING_EFFECT.get(), target);


            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);

            ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
            armorStand.addTag("indicator");
            double xr = x + Math.random() - 0.5;
            double yr = y + 1 + Math.random();
            double zr = z + Math.random() - 0.5;
            armorStand.addTag("indicator");
            CompoundTag tag1 = new CompoundTag();
            tag1.putBoolean("Marker", true);
            armorStand.load(tag1);
            armorStand.absMoveTo(x, y, z);

            armorStand.getPersistentData().putString("adventurer_fantasy:name", "Vaporise");
            armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.1);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
            armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 40);
            armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x601546);
            armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);

            armorStand.addTag("indicatorToInitialise");

            armorStand.setPos(x, y, z);
            armorStand.isMarker();
            armorStand.setInvisible(true);
            armorStand.setNoGravity(true);
            armorStand.setNoBasePlate(true);
            armorStand.setShowArms(false);
            level.addFreshEntity(armorStand);


        } else if (elementType == ElementType.FIRE && independentTargetStats.getActiveEffectList().containsKey(ModEffects.FROSTED_EFFECT.get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            if (!(attacker instanceof LivingEntity livingAttacker)) {
                damage = damage * 1.2;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);
                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 8 + 8), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.2 + 0.1);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, null, triggerer2, ElementalReaction.MELT);
                                }
                            }
                        }}
                });
            } else {
                damage = damage * (1.2 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));

                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 8 + 8), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.2 + 0.1);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, livingAttacker, triggerer2, ElementalReaction.MELT);
                                }
                            }
                        }}
                });
            }

            if (level instanceof ServerLevel serverLevel) {
                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);

                if (triggerer2 instanceof LivingEntity) {
                    Entity finalAttacker3 = attacker;
                    triggerer2.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, triggerer2, (LivingEntity) finalAttacker3, ElementalReaction.MELT);
                                    }
                                }
                            }}
                    });
                }
            }

            independentTargetStats.removeActiveEffect(ModEffects.FROSTED_EFFECT.get(), target);

            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.3F, 0.5F);
            ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
            armorStand.addTag("indicator");
            double xr = x + Math.random() - 0.5;
            double yr = y + 1 + Math.random();
            double zr = z + Math.random() - 0.5;
            armorStand.addTag("indicator");
            CompoundTag tag1 = new CompoundTag();
            tag1.putBoolean("Marker", true);
            armorStand.load(tag1);
            armorStand.absMoveTo(x, y, z);


            armorStand.getPersistentData().putString("adventurer_fantasy:name", "Melt");
            armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.1);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
            armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 40);
            armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x966b5b);
            armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);

            armorStand.addTag("indicatorToInitialise");

            armorStand.setPos(x, y, z);
            armorStand.isMarker();
            armorStand.setInvisible(true);
            armorStand.setNoGravity(true);
            armorStand.setNoBasePlate(true);
            armorStand.setShowArms(false);
            level.addFreshEntity(armorStand);

        } else if (elementType == ElementType.ICE && independentTargetStats.getActiveEffectList().containsKey(ModEffects.BLAZING_EFFECT.get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            if (!(attacker instanceof LivingEntity livingAttacker)) {
                damage = damage * 1.15;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);

                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 8 + 8), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.2 + 0.1);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, null, triggerer2, ElementalReaction.MELT);
                                }
                            }
                        }}
                });
            } else {
                damage = damage * (1.15 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));

                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.FROSTED_EFFECT.get()).applier();

                LivingEntity triggerer2;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 8 + 8), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.2 + 0.1);

                    triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);
                } else {
                    triggerer2 = null;
                }


                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, livingAttacker, triggerer2, ElementalReaction.MELT);
                                }
                            }
                        }}
                });
            }

            if (level instanceof ServerLevel serverLevel) {
                UUID uuid = independentTargetStats.getActiveEffectData(ModEffects.BLAZING_EFFECT.get()).applier();

                LivingEntity triggerer2 = (LivingEntity) serverLevel.getEntity(uuid);

                if (triggerer2 instanceof LivingEntity) {
                    Entity finalAttacker3 = attacker;
                    triggerer2.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onReactionTriggered(target, triggerer2, (LivingEntity) finalAttacker3, ElementalReaction.MELT);
                                    }
                                }
                            }}
                    });
                }
            }

            independentTargetStats.removeActiveEffect(ModEffects.BLAZING_EFFECT.get(), target);


            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.3F, 0.5F);

            ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
            armorStand.addTag("indicator");
            double xr = x + Math.random() - 0.5;
            double yr = y + 1 + Math.random();
            double zr = z + Math.random() - 0.5;
            armorStand.addTag("indicator");
            CompoundTag tag1 = new CompoundTag();
            tag1.putBoolean("Marker", true);
            armorStand.load(tag1);


            armorStand.getPersistentData().putString("adventurer_fantasy:name", "Melt");
            armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zr);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.1);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
            armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 40);
            armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x699389);
            armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);


            armorStand.addTag("indicatorToInitialise");

            armorStand.setPos(x, y, z);
            armorStand.isMarker();
            armorStand.setInvisible(true);
            armorStand.setNoGravity(true);
            armorStand.setNoBasePlate(true);
            armorStand.setShowArms(false);
            level.addFreshEntity(armorStand);

        } else if ((elementType.getEffect() != null) && (elementType.getEffect().getKey() != null)) {

            MinecraftForge.EVENT_BUS.post(new EffectApplicationEvent((LivingEntity) attacker, target, elementType.getEffect().get(), 120, 1, 1, 0, 0, true, new CompoundTag()));
        }


        ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
        armorStand.addTag("indicator");
        x = x + Math.random() - 0.5;
        y = y + 1 + Math.random();
        z = z + Math.random() - 0.5;
        String text = "";
        int color = 0xFFFFFF;
        int lifetime = 0;
        boolean isBold = false;
        double lostShield = 0;
        if (damage == 0) {

            color = 0x6E8287;
            text = "Immune";
            lifetime = 35;
            armorStand.addTag("indicator");


        } else if (shield <= damage) {
            lostShield = shield;
            damage = damage - shield;
            shield = 0;
            currentHP = max(0, currentHP - damage);
            if (!(lostShield == 0)) {

                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onShieldBreak(target);
                                }
                            }
                        }}
                });

                if (!isCrit) {
                    color = 0xdecfa0;
                    lostShield = (int) (lostShield * 100);
                    text = "-" + lostShield / 100;
                    lifetime = 35;
                } else {
                    color = 0xecfa0;
                    isBold = true;
                    lostShield = (int) (lostShield * 100);
                    text = "-" + lostShield / 100 + "!";
                    lifetime = 50;
                }
                armorStand.addTag("indicator");
                CompoundTag tag1 = new CompoundTag();
                tag1.putBoolean("Marker", true);
                armorStand.load(tag1);


            } else if (elementType == ElementType.TRUE) {
                color = 0xc7a4de;
                damage = (int) (damage * 100);
                text = "TRUE -" + damage / 100;
                lifetime = 35;

            } else {

                color = elementType.getColor();
                if (!isCrit) {
                    damage = (int) (damage * 100);
                    text = "-" + damage / 100;
                    lifetime = 35;
                } else {
                    isBold = true;
                    damage = (int) (damage * 100);
                    text = "-" + damage / 100 + "!";
                    lifetime = 50;
                }


            }
        } else {
            shield = max(0, shield - damage);
            if (!isCrit) {
                color = 0xdecfa0;
                lifetime = 35;
                damage = (int) (damage * 100);
                text = "-" + damage / 100;
            } else {
                color = 0xecfa0;
                isBold = true;
                lifetime = 50;
                text = "-" + damage / 100 + "!";
            }
        }

        armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
        armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", lifetime);
        armorStand.getPersistentData().putInt("adventurer_fantasy:color", color);
        armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", isBold);
        armorStand.absMoveTo(x, y, z);
        armorStand.addTag("indicatorToInitialise");


        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Marker", true);
        armorStand.load(tag);

        armorStand.setPos(x, y, z);
        armorStand.isMarker();
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setNoBasePlate(true);
        armorStand.setShowArms(false);

        level.addFreshEntity(armorStand);

        if (lostShield != 0) {
            double x1 = x + Math.random();
            double y1 = y + Math.random();
            double z1 = z + Math.random();
            color = elementType.getColor();
            if (!isCrit) {
                double ddamage = Math.abs((int) (damage - lostShield));
                text = "-" + ddamage / 100;
                lifetime = 35;
            } else {
                isBold = true;
                double ddamage = Math.abs((int) (damage - lostShield));
                text = "-" + ddamage / 100 + "!";
                lifetime = 50;
            }

            ArmorStand armorStand1 = new ArmorStand(EntityType.ARMOR_STAND, level);
            armorStand1.addTag("indicator");

            armorStand1.getPersistentData().putString("adventurer_fantasy:name", text);
            armorStand1.getPersistentData().putDouble("adventurer_fantasy:x", x1);
            armorStand1.getPersistentData().putDouble("adventurer_fantasy:y", y1);
            armorStand1.getPersistentData().putDouble("adventurer_fantasy:z", z1);
            armorStand1.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
            armorStand1.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
            armorStand1.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
            armorStand1.getPersistentData().putInt("adventurer_fantasy:lifetime", lifetime);
            armorStand1.getPersistentData().putInt("adventurer_fantasy:color", color);
            armorStand1.getPersistentData().putBoolean("adventurer_fantasy:isBold", isBold);
            armorStand1.addTag("indicatorToInitialise");


            CompoundTag tag1 = new CompoundTag();
            tag1.putBoolean("Marker", true);
            armorStand1.load(tag1);

            armorStand1.setPos(x1, y1, z1);
            armorStand1.isMarker();
            armorStand1.setInvisible(true);
            armorStand1.setNoGravity(true);
            armorStand1.setNoBasePlate(true);
            armorStand1.setShowArms(false);

            level.addFreshEntity(armorStand1);
        }


        if (currentHP > maxHealth) {
            currentHP = maxHealth;
            independentTargetStats.setIndependentStat(IndependentStatType.HEALTH, maxHealth);
        }

        if (target instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                player.getCapability(ModCapabilities.BASE_STATS).ifPresent(statsB -> {
                    player.getCapability(ModCapabilities.ADD_STATS).ifPresent(statsA -> {
                        player.getCapability(ModCapabilities.MULT_STATS).ifPresent(statsM -> {
                            ModNetworking.sendToPlayer(new ClientStatReceiver(player.getFoodData().getSaturationLevel(), independentTargetStats.serializeNBT(), targetStats.serializeNBT(), statsP.serializeNBT(), statsB.serializeNBT(), statsA.serializeNBT(), statsM.serializeNBT()), player);
                        });
                    });
                });
            });
        }

        if (attacker instanceof LivingEntity livingAttacker) {
            MinecraftForge.EVENT_BUS.post(new HurtCustomEvent(livingAttacker, target, damage));
        } else {
            MinecraftForge.EVENT_BUS.post(new HurtCustomEvent(null, target, damage));
        }

        if (currentHP <= 0) {
            if (independentTargetStats.getIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE) >= 1) {

                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect) {
                                    ((ICustomStatusEffect) copiedEffect).onDeathDefiance(target);
                                }
                            }
                        }}
                });
                if (target.getItemBySlot(EquipmentSlot.OFFHAND).getItem() == Items.TOTEM_OF_UNDYING && target.getItemBySlot(EquipmentSlot.MAINHAND).getItem() == Items.TOTEM_OF_UNDYING) {
                    independentTargetStats.setIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE, independentTargetStats.getIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE) + 1);
                }
                independentTargetStats.setIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE, independentTargetStats.getIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE) - 1);

                independentTargetStats.setIndependentStat(IndependentStatType.INVULNERABLE_TIME, targetStats.getFinalStat(StatType.INVULNERABLE_DURATION) * 5);

                independentTargetStats.setIndependentStat(IndependentStatType.HEALTH, Math.max(0.01, targetStats.getFinalStat(StatType.MAX_HEALTH) * targetStats.getFinalStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE)));
                RegistryAccess registryAccess1 = level.registryAccess();
                Registry<DamageType> damageTypeRegistry1 = registryAccess1.registryOrThrow(Registries.DAMAGE_TYPE);
                Holder<DamageType> damageHolder1 = damageTypeRegistry1.getHolderOrThrow(DamageTypes.GENERIC);
                DamageSource damageSourceFinal1 = new DamageSource(damageHolder1, (Vec3) null);
                target.hurt(damageSourceFinal1, 0.01F);

                level.playSound(null, x, y +1, z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.3F, 0.5F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, target.getX(), target.getY() + 1, target.getZ(), (int) (Math.random() * 8 + 8), 0, 0, 0, 0.2);
                }

            } else if (attacker instanceof LivingEntity finalAttacker1) {
                MinecraftForge.EVENT_BUS.post(new DeathHandlerEvent(target, null, finalAttacker1));

                attacker.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onKill(finalAttacker1, target);
                                }
                            }
                        }}
                });

            } else {
                MinecraftForge.EVENT_BUS.post(new DeathHandlerEvent(target, source, null));
            }
        } else {

            if (target.getControllingPassenger() instanceof Player player) {
                double finalCurrentHP1 = currentHP;
                player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                    statsP.setMountMaxHealth(maxHealth);
                    statsP.setMountHealth(finalCurrentHP1);
                    player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> statsI.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1));
                });
            }
            independentTargetStats.setIndependentStat(IndependentStatType.HEALTH, currentHP);
            independentTargetStats.setIndependentStat(IndependentStatType.SHIELD, shield);
            RegistryAccess registryAccess1 = level.registryAccess();
            Registry<DamageType> damageTypeRegistry1 = registryAccess1.registryOrThrow(Registries.DAMAGE_TYPE);
            Holder<DamageType> damageHolder1 = damageTypeRegistry1.getHolderOrThrow(DamageTypes.GENERIC);
            DamageSource damageSourceFinal1 = new DamageSource(damageHolder1, (Vec3) null);
            target.hurt(damageSourceFinal1, 0.01F);

            if (target.getControllingPassenger() instanceof Player player) {
                double finalCurrentHP = currentHP;
                player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                    statsP.setMountHealth(finalCurrentHP);
                    player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> statsI.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1));
                });
            }
        }


    }

    @SubscribeEvent
    public static void indicatorInitialisation(LivingEvent.LivingTickEvent event) {
        LivingEntity indicator = event.getEntity();
        if (indicator == null) return;
        if (!(indicator instanceof ArmorStand)) return;
        Level level = indicator.level();
        if (level.isClientSide() || !(indicator.getTags().contains("indicatorToInitialise"))) return;
        LazyOptional<IIndicatorStats> statsL = indicator.getCapability(ModCapabilities.INDICATOR_STATS);
        if (!statsL.isPresent()) return;
        IIndicatorStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        CompoundTag data = indicator.getPersistentData();

        if (data.contains("adventurer_fantasy:x") && data.contains("adventurer_fantasy:y") && data.contains("adventurer_fantasy:x")) {
            double x = data.getDouble("adventurer_fantasy:x");
            double y = data.getDouble("adventurer_fantasy:y");
            double z = data.getDouble("adventurer_fantasy:z");
            indicator.teleportTo(x, y, z);
            data.remove("adventurer_fantasy:x");
            data.remove("adventurer_fantasy:y");
            data.remove("adventurer_fantasy:z");
        }

        if (data.contains("adventurer_fantasy:color") && data.contains("adventurer_fantasy:name") && data.contains("adventurer_fantasy:isBold")) {
            int color = data.getInt("adventurer_fantasy:color");
            String name = data.getString("adventurer_fantasy:name");
            boolean isBold = data.getBoolean("adventurer_fantasy:isBold");
            Style style = Style.EMPTY.withColor(color).withBold(isBold);
            indicator.setCustomName(Component.literal(name).withStyle(style));
            data.remove("adventurer_fantasy:color");
            data.remove("adventurer_fantasy:name");
            data.remove("adventurer_fantasy:isBold");

            indicator.setCustomNameVisible(true);
        }

        if (data.contains("adventurer_fantasy:lifetime")) {
            int lifetime = data.getInt("adventurer_fantasy:lifetime");
            stats.setLifetime(lifetime);
            data.remove("adventurer_fantasy:lifetime");
        }

        ((ArmorStand) indicator).isMarker();
        indicator.setInvisible(true);
        indicator.setNoGravity(true);
        ((ArmorStand) indicator).setNoBasePlate(true);
        ((ArmorStand) indicator).setShowArms(false);

        indicator.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));


        indicator.setInvulnerable(true);

        indicator.getTags().remove("indicatorToInitialise");

    }

    @SubscribeEvent
    public static void HPConsumption(HealthConsumptionEvent event) {
        if (event.getTarget() == null) return;
        Level level = event.getTarget().level();
        if (level.isClientSide) return;
        LivingEntity target = event.getTarget();
        if (target == null) return;
        LazyOptional<IFinalStats> finalStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> statsL = target.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!finalStatsL.isPresent() || !statsL.isPresent()) return;

        IFinalStats finalStats = finalStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        double selfDmgAmp = finalStats.getFinalStat(StatType.SELF_DMG_AMP);
        double selfDmg = event.getAmount() * selfDmgAmp;
        double hp = stats.getIndependentStat(IndependentStatType.HEALTH);
        double newHp = max(0.01, hp - selfDmg);

            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
            if (!stats.getActiveEffectList().isEmpty()) {
                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                    if (effectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                            ((ICustomStatusEffect) copiedEffect).onHealthConsumption(target, hp - newHp);
                        }
                    }
                }}

        stats.setIndependentStat(IndependentStatType.HEALTH, newHp);
    }

    @SubscribeEvent
    public static void customDeath(DeathHandlerEvent event) {
        if (event.getTarget() == null) return;
        Level level = event.getTarget().level();
        if (level.isClientSide) return;
        DamageSource source = event.getCause();
        LivingEntity killer = event.getKiller();
        LivingEntity target = event.getTarget();

        if (target == null) return;
        target.addTag("dying");
        System.out.println("[DEBUG] Killed " + target.getName().getString());

        LazyOptional<IFinalStats> statsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> independentStatsL = target.getCapability(ModCapabilities.INDEPENDENT_STATS);

        if (!statsL.isPresent() || !independentStatsL.isPresent()) return;

        IFinalStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        boolean showVanillaDeathMessages =  target.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
        if (showVanillaDeathMessages) {
            target.level().getGameRules().getRule(GameRules.RULE_SHOWDEATHMESSAGES).set(false, target.getServer());
        }

        double maxHealth = stats.getFinalStat(StatType.MAX_HEALTH);
        independentStats.setIndependentStat(IndependentStatType.HEALTH, maxHealth);
        independentStats.setIndependentStat(IndependentStatType.SHIELD, 0);
        independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, 0);
        target.invulnerableTime = 0;

        if (target.getControllingPassenger() instanceof Player player) {
            player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                statsP.setMountHealth(0);
                statsP.setMountHealth(0);
                statsP.setMount(null);
                player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> statsI.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1));
            });
        }


        Map<MobEffect, StatusEffectInstanceEntry> effectMap = independentStats.getActiveEffectList();
        if (!independentStats.getActiveEffectList().isEmpty()) {
            for (MobEffect copiedEffect : (new HashMap<>(independentStats.getActiveEffectList())).keySet()) {
                if (copiedEffect instanceof ICustomStatusEffect) {
                    if (effectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        if (effectInstance.isInitialised()) {
                            ((ICustomStatusEffect) copiedEffect).onDeath(target);
                            independentStats.removeActiveEffect(copiedEffect, target);
                        }
                    }
                }
            }
        }

        boolean  showCustomDeathMessages = target.level().getGameRules().getBoolean(ModGameRules.SHOW_CUSTOM_DEATH_MESSAGES);
        if (killer == null) {
            if (source == null) {
                RegistryAccess registryAccess = level.registryAccess();
                Registry<DamageType> damageTypeRegistry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
                Holder<DamageType> damageHolder = damageTypeRegistry.getHolderOrThrow(DamageTypes.GENERIC);
                DamageSource damageSourceFinal = new DamageSource(damageHolder, (Vec3) null);
                target.hurt(damageSourceFinal, Float.MAX_VALUE);
                if (target instanceof ServerPlayer player && showCustomDeathMessages) {
                    String message = player.getGameProfile().getName() + " died.";
                    if (target.getServer() == null) return;
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                }
            } else {
                Holder<DamageType> type = source.typeHolder();
                RegistryAccess registryAccess = level.registryAccess();
                Registry<DamageType> damageTypeRegistry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
                Holder<DamageType> damageHolder = damageTypeRegistry.getHolderOrThrow(DamageTypes.GENERIC);
                DamageSource damageSourceFinal = new DamageSource(damageHolder, (Vec3) null);
                target.hurt(damageSourceFinal, Float.MAX_VALUE);
                if (!(target instanceof ServerPlayer) || !showCustomDeathMessages) return;
                if (target.getServer() == null) return;
                if (type == DamageTypes.WITHER || type == DamageTypes.WITHER_SKULL) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " withered away.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.SONIC_BOOM)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " was obliterated by a sonically-charged shriek.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALLING_STALACTITE)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + "'s head was pierced by a falling stalactite.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.STALAGMITE)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " was impaled by a stalagmite.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALLING_ANVIL) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALLING_BLOCK)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " was squashed under falling debris.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.LIGHTNING_BOLT)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " was struck by lightning.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.ON_FIRE) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.IN_FIRE)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " was incinerated.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.DRY_OUT)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " dried out.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.EXPLOSION) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FIREWORKS) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FIREBALL)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " was blown up by an explosion.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FREEZE)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " froze to death.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.THORNS)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + "'s attacks bounced back.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.MAGIC) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.INDIRECT_MAGIC) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.DRAGON_BREATH)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + "'s matter was magically spread all throughout the unknown universe.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.LAVA)) {
                    String message = "To " + ((ServerPlayer) target).getGameProfile().getName() + "'s amazement, swimming into lava is actually a bad idea.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.HOT_FLOOR)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " is bad at the floor is lava.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.CACTUS) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.SWEET_BERRY_BUSH)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " was prickled to death.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " fell out of the world.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.STARVE)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " starved to death.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.DROWN)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " drowned.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALL)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " discovered gravity.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FLY_INTO_WALL)) {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " experienced what a sudden release of kinetic energy does.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                } else {
                    String message = ((ServerPlayer) target).getGameProfile().getName() + " died.";
                    target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
                }
            }

        } else {
            RegistryAccess registryAccess = level.registryAccess();
            Registry<DamageType> damageTypeRegistry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
            Holder<DamageType> damageHolder = damageTypeRegistry.getHolderOrThrow(DamageTypes.MOB_ATTACK);
            DamageSource damageSourceFinal = new DamageSource(damageHolder, killer);
            target.hurt(damageSourceFinal, Float.MAX_VALUE);
            if (!(target instanceof ServerPlayer)|| !showCustomDeathMessages) return;
            int max = DeathMessageByEntity.values().length;
            int ordinal = (int) (Math.random() * max);
            DeathMessageByEntity messageOrdinal = DeathMessageByEntity.values()[ordinal];
            String message = messageOrdinal.getMessage();
            message = message.replace("$player", ((ServerPlayer) target).getGameProfile().getName());
            message = message.replace("$killer", killer.getDisplayName().getString());
            if (target.getServer() == null) return;
            target.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        LivingEntity entity = event.getEntity();
        if (entity == null) return;
        EquipmentSlot slot = event.getSlot();
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) return;
        ItemStack oldItem = event.getFrom();
        System.out.println("[DEBUG] Changed Equipment. Previous: " + oldItem.getItem());
        if (oldItem.getItem() instanceof IStatModifierProvider providerOld) {
            for (StatModifier mod : providerOld.getModifiers(oldItem)) {
                double amountOld = mod.amount();
                StatType statOld = mod.stat();
                LazyOptional<IDirtyStats> dirtyOldStatsL = entity.getCapability(ModCapabilities.DIRTY_STATS);
                if (!dirtyOldStatsL.isPresent()) return;
                IDirtyStats dirtyOldStats = dirtyOldStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                dirtyOldStats.setDirtyStat(statOld, true);

                if (mod.isMultiplicative()) {
                    LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
                    if (!multStatsL.isPresent()) return;
                    IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                    multStats.setMultStat(statOld, multStats.getMultStat(statOld) - amountOld);
                } else {
                    LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
                    if (!addStatsL.isPresent()) return;
                    IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                    addStats.setAddStat(statOld, addStats.getAddStat(statOld) - amountOld);
                }
            }
        } else if (oldItem.getItem() == Items.LEATHER_HELMET || oldItem.getItem() == Items.LEATHER_LEGGINGS || oldItem.getItem() == Items.LEATHER_BOOTS) {
            LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
            if (!multStatsL.isPresent()) return;
            IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            multStats.setMultStat(StatType.DASH_COOLDOWN, multStats.getMultStat(StatType.DASH_COOLDOWN) + 0.04);
            multStats.setMultStat(StatType.DASH_LENGTH, multStats.getMultStat(StatType.DASH_LENGTH) - 0.06);
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 1.25);
        } else if (oldItem.getItem() == Items.LEATHER_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
            if (!multStatsL.isPresent()) return;
            IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            multStats.setMultStat(StatType.DASH_COOLDOWN, multStats.getMultStat(StatType.DASH_COOLDOWN) + 0.04);
            multStats.setMultStat(StatType.DASH_LENGTH, multStats.getMultStat(StatType.DASH_LENGTH) - 0.06);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 2);
        } else if (oldItem.getItem() == Items.CHAINMAIL_BOOTS || oldItem.getItem() == Items.CHAINMAIL_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_STRENGTH_AMP, addStats.getAddStat(StatType.KNOCKBACK_STRENGTH_AMP) - 0.05);
            addStats.setAddStat(StatType.ARMOR_PEN, addStats.getAddStat(StatType.ARMOR_PEN) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 2);
        } else if (oldItem.getItem() == Items.CHAINMAIL_LEGGINGS || oldItem.getItem() == Items.CHAINMAIL_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_STRENGTH_AMP, addStats.getAddStat(StatType.KNOCKBACK_STRENGTH_AMP) - 0.05);
            addStats.setAddStat(StatType.ARMOR_PEN, addStats.getAddStat(StatType.ARMOR_PEN) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 3);
        } else if (oldItem.getItem() == Items.GOLDEN_BOOTS || oldItem.getItem() == Items.GOLDEN_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) - 0.0625);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 2);
        } else if (oldItem.getItem() == Items.GOLDEN_LEGGINGS || oldItem.getItem() == Items.GOLDEN_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) - 0.0625);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 3);
        } else if (oldItem.getItem() == Items.IRON_BOOTS || oldItem.getItem() == Items.IRON_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);

            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.EFFECT_RES, addStats.getAddStat(StatType.EFFECT_RES) - 0.0625);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 2.25);
        } else if (oldItem.getItem() == Items.IRON_LEGGINGS) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.EFFECT_RES, addStats.getAddStat(StatType.EFFECT_RES) - 0.0625);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 5);
        } else if (oldItem.getItem() == Items.IRON_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.EFFECT_RES, addStats.getAddStat(StatType.EFFECT_RES) - 0.0625);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 6);
        } else if (oldItem.getItem() == Items.DIAMOND_BOOTS || oldItem.getItem() == Items.DIAMOND_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) - 0.025);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) - 0.01875);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 3);
        } else if (oldItem.getItem() == Items.DIAMOND_LEGGINGS || oldItem.getItem() == Items.DIAMOND_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) - 0.025);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) - 0.01875);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 7);
        } else if (oldItem.getItem() == Items.NETHERITE_BOOTS || oldItem.getItem() == Items.NETHERITE_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) - 0.05);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) - 0.025);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) - 0.01875);
            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) - 0.05);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) - 0.01);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 3.5);
        } else if (oldItem.getItem() == Items.NETHERITE_CHESTPLATE || oldItem.getItem() == Items.NETHERITE_LEGGINGS) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) - 0.05);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) - 0.025);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) - 0.01875);
            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) - 0.05);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) - 0.01);
            addStats.setAddStat(StatType.MAX_HEALTH, addStats.getAddStat(StatType.MAX_HEALTH) - 2);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 7.5);
        } else if (oldItem.getItem() == Items.TURTLE_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.DROWNING_DMG_RES, addStats.getAddStat(StatType.DROWNING_DMG_RES) - 0.25);
            addStats.setAddStat(StatType.ELEMENTAL_MASTERY, addStats.getAddStat(StatType.ELEMENTAL_MASTERY) - 0.20);
            addStats.setAddStat(StatType.ELEMENTAL_AFFINITY, addStats.getAddStat(StatType.ELEMENTAL_AFFINITY) - 0.10);
            addStats.setAddStat(StatType.ATK_SPEED, addStats.getAddStat(StatType.ATK_SPEED) - 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 2.5);
        } else if (oldItem.getItem() == Items.ELYTRA) {
            LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            if (!multStatsL.isPresent()) return;
            IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.FALL_DMG_RES, addStats.getAddStat(StatType.FALL_DMG_RES) - 0.25);
            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) - 0.25);
            multStats.setMultStat(StatType.DASH_LENGTH, multStats.getMultStat(StatType.DASH_LENGTH) - 0.2);
            addStats.setAddStat(StatType.ADDITIONAL_JUMP, addStats.getAddStat(StatType.ADDITIONAL_JUMP) - 1);
            addStats.setAddStat(StatType.MOVEMENT_SPEED, addStats.getAddStat(StatType.MOVEMENT_SPEED) - 0.05);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) - 0.05);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 5);
        }
        ItemStack newItem = event.getTo();
        if (newItem.getItem() instanceof IStatModifierProvider providerNew) {
            for (StatModifier mod : providerNew.getModifiers(newItem)) {
                double amountNew = mod.amount();
                StatType statNew = mod.stat();
                LazyOptional<IDirtyStats> dirtyNewStatsL = entity.getCapability(ModCapabilities.DIRTY_STATS);
                if (!dirtyNewStatsL.isPresent()) return;
                IDirtyStats dirtyNewStats = dirtyNewStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                dirtyNewStats.setDirtyStat(statNew, true);

                if (mod.isMultiplicative()) {
                    LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
                    if (!multStatsL.isPresent()) return;
                    IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                    multStats.setMultStat(statNew, multStats.getMultStat(statNew) + amountNew);
                } else {
                    LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
                    if (!addStatsL.isPresent()) return;
                    IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                    addStats.setAddStat(statNew, addStats.getAddStat(statNew) + amountNew);
                }
            }
        } else if (newItem.getItem() == Items.LEATHER_HELMET || newItem.getItem() == Items.LEATHER_LEGGINGS || newItem.getItem() == Items.LEATHER_BOOTS) {
            LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
            if (!multStatsL.isPresent()) return;
            IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            multStats.setMultStat(StatType.DASH_COOLDOWN, multStats.getMultStat(StatType.DASH_COOLDOWN) - 0.04);
            multStats.setMultStat(StatType.DASH_LENGTH, multStats.getMultStat(StatType.DASH_LENGTH) + 0.06);
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 1.25);
        } else if (newItem.getItem() == Items.LEATHER_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
            if (!multStatsL.isPresent()) return;
            IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            multStats.setMultStat(StatType.DASH_COOLDOWN, multStats.getMultStat(StatType.DASH_COOLDOWN) - 0.04);
            multStats.setMultStat(StatType.DASH_LENGTH, multStats.getMultStat(StatType.DASH_LENGTH) + 0.06);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 2);
        } else if (newItem.getItem() == Items.CHAINMAIL_BOOTS || newItem.getItem() == Items.CHAINMAIL_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_STRENGTH_AMP, addStats.getAddStat(StatType.KNOCKBACK_STRENGTH_AMP) + 0.05);
            addStats.setAddStat(StatType.ARMOR_PEN, addStats.getAddStat(StatType.ARMOR_PEN) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 2);
        } else if (newItem.getItem() == Items.CHAINMAIL_LEGGINGS || newItem.getItem() == Items.CHAINMAIL_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_STRENGTH_AMP, addStats.getAddStat(StatType.KNOCKBACK_STRENGTH_AMP) + 0.05);
            addStats.setAddStat(StatType.ARMOR_PEN, addStats.getAddStat(StatType.ARMOR_PEN) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 3);
        } else if (newItem.getItem() == Items.GOLDEN_BOOTS || newItem.getItem() == Items.GOLDEN_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) + 0.0625);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 2);
        } else if (newItem.getItem() == Items.GOLDEN_LEGGINGS || newItem.getItem() == Items.GOLDEN_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) + 0.0625);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 3);
        } else if (newItem.getItem() == Items.IRON_BOOTS || newItem.getItem() == Items.IRON_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);

            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.EFFECT_RES, addStats.getAddStat(StatType.EFFECT_RES) + 0.0625);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 2.25);
        } else if (newItem.getItem() == Items.IRON_LEGGINGS) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.EFFECT_RES, addStats.getAddStat(StatType.EFFECT_RES) + 0.0625);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 5);
        } else if (newItem.getItem() == Items.IRON_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.EFFECT_RES, addStats.getAddStat(StatType.EFFECT_RES) + 0.0625);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 6);
        } else if (newItem.getItem() == Items.DIAMOND_BOOTS || newItem.getItem() == Items.DIAMOND_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) + 0.025);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) + 0.01875);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 3);
        } else if (newItem.getItem() == Items.DIAMOND_LEGGINGS || newItem.getItem() == Items.DIAMOND_CHESTPLATE) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) + 0.025);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) + 0.01875);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 7);
        } else if (newItem.getItem() == Items.NETHERITE_BOOTS || newItem.getItem() == Items.NETHERITE_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) + 0.05);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) + 0.025);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) + 0.01875);
            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) + 0.05);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) + 0.01);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 3.5);
        } else if (newItem.getItem() == Items.NETHERITE_CHESTPLATE || newItem.getItem() == Items.NETHERITE_LEGGINGS) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) + 0.05);
            addStats.setAddStat(StatType.ALL_DMG_RES, addStats.getAddStat(StatType.ALL_DMG_RES) + 0.025);
            addStats.setAddStat(StatType.ALL_DMG_AMP, addStats.getAddStat(StatType.ALL_DMG_AMP) + 0.01875);
            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) + 0.05);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) + 0.01);
            addStats.setAddStat(StatType.MAX_HEALTH, addStats.getAddStat(StatType.MAX_HEALTH) + 2);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 7.5);
        } else if (newItem.getItem() == Items.TURTLE_HELMET) {
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.DROWNING_DMG_RES, addStats.getAddStat(StatType.DROWNING_DMG_RES) + 0.25);
            addStats.setAddStat(StatType.ELEMENTAL_MASTERY, addStats.getAddStat(StatType.ELEMENTAL_MASTERY) + 0.20);
            addStats.setAddStat(StatType.ELEMENTAL_AFFINITY, addStats.getAddStat(StatType.ELEMENTAL_AFFINITY) + 0.10);
            addStats.setAddStat(StatType.ATK_SPEED, addStats.getAddStat(StatType.ATK_SPEED) + 0.0125);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 2.5);
        } else if (newItem.getItem() == Items.ELYTRA) {
            LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
            LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
            if (!addStatsL.isPresent()) return;
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            if (!multStatsL.isPresent()) return;
            IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            addStats.setAddStat(StatType.FALL_DMG_RES, addStats.getAddStat(StatType.FALL_DMG_RES) + 0.25);
            addStats.setAddStat(StatType.CRIT_DMG, addStats.getAddStat(StatType.CRIT_DMG) + 0.25);
            multStats.setMultStat(StatType.DASH_LENGTH, multStats.getMultStat(StatType.DASH_LENGTH) + 0.2);
            addStats.setAddStat(StatType.ADDITIONAL_JUMP, addStats.getAddStat(StatType.ADDITIONAL_JUMP) + 1);
            addStats.setAddStat(StatType.MOVEMENT_SPEED, addStats.getAddStat(StatType.MOVEMENT_SPEED) + 0.05);
            addStats.setAddStat(StatType.CRIT_RATE, addStats.getAddStat(StatType.CRIT_RATE) + 0.05);
            addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) + 5);
        }
        LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
        if (!addStatsL.isPresent()) return;
        IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        LazyOptional<IDirtyStats> dirtyStatsL = entity.getCapability(ModCapabilities.DIRTY_STATS);
        if (!dirtyStatsL.isPresent()) return;
        IDirtyStats dirtyStats = dirtyStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        dirtyStats.setDirtyStat(StatType.ARMOR, true);
        dirtyStats.setDirtyStat(StatType.DASH_LENGTH, true);
        dirtyStats.setDirtyStat(StatType.DASH_COOLDOWN, true);
        dirtyStats.setDirtyStat(StatType.KNOCKBACK_RES, true);
        dirtyStats.setDirtyStat(StatType.KNOCKBACK_STRENGTH_AMP, true);
        dirtyStats.setDirtyStat(StatType.ARMOR_PEN, true);
        dirtyStats.setDirtyStat(StatType.CRIT_DMG, true);
        dirtyStats.setDirtyStat(StatType.CRIT_RATE, true);
        dirtyStats.setDirtyStat(StatType.ALL_DMG_AMP, true);
        dirtyStats.setDirtyStat(StatType.ALL_DMG_RES, true);
        dirtyStats.setDirtyStat(StatType.EFFECT_RES, true);
        dirtyStats.setDirtyStat(StatType.ELEMENTAL_AFFINITY, true);
        dirtyStats.setDirtyStat(StatType.ELEMENTAL_MASTERY, true);
        dirtyStats.setDirtyStat(StatType.MAX_HEALTH, true);
        dirtyStats.setDirtyStat(StatType.ADDITIONAL_JUMP, true);
    }

    @SubscribeEvent
    public static void naturalRegeneration(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        LivingEntity entity = event.getEntity();
        if (entity == null) return;
        LazyOptional<IIndependentStats> independentStatsL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        LazyOptional<IFinalStats> statsL = entity.getCapability(ModCapabilities.FINAL_STATS);
        if (!independentStatsL.isPresent() || !statsL.isPresent()) return;
        IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IFinalStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        if (independentStats.getIndependentStat(IndependentStatType.BOND_OF_LIFE) < 0) {
            independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, 0);
        }

         if (independentStats.getIndependentStat(IndependentStatType.BOND_OF_LIFE) > 0) return;

        if (stats.getFinalStat(StatType.HEALTH_REGENERATION_AMP) > 0) {
            double hpRegenAmp = stats.getFinalStat(StatType.HEALTH_REGENERATION_AMP);
            double maxHp = stats.getFinalStat(StatType.MAX_HEALTH);
            double hp = independentStats.getIndependentStat(IndependentStatType.HEALTH);
            if (hp >= maxHp) return;
            double calculations = independentStats.getIndependentStat(IndependentStatType.REGENERATION_CALCULATION);
            double regen;
            if (entity instanceof Player) {
                int food = ((Player) entity).getFoodData().getFoodLevel();
                float saturation = ((Player) entity).getFoodData().getSaturationLevel();
                saturation = Math.min(saturation, 20);
                if (saturation > 0) {
                    regen = 0.0002 * saturation * (2 - 0.05 * saturation) * hpRegenAmp * maxHp;
                    calculations = calculations + 5 * saturation * (2 - 0.05 * saturation)/20;
                } else {
                    regen = 0.000015 * food * (2 - 0.05 * food) * hpRegenAmp * maxHp;
                    calculations = calculations + 0.5 * food * (2 - 0.05 * food)/20;
                }
                independentStats.setIndependentStat(IndependentStatType.HEALTH, Math.min(hp + regen, maxHp));
                double hungerMult = 0;
                if (entity.hasEffect(MobEffects.HUNGER)) {
                    int hunger = Objects.requireNonNull(entity.getEffect(MobEffects.HUNGER)).getAmplifier();
                    hungerMult = (hunger * Math.random());
                }
                independentStats.setIndependentStat(IndependentStatType.REGENERATION_CALCULATION, calculations * (1 + hungerMult));
                if (calculations >= 160) {
                    if (saturation > 0) {
                        ((Player) entity).getFoodData().setSaturation(max(0, saturation - 1));
                    } else {
                        ((Player) entity).getFoodData().setFoodLevel(max(0, food - 1));
                    }
                    independentStats.setIndependentStat(IndependentStatType.REGENERATION_CALCULATION, 0);
                }
            } else {
                regen = 0.0005 * hpRegenAmp * maxHp;
                independentStats.setIndependentStat(IndependentStatType.HEALTH, Math.min(hp + regen, maxHp));
            }
        }
    }

    @SubscribeEvent
    public static void indicatorTicking(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        if (!(event.getEntity() instanceof ArmorStand) || !event.getEntity().getTags().contains("indicator")) return;
        LivingEntity armorStand = event.getEntity();


        armorStand.getCapability(IndicatorStatsProvider.INDICATOR_STATS).ifPresent(indicatorStats -> {
            int age = indicatorStats.getAge();
            int lifetime = indicatorStats.getLifetime();
            if (lifetime == -1) return;
            if (age >= lifetime) {
                armorStand.discard();
            } else {
                indicatorStats.setAge(age + 1);
            }
        });

        CompoundTag data = armorStand.getPersistentData();
        if (data.contains("adventurer_fantasy:Dx") && data.contains("adventurer_fantasy:Dy") && data.contains("adventurer_fantasy:Dx")) {
            double Dx = data.getDouble("adventurer_fantasy:Dx");
            double Dy = data.getDouble("adventurer_fantasy:Dy");
            double Dz = data.getDouble("adventurer_fantasy:Dz");
            armorStand.teleportTo(Dx + armorStand.getX(), Dy + armorStand.getY(), Dz + armorStand.getZ());
        }
    }

    @SubscribeEvent
    public static void reactionTicking(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = event.getEntity();
        LazyOptional<IIndependentStats> statsL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!statsL.isPresent()) return;
        IIndependentStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        double reactionTime = stats.getIndependentStat(IndependentStatType.REACTION_TIME);
        if (reactionTime > 0) {
            stats.setIndependentStat(IndependentStatType.REACTION_TIME, (int) (reactionTime - 1));
        }
    }

    @SubscribeEvent
    public static void dashTicking(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity && event.getEntity() instanceof Player)) return;
        LivingEntity entity = event.getEntity();
        LazyOptional<IIndependentStats> statsL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        LazyOptional<IFinalStats> finalStatsL = entity.getCapability(ModCapabilities.FINAL_STATS);
        if (!statsL.isPresent() || !finalStatsL.isPresent()) return;
        IIndependentStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IFinalStats finalStats = finalStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        double dashTime = stats.getIndependentStat(IndependentStatType.DASH_TIME);

        entity.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
            if (statsP.getMilkDrinkingTime() > 0) {
                statsP.setMilkDrinkingTime(statsP.getMilkDrinkingTime() - 1);
            }
        });

        if (stats.getIndependentStat(IndependentStatType.DASHES_AVAILABLE) < finalStats.getFinalStat(StatType.MAX_DASHES)) {
        if (dashTime > 0) {
            stats.setIndependentStat(IndependentStatType.DASH_TIME, (int) (dashTime - 1));
        } else if (stats.getIndependentStat(IndependentStatType.DASHES_AVAILABLE) < (int) finalStats.getFinalStat(StatType.MAX_DASHES)) {
            stats.setIndependentStat(IndependentStatType.DASHES_AVAILABLE, stats.getIndependentStat(IndependentStatType.DASHES_AVAILABLE) + 1);
            stats.setIndependentStat(IndependentStatType.DASH_TIME, (int) finalStats.getFinalStat(StatType.DASH_COOLDOWN));
        }} else {
            stats.setIndependentStat(IndependentStatType.DASH_TIME, finalStats.getFinalStat(StatType.DASH_COOLDOWN));
        }
    }

    @SubscribeEvent
    public static void shieldTicking(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = event.getEntity();
        LazyOptional<IFinalStats> entityStatsL = entity.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> independentStatsL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!entityStatsL.isPresent() || !independentStatsL.isPresent()) return;
        IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        double shield = independentStats.getIndependentStat(IndependentStatType.SHIELD);
        if (!(shield == 0)) {
            int shieldDuration =(int) independentStats.getIndependentStat(IndependentStatType.SHIELD_TIME);
            if (shieldDuration <= 0) {
                independentStats.setIndependentStat(IndependentStatType.SHIELD, 0);

                entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onShieldDecay(entity);
                                }
                            }
                        }}
                });

            } else {
                independentStats.setIndependentStat(IndependentStatType.SHIELD_TIME,shieldDuration - 1);
            }
        } else {
            independentStats.setIndependentStat(IndependentStatType.SHIELD_TIME, 0);
        }

    }

    @SubscribeEvent
    public static void invulnerabilityTicking(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        LivingEntity entity = event.getEntity();
        if (entity == null) return;
        LazyOptional<IIndependentStats> statsL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!statsL.isPresent()) return;
        IIndependentStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        int invulnerableTime = (int) stats.getIndependentStat(IndependentStatType.INVULNERABLE_TIME);
        if (invulnerableTime <= 0) return;
        stats.setIndependentStat(IndependentStatType.INVULNERABLE_TIME, invulnerableTime - 1);
    }

    @SubscribeEvent
    public static void statRecalculationTicking(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        LivingEntity entity = event.getEntity();
        if (entity == null) return;
        LazyOptional<IIndependentStats> statsL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!statsL.isPresent()) return;
        IIndependentStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        int statRecalculationTime = (int) stats.getIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME);
        if (statRecalculationTime <= 0) return;
        stats.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, statRecalculationTime - 1);
    }

    @SubscribeEvent
    public static void statRecalculation(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;
        LivingEntity entity = event.getEntity();
        if (entity == null) return;
        LazyOptional<IIndependentStats> independentStatsL = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!independentStatsL.isPresent()) return;
        IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        int statRecalculationTime = (int) independentStats.getIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME);
        if (statRecalculationTime > 0) return;
        LazyOptional<IDirtyStats> dirtyStatsL = entity.getCapability(ModCapabilities.DIRTY_STATS);
        LazyOptional<IBaseStats> baseStatsL = entity.getCapability(ModCapabilities.BASE_STATS);
        LazyOptional<IAddStats> addStatsL = entity.getCapability(ModCapabilities.ADD_STATS);
        LazyOptional<IMultStats> multStatsL = entity.getCapability(ModCapabilities.MULT_STATS);
        LazyOptional<IFinalStats> finalStatsL = entity.getCapability(ModCapabilities.FINAL_STATS);

        if (!dirtyStatsL.isPresent() || !baseStatsL.isPresent() || !addStatsL.isPresent() || !multStatsL.isPresent() || !finalStatsL.isPresent())
            return;

        IDirtyStats dirtyStats = dirtyStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IBaseStats baseStats = baseStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IFinalStats finalStats = finalStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        for (Holder<MobEffect> holder : new HashSet<>(entity.getActiveEffectsMap().keySet())) {
            if (holder.is(ModTags.STACKABLE_EFFECT) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.DEBUFFS) || holder.is(ModTags.CC_DEBUFFS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI) || holder.is(ModTags.BUFFS) || holder.is(ModTags.DOT_EFFECTS) || holder.is(ModTags.SPECIAL_BUFFS) || holder.is(ModTags.SPECIAL_DEBUFFS) || holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    if (!statsI.getActiveEffectList().containsKey(holder.get())) {
                        entity.removeEffect(holder);
                    }
                });
            } else {
                entity.removeEffect(holder);
            }
        }

        if (independentStats.getActiveEffectList().containsKey(MobEffects.WATER_BREATHING.get()) || independentStats.getActiveEffectList().containsKey(MobEffects.CONDUIT_POWER.get())) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }

        if (independentStats.getIndependentStat(IndependentStatType.HEALTH) > finalStats.getFinalStat(StatType.MAX_HEALTH)) {
            independentStats.setIndependentStat(IndependentStatType.HEALTH, finalStats.getFinalStat(StatType.MAX_HEALTH));
        }

        ItemStack weapon = ItemStack.EMPTY;
        Item mainItem;
        Item offItem;
        ItemStack mainHandItem = entity.getMainHandItem();
        ItemStack offHandItem = entity.getOffhandItem();

        if (entity instanceof Player player) {
            if (player.isSprinting()) {
                baseStats.setBaseStat(StatType.MOVEMENT_SPEED, 2.75);
                dirtyStats.setDirtyStat(StatType.MOVEMENT_SPEED, true);
            } else {
                baseStats.setBaseStat(StatType.MOVEMENT_SPEED, 2);
                dirtyStats.setDirtyStat(StatType.MOVEMENT_SPEED, true);
            }
        }

        if (!mainHandItem.isEmpty()) {
            mainItem = mainHandItem.getItem();
        } else {
            mainItem = null;
        }

        if (!offHandItem.isEmpty()) {
            offItem = offHandItem.getItem();
        } else {
            offItem = null;
        }

        if (!(mainItem == null) && (mainItem instanceof IStatModifierProvider || mainHandItem.is(ItemTags.SWORDS) || mainHandItem.is(ItemTags.PICKAXES) || mainHandItem.is(ItemTags.AXES) || mainHandItem.is(ItemTags.SHOVELS) || mainHandItem.is(ItemTags.HOES) || (mainHandItem.getItem() == Items.TRIDENT) || (mainHandItem.getItem() == Items.MACE) || (mainHandItem.getItem() == Items.BOW) || (mainHandItem.getItem() == Items.CROSSBOW))) {
            weapon = mainHandItem;
        } else if (!(offItem == null) && (offItem instanceof IStatModifierProvider || offHandItem.is(ItemTags.SWORDS) || offHandItem.is(ItemTags.PICKAXES) || offHandItem.is(ItemTags.AXES) || offHandItem.is(ItemTags.SHOVELS) || offHandItem.is(ItemTags.HOES) || (offHandItem.getItem() == Items.TRIDENT) || (offHandItem.getItem() == Items.MACE) || (offHandItem.getItem() == Items.BOW) || (offHandItem.getItem() == Items.CROSSBOW))) {
            weapon = offHandItem;
        } else if (entity instanceof Player) {
            int i = 0;
            while (i <= 35) {
                ItemStack inventoryItem = ((Player) entity).getInventory().getItem(i);
                Item iItem = inventoryItem.getItem();
                if (!(inventoryItem == ItemStack.EMPTY) && (iItem instanceof IStatModifierProvider || inventoryItem.is(ItemTags.SWORDS) || inventoryItem.is(ItemTags.PICKAXES) || inventoryItem.is(ItemTags.AXES) || inventoryItem.is(ItemTags.SHOVELS) || inventoryItem.is(ItemTags.HOES) || (inventoryItem.getItem() == Items.TRIDENT) || (inventoryItem.getItem() == Items.MACE) || (inventoryItem.getItem() == Items.BOW) || (inventoryItem.getItem() == Items.CROSSBOW))) {
                    weapon = inventoryItem;
                    i = 36;
                } else {
                    i = i + 1;
                }
            }
        }

        boolean hasInfusionEffect = false;

        for (MobEffect effect : new HashSet<>(independentStats.getActiveEffectList().keySet())) {
            Optional<ResourceKey<MobEffect>> optionalKey = BuiltInRegistries.MOB_EFFECT.getResourceKey(effect);
            if (optionalKey.isPresent()) {
                Optional<Holder.Reference<MobEffect>> optionalHolder = BuiltInRegistries.MOB_EFFECT.getHolder(optionalKey.get().location());
                if (optionalHolder.isPresent()) {
                    Holder<MobEffect> holder = optionalHolder.get();
                    if (holder.is(ModTags.ELEMENTAL_INFUSION_EFFECT)) {
                        hasInfusionEffect = true;
                        break;
                    }
                }
            }
        }

        if (entity instanceof Player && !hasInfusionEffect) {
        if (weapon.getItem() == Items.WOODEN_AXE || weapon.getItem() == Items.WOODEN_SHOVEL || weapon.getItem() == Items.WOODEN_SWORD || weapon.getItem() == Items.WOODEN_HOE || weapon.getItem() == Items.WOODEN_PICKAXE) {
            independentStats.setElementType(ElementType.NATURE);
        } else if (weapon.getItem() == Items.STONE_AXE || weapon.getItem() == Items.STONE_SHOVEL || weapon.getItem() == Items.STONE_SWORD || weapon.getItem() == Items.STONE_HOE || weapon.getItem() == Items.STONE_PICKAXE) {
            independentStats.setElementType(ElementType.EARTH);
        } else if (weapon.getItem() == Items.GOLDEN_AXE || weapon.getItem() == Items.GOLDEN_SHOVEL || weapon.getItem() == Items.GOLDEN_SWORD || weapon.getItem() == Items.GOLDEN_HOE || weapon.getItem() == Items.GOLDEN_PICKAXE) {
            independentStats.setElementType(ElementType.FIRE);
        } else if (weapon.getItem() == Items.IRON_AXE || weapon.getItem() == Items.IRON_SHOVEL || weapon.getItem() == Items.IRON_SWORD || weapon.getItem() == Items.IRON_HOE || weapon.getItem() == Items.IRON_PICKAXE) {
            independentStats.setElementType(ElementType.PHYSICAL);
        } else if (weapon.getItem() == Items.DIAMOND_AXE || weapon.getItem() == Items.DIAMOND_SHOVEL || weapon.getItem() == Items.DIAMOND_SWORD || weapon.getItem() == Items.DIAMOND_HOE || weapon.getItem() == Items.DIAMOND_PICKAXE) {
            independentStats.setElementType(ElementType.ICE);
        } else if (weapon.getItem() == Items.NETHERITE_AXE || weapon.getItem() == Items.NETHERITE_SHOVEL || weapon.getItem() == Items.NETHERITE_SWORD || weapon.getItem() == Items.NETHERITE_HOE || weapon.getItem() == Items.NETHERITE_PICKAXE) {
            independentStats.setElementType(ElementType.LAVA);
        } else if (weapon.getItem() == Items.BOW || weapon.getItem() == Items.CROSSBOW) {
            independentStats.setElementType(ElementType.WIND);
        } else if (weapon.getItem() == Items.TRIDENT) {
            independentStats.setElementType(ElementType.WATER);
        } else if (weapon.getItem() == Items.MACE) {
            independentStats.setElementType(ElementType.VOID);
        } else if (weapon == ItemStack.EMPTY) {
            independentStats.setElementType(ElementType.PHYSICAL);
        }}

        ItemStack oldItem = independentStats.getWeapon();

        if (!(weapon == oldItem)) {
            independentStats.setWeapon(weapon);
            if (oldItem.getItem() instanceof IStatModifierProvider providerOld) {
                for (StatModifier mod : providerOld.getModifiers(oldItem)) {
                    double amountOld = mod.amount();
                    StatType statOld = mod.stat();
                    dirtyStats.setDirtyStat(statOld, true);

                    if (mod.isMultiplicative()) {
                        multStats.setMultStat(statOld, multStats.getMultStat(statOld) - amountOld);
                    } else {
                        addStats.setAddStat(statOld, addStats.getAddStat(statOld) - amountOld);
                    }
                }
            }
            if (weapon.getItem() instanceof IStatModifierProvider providerNew) {
                for (StatModifier mod : providerNew.getModifiers(weapon)) {
                    double amountNew = mod.amount();
                    StatType statNew = mod.stat();
                    dirtyStats.setDirtyStat(statNew, true);

                    if (mod.isMultiplicative()) {
                        multStats.setMultStat(statNew, multStats.getMultStat(statNew) + amountNew);
                    } else {
                        addStats.setAddStat(statNew, addStats.getAddStat(statNew) + amountNew);
                    }
                }
            }
        }

        independentStats.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 4);

        StatType[] statList = StatType.values();

        for (StatType stat : Arrays.stream(statList).toList()) {
            if (dirtyStats.getDirtyStat(stat)) {
                double multStat = max(0, multStats.getMultStat(stat));


                if ((stat == StatType.ATK_SPEED) || (stat == StatType.ATK_COOLDOWN)) {
                    double atkSpeed = (baseStats.getBaseStat(StatType.ATK_SPEED) + addStats.getAddStat(StatType.ATK_SPEED) * multStats.getMultStat(StatType.ATK_SPEED));
                    atkSpeed = Math.max(Math.min(atkSpeed, stat.getMaxValue()), stat.getMinValue());
                    finalStats.setFinalStat(StatType.ATK_SPEED,atkSpeed);
                    double atkCooldown = (baseStats.getBaseStat(StatType.ATK_COOLDOWN) + addStats.getAddStat(StatType.ATK_COOLDOWN)) * multStats.getMultStat(StatType.ATK_COOLDOWN) / (1 + finalStats.getFinalStat(StatType.ATK_SPEED));
                    finalStats.setFinalStat(StatType.ATK_COOLDOWN, atkCooldown);
                    dirtyStats.setDirtyStat(StatType.ATK_SPEED, false);
                    dirtyStats.setDirtyStat(StatType.ATK_COOLDOWN, false);
                } else if (stat == StatType.ELEMENTAL_AFFINITY) {
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                    newStat = Math.max(Math.min(newStat, stat.getMaxValue()), stat.getMinValue());
                    newStat = Math.min(1, newStat);
                    finalStats.setFinalStat(stat, newStat);
                    dirtyStats.setDirtyStat(stat, false);
                    int reactionCooldown = (int) (40 - 39 * newStat);
                    reactionCooldown = max(1, reactionCooldown);

                    independentStats.setIndependentStat(IndependentStatType.REACTION_COOLDOWN, reactionCooldown);
                } else if (stat == StatType.MOVEMENT_SPEED) {
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                    newStat = Math.max(Math.min(newStat, stat.getMaxValue()), stat.getMinValue());
                    finalStats.setFinalStat(stat, newStat);
                    dirtyStats.setDirtyStat(stat, false);
                    if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                        Objects.requireNonNull(entity.getAttribute(Attributes.MOVEMENT_SPEED)).addOrReplacePermanentModifier( new AttributeModifier(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "speed"), newStat / 2 - 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                     } else if (stat == StatType.MAX_HEALTH) {
                    double oldStat = finalStats.getFinalStat(StatType.MAX_HEALTH);
                double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                    newStat = Math.max(Math.min(newStat, stat.getMaxValue()), stat.getMinValue());
                finalStats.setFinalStat(stat,  newStat);
                double newHP = independentStats.getIndependentStat(IndependentStatType.HEALTH)*newStat/oldStat;
                independentStats.setIndependentStat(IndependentStatType.HEALTH, newHP);
                dirtyStats.setDirtyStat(stat, false);
                baseStats.setBaseStat(StatType.MAX_BOND_OF_LIFE, 2 * newStat);
                dirtyStats.setDirtyStat(StatType.MAX_BOND_OF_LIFE, true);

                if (entity.getControllingPassenger() instanceof Player player) {
                    double finalNewStat = newStat;
                    player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                        statsP.setMountMaxHealth(finalNewStat);
                        statsP.setMountHealth(newHP);
                        player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> statsI.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1));
                    });
                }
            } else if (stat == StatType.MAX_BOND_OF_LIFE) {
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat))* multStat;
                    newStat = Math.max(Math.min(newStat, stat.getMaxValue()), stat.getMinValue());
                    double oldStat = finalStats.getFinalStat(stat);
                    finalStats.setFinalStat(StatType.MAX_BOND_OF_LIFE, newStat);
                    independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, independentStats.getIndependentStat(IndependentStatType.BOND_OF_LIFE) * newStat/oldStat);
                } else if (stat == StatType.MAX_MANA) {
                    double oldStat = finalStats.getFinalStat(StatType.MAX_MANA);
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                    newStat = Math.max(Math.min(newStat, stat.getMaxValue()), stat.getMinValue());
                    finalStats.setFinalStat(stat, newStat);
                    double newMana= independentStats.getIndependentStat(IndependentStatType.MANA)*newStat/oldStat;
                    independentStats.setIndependentStat(IndependentStatType.MANA, newMana);
                    dirtyStats.setDirtyStat(stat, false);
                } else {
                        double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                        newStat = Math.max(Math.min(newStat, stat.getMaxValue()), stat.getMinValue());
                        if (stat.getShouldBeTruncated()) {
                            finalStats.setFinalStat(stat, (int) newStat);
                        } else {
                            finalStats.setFinalStat(stat, newStat);
                        }
                        dirtyStats.setDirtyStat(stat, false);
                }
            }
        }
        if (entity instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                player.getCapability(ModCapabilities.BASE_STATS).ifPresent(statsB -> {
                    player.getCapability(ModCapabilities.ADD_STATS).ifPresent(statsA -> {
                        player.getCapability(ModCapabilities.MULT_STATS).ifPresent(statsM -> {
                            ModNetworking.sendToPlayer(new ClientStatReceiver(player.getFoodData().getSaturationLevel(), independentStats.serializeNBT(), finalStats.serializeNBT(), statsP.serializeNBT(), statsB.serializeNBT(), statsA.serializeNBT(), statsM.serializeNBT()), player);
                        });
                    });
                });
            });
        }
    }

    @SubscribeEvent
    public static void healing(HealingEvent event) {
        Level level = event.getTarget().level();
        if (level.isClientSide) return;
        LivingEntity healer = event.getHealer();
        LivingEntity target = event.getTarget();
        if (target == null) return;

        LazyOptional<IFinalStats> targetStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> independentStatsL = target.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!targetStatsL.isPresent() || !independentStatsL.isPresent()) return;
        IFinalStats targetStats = targetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        double health = independentStats.getIndependentStat(IndependentStatType.HEALTH);
        double maxHealth = targetStats.getFinalStat(StatType.MAX_HEALTH);
        double bondOfLife = independentStats.getIndependentStat(IndependentStatType.BOND_OF_LIFE);
        double healAmount = event.getHealAmount();
        double incomingHealAmp = targetStats.getFinalStat(StatType.INCOMING_HEALING_AMP);

        if (healer == null) {
             double healMult = healAmount * incomingHealAmp;
            if (healMult <= bondOfLife) {
                double finalBondOfLife = bondOfLife - healMult;
                independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, finalBondOfLife);

                double x = target.getX();
                double y = target.getY();
                double z = target.getZ();
                ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                x = x + Math.random() - 0.5;
                y = y - 1.5 + Math.random();
                z = z + Math.random() - 0.5;
                healMult = (int) (healMult * 100);
                String text = "-" + healMult / 100;
                armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x760000);
                armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);

                armorStand.addTag("indicator");
                armorStand.addTag("indicatorToInitialise");
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Marker", true);
                armorStand.load(tag);
                armorStand.setPos(x, y, z);
                armorStand.isMarker();
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setNoBasePlate(true);
                armorStand.setShowArms(false);
                level.addFreshEntity(armorStand);


            } else {
                double finalHeal = healMult - bondOfLife;
                double incomingHeal = Math.min(health + finalHeal, maxHealth);
                independentStats.setIndependentStat(IndependentStatType.HEALTH, incomingHeal);

                double healToEffect = incomingHeal - health;
                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onBeingHealed(target, null, healToEffect);
                                }
                            }
                        }}
                });

                if (bondOfLife > 0) {

                    target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onBondOfLifeDispel(target);
                                    }
                                }
                            }}
                    });

                    double xb = target.getX();
                    double yb = target.getY();
                    double zb = target.getZ();

                    ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                    xb = xb + Math.random() - 0.5;
                    yb = yb - 1.5 + Math.random();
                    zb = zb + Math.random() - 0.5;
                    bondOfLife = (int) (bondOfLife * 100);
                    String text = "-" + bondOfLife / 100;
                    armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xb);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yb);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zb);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                    armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                    armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x760000);
                    armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
                    armorStand.addTag("indicator");
                    armorStand.addTag("indicatorToInitialise");
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("Marker", true);
                    armorStand.load(tag);
                    armorStand.setPos(xb, yb, zb);
                    armorStand.isMarker();
                    armorStand.setInvisible(true);
                    armorStand.setNoGravity(true);
                    armorStand.setNoBasePlate(true);
                    armorStand.setShowArms(false);
                    level.addFreshEntity(armorStand);
                }

                independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, 0);

                double x = target.getX();
                double y = target.getY();
                double z = target.getZ();

                ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                x = x + Math.random() - 0.5;
                y = y + 1 + Math.random();
                z = z + Math.random() - 0.5;
                incomingHeal = (int) ((incomingHeal-health) * 100);
                String text = "+" + incomingHeal / 100;
                armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x2deb49);
                armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
                armorStand.addTag("indicator");
                armorStand.addTag("indicatorToInitialise");
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Marker", true);
                armorStand.load(tag);
                armorStand.setPos(x, y, z);
                armorStand.isMarker();
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setNoBasePlate(true);
                armorStand.setShowArms(false);
                level.addFreshEntity(armorStand);


                if (target.getControllingPassenger() instanceof Player player) {
                    double finalIncomingHeal = incomingHeal;
                    player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                        statsP.setMountHealth(finalIncomingHeal);
                        player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> statsI.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1));
                    });
                }

            }
        } else {
            LazyOptional<IFinalStats> healerStatsL = healer.getCapability(ModCapabilities.FINAL_STATS);
            if (!healerStatsL.isPresent()) return;
            IFinalStats healerStats = healerStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            double outgoingHealAmp = healerStats.getFinalStat(StatType.OUTGOING_HEALING_AMP);
            double healMult = healAmount * incomingHealAmp * outgoingHealAmp;
            if (healMult <= bondOfLife) {
                double finalBondOfLife = bondOfLife - healMult;
                independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, finalBondOfLife);

                double x = target.getX();
                double y = target.getY();
                double z = target.getZ();
                ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                x = x + Math.random() - 0.5;
                y = y + 1 + Math.random();
                z = z + Math.random() - 0.5;
                healMult = (int) (healMult * 100);
                String text = "-" + healMult / 100;
                armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x760000);
                armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
                armorStand.addTag("indicator");
                armorStand.addTag("indicatorToInitialise");
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Marker", true);
                armorStand.load(tag);
                armorStand.setPos(x, y, z);
                armorStand.isMarker();
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setNoBasePlate(true);
                armorStand.setShowArms(false);
                level.addFreshEntity(armorStand);

            } else {
                double finalHeal = healMult - bondOfLife;
                double incomingHeal = Math.min(health + finalHeal, maxHealth);
                independentStats.setIndependentStat(IndependentStatType.HEALTH, incomingHeal);

                double finalIncomingHeal = incomingHeal - health;
                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onBeingHealed(target, healer, finalIncomingHeal);
                                }
                            }
                        }}
                });

                healer.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onHeal(healer, target, finalIncomingHeal);
                                }
                            }
                        }}
                });

                if (bondOfLife > 0) {

                    target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onBondOfLifeDispel(target);
                                    }
                                }
                            }}
                    });

                    double xb = target.getX();
                    double yb = target.getY();
                    double zb = target.getZ();

                    ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                    xb = xb + Math.random() - 0.5;
                    yb = yb + 1 + Math.random();
                    zb = zb + Math.random() - 0.5;
                    bondOfLife = (int) (bondOfLife * 100);
                    String text = "-" + bondOfLife / 100;
                    armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xb);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yb);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zb);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                    armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                    armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                    armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x760000);
                    armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
                    armorStand.addTag("indicator");
                    armorStand.addTag("indicatorToInitialise");
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("Marker", true);
                    armorStand.load(tag);
                    armorStand.setPos(xb, yb, zb);
                    armorStand.isMarker();
                    armorStand.setInvisible(true);
                    armorStand.setNoGravity(true);
                    armorStand.setNoBasePlate(true);
                    armorStand.setShowArms(false);
                    level.addFreshEntity(armorStand);
                }

                independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, 0);

                double x = target.getX();
                double y = target.getY();
                double z = target.getZ();

                incomingHeal = (int) ((incomingHeal-health) * 100);
                String text = "+" + incomingHeal/100;

                ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                x = x + Math.random() - 0.5;
                y = y +1 + Math.random();
                z = z + Math.random() - 0.5;
                armorStand.addTag("indicator");
                armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x2deb49);
                armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
                CompoundTag tag = new CompoundTag();
                armorStand.addTag("indicator");
                tag.putBoolean("Marker", true);
                armorStand.load(tag);
                armorStand.setPos(x, y, z);
                armorStand.isMarker();
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setNoBasePlate(true);
                armorStand.setShowArms(false);
                level.addFreshEntity(armorStand);

                if (target.getControllingPassenger() instanceof Player player) {
                    double finalIncomingHeal1 = incomingHeal;
                    player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                        statsP.setMountHealth(finalIncomingHeal1);
                        player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> statsI.setIndependentStat(IndependentStatType.STAT_RECALCULATION_TIME, 1));
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void bindingByLife (BindByLifeEvent event) {
        Level level = event.getTarget().level();
        if (level.isClientSide) return;
        LivingEntity target = event.getTarget();
        if (target == null) return;
        LazyOptional<IFinalStats> targetStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> independentStatsL = target.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!targetStatsL.isPresent() || !independentStatsL.isPresent()) return;
        IFinalStats targetStats = targetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        System.out.println("Applying bond of life. Amount: " + event.getBondOfLife());

        double initialBondOfLife = independentStats.getIndependentStat(IndependentStatType.BOND_OF_LIFE);
        double eventBondOfLife = event.getBondOfLife();
        double bondOfLife = Math.min(targetStats.getFinalStat(StatType.MAX_BOND_OF_LIFE), initialBondOfLife + eventBondOfLife);
        double newBondOfLife = bondOfLife - initialBondOfLife;

        double finalNewBondOfLife = newBondOfLife;
        target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
            if (!stats.getActiveEffectList().isEmpty()) {
                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                    if (effectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                            ((ICustomStatusEffect) copiedEffect).onBoundByLife(target, null, finalNewBondOfLife - stats.getIndependentStat(IndependentStatType.BOND_OF_LIFE));
                        }
                    }
                }}
        });

        if (event.getApplier() instanceof LivingEntity applier) {
            applier.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                if (!stats.getActiveEffectList().isEmpty()) {
                    for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                        if (effectMap.containsKey(copiedEffect)) {
                            StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                            if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                ((ICustomStatusEffect) copiedEffect).onBindByLife(applier, target, finalNewBondOfLife - stats.getIndependentStat(IndependentStatType.BOND_OF_LIFE));
                            }
                        }
                    }}
            });
        }

        independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, bondOfLife);
        System.out.println("[DEBUG] Created Bond of Life. New value: " + bondOfLife + ". Maximum value: " + targetStats.getFinalStat(StatType.MAX_BOND_OF_LIFE));

        double xb = target.getX();
        double yb = target.getY();
        double zb = target.getZ();

        ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
        xb = xb + Math.random() - 0.5;
        yb = yb + 1 + Math.random();
        zb = zb + Math.random() - 0.5;
        newBondOfLife = (int) (newBondOfLife * 100);
        String text = "+" + newBondOfLife / 100;

        armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:x", xb);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:y", yb);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:z", zb);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
        armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
        armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
        armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x760000);
        armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
        armorStand.addTag("indicator");
        armorStand.addTag("indicatorToInitialise");
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Marker", true);
        armorStand.load(tag);
        armorStand.setPos(xb, yb, zb);
        armorStand.isMarker();
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setNoBasePlate(true);
        armorStand.setShowArms(false);
        level.addFreshEntity(armorStand);
    }

    @SubscribeEvent
    public static void shielding(ShieldingEvent event) {
        Level level = event.getTarget().level();
        if (level.isClientSide) return;
        LivingEntity shieldProvider = event.getShieldProvider();
        LivingEntity target = event.getTarget();
        if (target == null) return;

        LazyOptional<IFinalStats> targetStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> independentStatsL = target.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!targetStatsL.isPresent() || !independentStatsL.isPresent()) return;
        IFinalStats targetStats = targetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        double shield = independentStats.getIndependentStat(IndependentStatType.SHIELD);
        double shieldStrength = event.getShieldStrength();
        double incomingShieldAmp = targetStats.getFinalStat(StatType.INCOMING_SHIELD_STRENGTH_AMP);
        int newDuration = event.getShieldDuration();
        int duration = (int) independentStats.getIndependentStat(IndependentStatType.SHIELD_TIME);

        if (shieldProvider == null) {
            double shieldMult = shieldStrength * incomingShieldAmp;
            if (shieldMult < shield) {
                independentStats.setIndependentStat(IndependentStatType.SHIELD, shield);
                independentStats.setIndependentStat(IndependentStatType.SHIELD_TIME, max(duration, newDuration));
            } else {
                independentStats.setIndependentStat(IndependentStatType.SHIELD, shieldMult);
                independentStats.setIndependentStat(IndependentStatType.SHIELD_TIME, newDuration);

                double finalShieldMult = shieldMult;
                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReceivingShield(target, null, finalShieldMult);
                                }
                            }
                        }}
                });

                double x = target.getX();
                double y = target.getY();
                double z = target.getZ();

                shieldMult = (int) (shieldMult);
                String text = "+" + shieldMult / 100;

                ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                x = x + Math.random() - 0.5;
                y = y + 1 + Math.random();
                z = z + Math.random() - 0.5;
                armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0xecfa0);
                armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
                armorStand.addTag("indicator");
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Marker", true);
                armorStand.load(tag);
                armorStand.setPos(x, y, z);
                armorStand.isMarker();
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setNoBasePlate(true);
                armorStand.setShowArms(false);
                level.addFreshEntity(armorStand);
            }


        } else {
            LazyOptional<IFinalStats> shieldProviderStatsL = shieldProvider.getCapability(ModCapabilities.FINAL_STATS);
            if (!shieldProviderStatsL.isPresent()) return;
            IFinalStats shieldProviderStats = shieldProviderStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            double outgoingShieldAmp = shieldProviderStats.getFinalStat(StatType.OUTGOING_SHIELD_STRENGTH_AMP);
            double shieldMult = shieldStrength * incomingShieldAmp * outgoingShieldAmp;
            double durationAmp = shieldProviderStats.getFinalStat(StatType.EFFECT_DURATION);
            newDuration = (int) (newDuration * durationAmp);

            if (shieldMult <= shield) {
                independentStats.setIndependentStat(IndependentStatType.SHIELD, shield);
                independentStats.setIndependentStat(IndependentStatType.SHIELD_TIME, max(duration, newDuration));
            } else {
                independentStats.setIndependentStat(IndependentStatType.SHIELD, shieldMult);
                independentStats.setIndependentStat(IndependentStatType.SHIELD_TIME, newDuration);

                double finalShieldMult = shieldMult;
                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onReceivingShield(target, shieldProvider, finalShieldMult);
                                }
                            }
                        }}
                });

                double x = target.getX();
                double y = target.getY();
                double z = target.getZ();

                shieldMult = (int) (shieldMult * 100);
                String text = "+" + shieldMult;

                ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
                x = x + Math.random() - 0.5;
                y = y + 1 + Math.random();
                z = z + Math.random() - 0.5;
                armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
                armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
                armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
                armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0xecfa0);
                armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
                armorStand.addTag("indicator");
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("Marker", true);
                armorStand.load(tag);
                armorStand.setPos(x, y, z);
                armorStand.isMarker();
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setNoBasePlate(true);
                armorStand.setShowArms(false);
                level.addFreshEntity(armorStand);
            }

        }
    }

    @SubscribeEvent
    public static void EffectApplicationEvent(EffectApplicationEvent event) {
        Level level = event.getTarget().level();
        if (level.isClientSide) return;
        LivingEntity target = event.getTarget();
        LivingEntity caster = event.getCaster();
        MobEffect effect = event.getEffect();
        if (target == null) return;
        LazyOptional<IFinalStats> targetStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> targetStatsLI = target.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!targetStatsL.isPresent() || !targetStatsLI.isPresent()) return;
        System.out.println("Applying status effect to " + target.getName());
        IFinalStats targetStats = targetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats targetStatsI = targetStatsLI.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        if (caster == null) {
            double baseChance = event.getEffectChance();
            double effectRes = targetStats.getFinalStat(StatType.EFFECT_RES);
            double effectChance;
            if (event.getIsCertain()) {

                target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                    Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                    if (!stats.getActiveEffectList().isEmpty()) {
                        for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                            if (effectMap.containsKey(copiedEffect)) {
                                StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                    ((ICustomStatusEffect) copiedEffect).onEffectReception(target, null, event.getEffect());
                                }
                            }
                        }}
                });

                targetStatsI.addActiveEffectEntry(event.getEffect(), new StatusEffectInstanceEntry(event.getEffectDuration(), event.getEffectAmp(), event.getStacks(), event.getMaxStacks(), null, event.getData(), false), target);
            } else {

                BuiltInRegistries.MOB_EFFECT.getResourceKey(effect).ifPresent(key -> {
                    double finalBaseChance = baseChance;
                    double finalEffectChance;
                if (key.getOrThrow(target).is(ModTags.DEBUFFS)) {
                    if (key.getOrThrow(target).is(ModTags.CC_DEBUFFS)) {
                        finalBaseChance -= targetStats.getFinalStat(StatType.CC_RES);
                    }
                    finalEffectChance = finalBaseChance - effectRes;
                } else {
                    finalEffectChance = baseChance;
                }
                double chance = Math.random();
                if (chance <= finalEffectChance) {

                    target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onEffectReception(target, null, event.getEffect());
                                    }
                                }
                            }}
                    });

                        targetStatsI.addActiveEffectEntry(event.getEffect(), new StatusEffectInstanceEntry(event.getEffectDuration(), event.getEffectAmp(), event.getStacks(), event.getMaxStacks(), null, new CompoundTag(), false), target);
                } else {
                    if (event.getEffect() instanceof ICustomStatusEffect customEffect) {
                        customEffect.onThisEffectResist(target, null);
                    }

                    target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onEffectResist(target, null, event.getEffect());
                                    }
                                }
                            }}
                    });
                }
            });}
        } else {
            LazyOptional<IFinalStats> casterStatsL = caster.getCapability(ModCapabilities.FINAL_STATS);
            if (!casterStatsL.isPresent()) return;
            IFinalStats casterStats = casterStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            final double baseChance = event.getEffectChance();
            double effectRes = targetStats.getFinalStat(StatType.EFFECT_RES);

            double effectHitRate = casterStats.getFinalStat(StatType.EFFECT_HIT_RATE);
            double effectAmp = casterStats.getFinalStat(StatType.EFFECT_EFFICIENCY);


            if (event.getIsCertain()) {
                BuiltInRegistries.MOB_EFFECT.getResourceKey(effect).ifPresent(key -> {
                    double finalEffectAmp = effectAmp;
                    if (key.getOrThrow(target).is(ModTags.CC_DEBUFFS)) {
                        finalEffectAmp *= casterStats.getFinalStat(StatType.CC_DEBUFF_EFFICIENCY);
                    }

                    target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onEffectReception(target, caster, event.getEffect());
                                    }
                                }
                            }
                        }
                    });

                    caster.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                        Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                        if (!stats.getActiveEffectList().isEmpty()) {
                            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                if (effectMap.containsKey(copiedEffect)) {
                                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                        ((ICustomStatusEffect) copiedEffect).onEffectApplication(caster, target, event.getEffect());
                                    }
                                }
                            }
                        }
                    });

                    targetStatsI.addActiveEffectEntry(event.getEffect(), new StatusEffectInstanceEntry(((int) (event.getEffectDuration() * casterStats.getFinalStat(StatType.EFFECT_DURATION))), event.getEffectAmp() * finalEffectAmp, event.getStacks(), event.getMaxStacks(), caster.getUUID(), new CompoundTag(), false), target);
                });
            } else {
                BuiltInRegistries.MOB_EFFECT.getResourceKey(effect).ifPresent(key -> {
                    double finalBaseChance = baseChance;
                    double finalEffectAmp = effectAmp;
                    double effectChance;
                    if (key.getOrThrow(target).is(ModTags.DEBUFFS)) {
                        if (key.getOrThrow(target).is(ModTags.CC_DEBUFFS)) {
                            finalBaseChance -= targetStats.getFinalStat(StatType.CC_RES);
                            finalEffectAmp *= casterStats.getFinalStat(StatType.CC_DEBUFF_EFFICIENCY);
                        }
                        if (!(key.getOrThrow(target).is(ModTags.DOT_EFFECTS))) {
                            finalEffectAmp *= casterStats.getFinalStat(StatType.NON_DAMAGING_DEBUFF_EFFICIENCY);
                        }
                        effectChance = finalBaseChance - effectRes + effectHitRate;
                    } else {
                        effectChance = baseChance + effectHitRate;
                    }
                    double chance = Math.random();
                    if (chance <= effectChance) {

                        target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                            if (!stats.getActiveEffectList().isEmpty()) {
                                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                    if (effectMap.containsKey(copiedEffect)) {
                                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                            ((ICustomStatusEffect) copiedEffect).onEffectReception(target, caster, event.getEffect());
                                        }
                                    }
                                }}
                        });

                        caster.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                            if (!stats.getActiveEffectList().isEmpty()) {
                                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                    if (effectMap.containsKey(copiedEffect)) {
                                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                            ((ICustomStatusEffect) copiedEffect).onEffectApplication(caster, target, event.getEffect());
                                        }
                                    }
                                }}
                        });

                        targetStatsI.addActiveEffectEntry(event.getEffect(), new StatusEffectInstanceEntry(((int) (event.getEffectDuration() * casterStats.getFinalStat(StatType.EFFECT_DURATION))), event.getEffectAmp() * finalEffectAmp, event.getStacks(), event.getMaxStacks(), caster.getUUID(), new CompoundTag(), false), target);
                    } else {
                        if (event.getEffect() instanceof ICustomStatusEffect customEffect) {
                            customEffect.onThisEffectResist(target, caster);
                            customEffect.onThisEffectBeingResisted(caster, target);
                        }

                        target.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                            if (!stats.getActiveEffectList().isEmpty()) {
                                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                    if (effectMap.containsKey(copiedEffect)) {
                                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                            ((ICustomStatusEffect) copiedEffect).onEffectResist(target, caster, event.getEffect());
                                        }
                                    }
                                }}
                        });

                        caster.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                            if (!stats.getActiveEffectList().isEmpty()) {
                                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                                    if (effectMap.containsKey(copiedEffect)) {
                                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                            ((ICustomStatusEffect) copiedEffect).onEffectBeingResisted(caster, target, event.getEffect());
                                        }
                                    }
                                }}
                        });
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void environmentalElementApplication(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity.level().isClientSide()) return;

        LazyOptional<IIndependentStats> targetStatsLI = entity.getCapability(ModCapabilities.INDEPENDENT_STATS);
        if (!targetStatsLI.isPresent()) return;
        IIndependentStats targetStatsI = targetStatsLI.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));


        if (entity.isInWaterRainOrBubble() && !targetStatsI.getActiveEffectList().containsKey(ModEffects.WET_EFFECT.get())) {
            targetStatsI.addActiveEffectEntry(ModEffects.WET_EFFECT.get(), new StatusEffectInstanceEntry(120, 1, 0, 0, null, new CompoundTag(), false), entity);
        }
        if (entity.isInPowderSnow && !targetStatsI.getActiveEffectList().containsKey(ModEffects.FROSTED_EFFECT.get())) {
            targetStatsI.addActiveEffectEntry(ModEffects.FROSTED_EFFECT.get(), new StatusEffectInstanceEntry(120, 1, 0, 0, null, new CompoundTag(), false), entity);
        }
        if (entity.isInLava() && !targetStatsI.getActiveEffectList().containsKey(ModEffects.MOLTEN_EFFECT.get())) {
            targetStatsI.addActiveEffectEntry(ModEffects.MOLTEN_EFFECT.get(), new StatusEffectInstanceEntry(120, 1, 0, 0, null, new CompoundTag(), false), entity);
        }
    }

    @SubscribeEvent
    public static void breakDenial(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = event.getPlayer().level();
        if (player == null || (level.isClientSide())) return;
        player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
            if (statsI.getActiveEffectList().containsKey(ModEffects.CREATIVE_SHOCK_EFFECT.get())) {
            event.setCanceled(true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.3F, 0.7F);
            }});
    }

    @SubscribeEvent
    public static void placementDenial(BlockEvent.EntityPlaceEvent event) {
        Entity player = event.getEntity();
        if (player instanceof Player) {
            Level level = event.getEntity().level();
            if ((level.isClientSide())) return;
            player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
            if (statsI.getActiveEffectList().containsKey(ModEffects.CREATIVE_SHOCK_EFFECT.get())) {
                event.setCanceled(true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.3F, 0.7F);
            }});
        }
    }

    @SubscribeEvent
    public static void buttonPress(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (ModKeyBindings.DASH_KEY.consumeClick() && Minecraft.getInstance().screen == null) {
                ModNetworking.sendToServer(new DashInputReceiver());
            }
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    double jumps = statsI.getIndependentStat(IndependentStatType.ADDITIONAL_JUMPS_AVAILABLE);
                    if (jumps > 0) {
                        if (Minecraft.getInstance().options.keyJump.consumeClick() && Minecraft.getInstance().screen == null) {
                            ModNetworking.sendToServer(new AdditionalJumpInputReceiver());
                        }
                    }
                });}
            if (Minecraft.getInstance().options.keyUse.isDown() && Minecraft.getInstance().screen == null) {
                ModNetworking.sendToServer(new InteractInputReceiver());
            }
        }
    }

    @SubscribeEvent
    public static void checkGround(LivingEvent.LivingTickEvent event) {
        LivingEntity player = event.getEntity();
        if (player != null && !player.level().isClientSide()) {
            LazyOptional<IIndependentStats> independentStatsL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);
            LazyOptional<IFinalStats> statsL = player.getCapability(ModCapabilities.FINAL_STATS);

            if (!statsL.isPresent() || !independentStatsL.isPresent()) return;

            IFinalStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            if (!(player instanceof Player)) return;
            if (player.onGround() || player.isInLava() || player.isInWater()) {
                player.getTags().add("onGround");
                independentStats.setIndependentStat(IndependentStatType.ADDITIONAL_JUMPS_AVAILABLE, (int) stats.getFinalStat(StatType.ADDITIONAL_JUMP));

            } else {
                player.getTags().remove("onGround");
            }
        }
    }

    @SubscribeEvent
    public static void changeVanillaEquipmentTooltips (ItemTooltipEvent event) {
        ItemStack item = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        Style buff = Style.EMPTY.withColor(0x1d1ad1);
        Style name = Style.EMPTY.withColor(0xffffff);
        Style debuff = Style.EMPTY.withColor(0xd11a1a);

        if (item.is(Items.LEATHER_HELMET) || item.is(Items.LEATHER_LEGGINGS) || item.is(Items.LEATHER_BOOTS)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+1.25 Armor").withStyle(buff));
            tooltip.add(Component.literal("+6% Dash Length").withStyle(buff));
            tooltip.add(Component.literal("-4% Dash Cooldown").withStyle(buff));
        } else if (item.is(Items.LEATHER_CHESTPLATE)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+2 Armor").withStyle(buff));
            tooltip.add(Component.literal("+6% Dash Length").withStyle(buff));
            tooltip.add(Component.literal("-4% Dash Cooldown").withStyle(buff));
        } else if (item.is(Items.CHAINMAIL_BOOTS) || item.is(Items.CHAINMAIL_HELMET)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+2 Armor").withStyle(buff));
            tooltip.add(Component.literal("+5% Knockback").withStyle(buff));
            tooltip.add(Component.literal("+1.25% Armor Penetration").withStyle(buff));
        } else if (item.is(Items.CHAINMAIL_LEGGINGS) || item.is(Items.CHAINMAIL_CHESTPLATE)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+3 Armor").withStyle(buff));
            tooltip.add(Component.literal("+5% Knockback").withStyle(buff));
            tooltip.add(Component.literal("+1.25% Armor Penetration").withStyle(buff));
        } else if (item.is(Items.GOLDEN_BOOTS) || item.is(Items.GOLDEN_HELMET)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+2 Armor").withStyle(buff));
            tooltip.add(Component.literal("+6.25% Crit Dmg").withStyle(buff));
            tooltip.add(Component.literal("+1.25% Crit Rate").withStyle(buff));
        } else if (item.is(Items.GOLDEN_LEGGINGS) || item.is(Items.GOLDEN_CHESTPLATE)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+3 Armor").withStyle(buff));
            tooltip.add(Component.literal("+6.25% Crit Dmg").withStyle(buff));
            tooltip.add(Component.literal("+1.25% Crit Rate").withStyle(buff));
        } else if (item.is(Items.IRON_BOOTS) || item.is(Items.IRON_HELMET)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+2.25 Armor").withStyle(buff));
            tooltip.add(Component.literal("+6.25% Effect Res").withStyle(buff));
            tooltip.add(Component.literal("+1.25% All Dmg Res").withStyle(buff));
        } else if (item.is(Items.IRON_LEGGINGS)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+5 Armor").withStyle(buff));
            tooltip.add(Component.literal("+6.25% Effect Res").withStyle(buff));
            tooltip.add(Component.literal("+1.25% All Dmg Res").withStyle(buff));
        } else if (item.is(Items.IRON_CHESTPLATE)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+6 Armor").withStyle(buff));
            tooltip.add(Component.literal("+6.25% Effect Res").withStyle(buff));
            tooltip.add(Component.literal("+1.25% All Dmg Res").withStyle(buff));
        } else if (item.is(Items.DIAMOND_BOOTS) || item.is(Items.DIAMOND_HELMET)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+3 Armor").withStyle(buff));
            tooltip.add(Component.literal("+2.5% Knockback Res").withStyle(buff));
            tooltip.add(Component.literal("+1.875% All Dmg Res").withStyle(buff));
            tooltip.add(Component.literal("+1.25% All Dmg Amp").withStyle(buff));
        } else if (item.is(Items.DIAMOND_LEGGINGS) || item.is(Items.DIAMOND_CHESTPLATE)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+7 Armor").withStyle(buff));
            tooltip.add(Component.literal("+2.5% Knockback Res").withStyle(buff));
            tooltip.add(Component.literal("+1.875% All Dmg Res").withStyle(buff));
            tooltip.add(Component.literal("+1.25% All Dmg Amp").withStyle(buff));
        } else if (item.is(Items.NETHERITE_BOOTS) || item.is(Items.NETHERITE_HELMET)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness") || text.contains("Knockback");
            });
            tooltip.add(Component.literal("+3.5 Armor").withStyle(buff));
            tooltip.add(Component.literal("+5% Knockback Res").withStyle(buff));
            tooltip.add(Component.literal("+1.875% All Dmg Amp").withStyle(buff));
            tooltip.add(Component.literal("+2.5% All Dmg Amp").withStyle(buff));
            tooltip.add(Component.literal("+5% Crit Dmg").withStyle(buff));
            tooltip.add(Component.literal("+1% Crit Rate").withStyle(buff));
        } else if (item.is(Items.NETHERITE_CHESTPLATE) || item.is(Items.NETHERITE_LEGGINGS)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness") || text.contains("Knockback");
            });
            tooltip.add(Component.literal("+7.5 Armor").withStyle(buff));
            tooltip.add(Component.literal("+5% Knockback Res").withStyle(buff));
            tooltip.add(Component.literal("+1.875% All Dmg Amp").withStyle(buff));
            tooltip.add(Component.literal("+2.5% All Dmg Amp").withStyle(buff));
            tooltip.add(Component.literal("+5% Crit Dmg").withStyle(buff));
            tooltip.add(Component.literal("+1% Crit Rate").withStyle(buff));
            tooltip.add(Component.literal("+2 Max Health").withStyle(buff));
        } else if (item.is(Items.TURTLE_HELMET)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+2.5 Armor").withStyle(buff));
            tooltip.add(Component.literal("+25% Drowning Dmg Res").withStyle(buff));
            tooltip.add(Component.literal("+20 Elemental Mastery").withStyle(buff));
            tooltip.add(Component.literal("+10 Elemental Affinity").withStyle(buff));
            tooltip.add(Component.literal("+1.25% Atk Speed").withStyle(buff));
        } else if (item.is(Items.ELYTRA)) {
            tooltip.removeIf(line -> {
                String text = line.getString();
                return text.contains("Armor") || text.contains("Toughness");
            });
            tooltip.add(Component.literal("+5 Armor").withStyle(buff));
            tooltip.add(Component.literal("+25% Fall Dmg Res").withStyle(buff));
            tooltip.add(Component.literal("+25% Crit Dmg").withStyle(buff));
            tooltip.add(Component.literal("+5% Crit Rate").withStyle(buff));
            tooltip.add(Component.literal("+1 Additional Jump").withStyle(buff));
            tooltip.add(Component.literal("+20% Dash Length").withStyle(buff));
            tooltip.add(Component.literal("+5% Movement Speed").withStyle(buff));
            tooltip.add(Component.literal("Enables Elytra Flight").withStyle(buff));
        } else if (item.is(Items.WOODEN_AXE) || item.is(Items.WOODEN_SHOVEL) || item.is(Items.WOODEN_SWORD) || item.is(Items.WOODEN_HOE) || item.is(Items.WOODEN_PICKAXE)) {
            tooltip.add(Component.literal("Element: Nature").withStyle(Style.EMPTY.withColor(ElementType.NATURE.getColor())));
        } else if (item.is(Items.STONE_AXE) || item.is(Items.STONE_SHOVEL) || item.is(Items.STONE_SWORD) || item.is(Items.STONE_HOE) || item.is(Items.STONE_PICKAXE)) {
            tooltip.add(Component.literal("Element: Earth").withStyle(Style.EMPTY.withColor(ElementType.EARTH.getColor())));
        } else if (item.is(Items.GOLDEN_AXE) || item.is(Items.GOLDEN_SHOVEL) || item.is(Items.GOLDEN_SWORD)  || item.is(Items.GOLDEN_HOE) || item.is(Items.GOLDEN_PICKAXE)) {
            tooltip.add(Component.literal("Element: Fire").withStyle(Style.EMPTY.withColor(ElementType.FIRE.getColor())));
        } else if (item.is(Items.IRON_AXE) || item.is(Items.IRON_SHOVEL) || item.is(Items.IRON_SWORD)   || item.is(Items.IRON_HOE) || item.is(Items.IRON_PICKAXE)) {
            tooltip.add(Component.literal("Element: Physical").withStyle(Style.EMPTY.withColor(ElementType.PHYSICAL.getColor())));
        } else if (item.is(Items.DIAMOND_AXE) || item.is(Items.DIAMOND_SHOVEL) || item.is(Items.DIAMOND_SWORD) || item.is(Items.DIAMOND_HOE) || item.is(Items.DIAMOND_PICKAXE)) {
            tooltip.add(Component.literal("Element: Ice").withStyle(Style.EMPTY.withColor(ElementType.ICE.getColor())));
        } else if (item.is(Items.NETHERITE_AXE) || item.is(Items.NETHERITE_SHOVEL) || item.is(Items.NETHERITE_SWORD) || item.is(Items.NETHERITE_HOE) || item.is(Items.NETHERITE_PICKAXE)) {
            tooltip.add(Component.literal("Element: Lava").withStyle(Style.EMPTY.withColor(ElementType.LAVA.getColor())));
        } else if (item.is(Items.BOW) || item.is(Items.CROSSBOW)) {
            tooltip.add(Component.literal("Element: Wind").withStyle(Style.EMPTY.withColor(ElementType.WIND.getColor())));
        } else if (item.is(Items.TRIDENT)) {
            tooltip.add(Component.literal("Element: Water").withStyle(Style.EMPTY.withColor(ElementType.WATER.getColor())));
        } else if (item.is(Items.MACE)) {
            tooltip.add(Component.literal("Element: Void").withStyle(Style.EMPTY.withColor(ElementType.VOID.getColor())));
        }
    }

    @SubscribeEvent
    public static void renderCustomHUD(CustomizeGuiOverlayEvent event) {
        GuiGraphics gui = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        Player player = mc.player;
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            player.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF-> {
                player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                        System.out.print("");
                        if (mc.gameMode.getPlayerMode() == GameType.ADVENTURE || mc.gameMode.getPlayerMode() == GameType.SURVIVAL) {
                            double maxHP = statsF.getFinalStat(StatType.MAX_HEALTH);
                            double hp = statsI.getIndependentStat(IndependentStatType.HEALTH);
                            double hpPerc = hp / maxHP;
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/health_bar_background.png"), width / 2 - 101, height - 42, 0, 0, 91, 11, 91, 11);
                            hp = (double) ((int) (hp * 100)) / 100;
                            maxHP = (double) ((int) (maxHP * 100)) / 100;
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/health_bar.png"), width / 2 - 89, height - 40, 0, 0, (int) (77 * hpPerc), 7, 77, 7);
                            gui.drawCenteredString(mc.font, hp + "/" + maxHP, width / 2 - 50, height - 40, 0xFFFFFF);
                            double shield = statsI.getIndependentStat(IndependentStatType.SHIELD);
                            if (shield > 0) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/shield_background.png"), width / 2 - 101, height - 41, 0, 0, 9, 9, 9, 9);
                                double shieldPerc = Math.min(maxHP, shield) / maxHP;
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/shield.png"), width / 2 - 92, height - 43, 0, 0, (int) (83 * shieldPerc), 13, 83, 13);
                            }

                            double hunger = player.getFoodData().getFoodLevel();
                            double sat = player.getFoodData().getSaturationLevel();
                            double saturation = Math.min(sat, 20);
                            double armor = (double) ((int) (statsF.getFinalStat(StatType.ARMOR) * 10)) / 10;


                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/hunger_bar_background.png"), width / 2 + 10, height - 42, 0, 0, 91, 11, 91, 11);
                            if (armor != 0) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/armor.png"), width / 2 - 16, height - 58, 0, 0, 32, 18, 32, 18);
                                if (armor >= 1000 || armor == (int) armor) {
                                    int armor1 = (int) armor;
                                    gui.drawCenteredString(mc.font, "" + armor1, width / 2, height - 54, 0xF9F9F9);
                                } else {
                                    gui.drawCenteredString(mc.font, "" + armor, width / 2, height - 54, 0xF9F9F9);
                                }
                            }
                            if (hunger == 20) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/hunger_bar.png"), width / 2 + 12, height - 40, 0, 0, 77, 7, 77, 7);
                            } else {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/hunger_bar.png"), (int) ((double) width / 2 + 90 - (hunger / 20 * 77)), height - 40, 0, 0, (int) (77 * hunger / 20), 7, 77, 7);
                            }
                            gui.drawCenteredString(mc.font, (int) hunger + "/20", width / 2 + 50, height - 40, 0xFFFFFF);
                            if (saturation > 0) {

                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation_background.png"), width / 2 + 92, height - 41, 0, 0, 9, 9, 9, 9);
                                if (saturation != 20) {
                                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation.png"), (int) ((double) width / 2 + 90 - (saturation / 20 * 83)), height - 43, height - 42, 0, 0, (int) (83 * saturation / 20), 13, 83, 13);
                                } else {
                                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation.png"), (int) ((double) width / 2 + 9), height - 43, height - 43, 0, 0, 83, 13, 83, 13);
                                }
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation_start.png"), width / 2 + 88, height - 43, 0, 0, 3, 13, 3, 13);
                                if (saturation == 20) {
                                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation_end.png"), width / 2 + 9, height - 43, 0, 0, 3, 13, 3, 13);
                                }
                            }


                            int oxygen = max(0, player.getAirSupply());
                            int maxO2 = player.getMaxAirSupply();
                            if (oxygen < maxO2) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/air_bar_background.png"), width / 2 + 20, height - 55, 0, 0, 91, 11, 91, 11);
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/air_bar.png"), (int) ((double) width / 2 + 100 - ((double) oxygen / maxO2 * 77)), height - 53, 0, 0, (int) (77 * oxygen / maxO2), 7, 77, 7);
                                gui.drawCenteredString(mc.font, oxygen / 20 + "/" + maxO2 / 20, width / 2 + 60, height - 53, 0xFFFFFF);
                            }

                            double bondOfLife = statsI.getIndependentStat(IndependentStatType.BOND_OF_LIFE);
                            int bondOfLifePerc = (int) (bondOfLife / maxHP * 100);
                            if (bondOfLifePerc > 0) {
                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar_background.png"), width / 2 - 110, height - 55, 0, 0, 91, 11, 91, 11);
                                if (bondOfLifePerc <= 100) {
                                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar.png"), width / 2 - 98, height - 53, 0, 0, (int) (77 * bondOfLifePerc / 100), 7, 77, 7);
                                } else if (bondOfLifePerc <= 200) {
                                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar.png"), width / 2 - 98, height - 53, 0, 0, 77, 7, 77, 7);
                                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar_2.png"), width / 2 - 98, height - 53, 0, 0, (int) (77 * (bondOfLifePerc / 100 - 1)), 7, 77, 7);
                                }
                                gui.drawCenteredString(mc.font, bondOfLifePerc + "%", width / 2 - 60, height - 53, 0xFFFFFF);
                            }
                        }

                    ItemStack weapon = statsI.getWeapon();
                    ElementType element = statsI.getElementType();

                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/dash_display.png"), width/2 + 94, height - 40, 0 ,0, 20, 20);

                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/elemental_icons.png"), 0, height - 48, 0, 48 * element.ordinal(), 48, 48, 48, 720);
                    gui.renderItem(weapon, 16, height - 32);

                    double mountMaxHealth = statsP.getMountMaxHealth();
                    if (mountMaxHealth > 0) {
                            double mountHealth = statsP.getMountHealth();

                            double mountHealthPerc = Math.min(1, mountHealth/mountMaxHealth);

                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/mount_health_bar_background.png"), width/2 + 92, height - 12, 0, 0, 50, 11, 50, 11);
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/mount_health_bar.png"), width/2 + 94, height - 10, 0 , 0, (int) (36 * mountHealthPerc), 7);
                            gui.drawCenteredString(mc.font,((int) (Math.min(mountHealth, mountMaxHealth) * 100)) / 100 + "/" + ((int) (mountMaxHealth * 100)) / 100, width/2 + 112, height - 10, 0xFFFFFF);
                    } else {

                        int dashes = (int) statsI.getIndependentStat(IndependentStatType.DASHES_AVAILABLE);
                        int maxDashes = (int) statsF.getFinalStat(StatType.MAX_DASHES);
                        double dashCooldownPerc = statsI.getIndependentStat(IndependentStatType.DASH_TIME) / statsF.getFinalStat(StatType.DASH_COOLDOWN);

                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/dash_display.png"), width/2 + 94, height - 20, 0 ,0, 18, 18, 18, 18);

                        if (dashCooldownPerc < 1) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/dash_bar.png"), width / 2 + 94, height - 20, 0, 0, 18, (int) (18 * dashCooldownPerc), 18, 18);
                        }

                        gui.drawCenteredString(mc.font, dashes + "/" + maxDashes, width/2 + 104, height - 17, 0xFFFFFF);

                        int addJumps = (int) statsI.getIndependentStat(IndependentStatType.ADDITIONAL_JUMPS_AVAILABLE);
                        int maxAddJumps = (int) statsF.getFinalStat(StatType.ADDITIONAL_JUMP);

                        if (maxAddJumps > 0) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/additional_jump_display.png"), width/2 + 114, height - 20, 0 ,0, 20, 20, 20, 20);
                            gui.drawCenteredString(mc.font, addJumps + "/" + maxAddJumps, width/2 + 124, height - 17, 0xFFFFFF);
                        }}

                    int deathDefianceChances = (int) statsI.getIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE);
                    if (deathDefianceChances > 0) {
                        gui.drawCenteredString(mc.font, deathDefianceChances + "", width / 2 - 106, height - 40, 0xe68600);
                    }

                    double maxMana = statsF.getFinalStat(StatType.MAX_MANA);
                    if (maxMana > 0) {
                        double mana = statsI.getIndependentStat(IndependentStatType.MANA);
                        double manaPerc = mana / maxMana;
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/mana_icon_background.png"), width - 48, height - 48, 0, 0, 48, 48, 48, 48);
                        RenderSystem.setShaderColor(0, 0.33F,1,1F);
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/circle.png"), (int) (width - 24 - 21 * manaPerc), (int) (height - 26 - 21 * manaPerc), (float) 0, (float) 0, (int) (42 * manaPerc), (int) (42*manaPerc), (int) (42*manaPerc), (int) (42*manaPerc));
                        RenderSystem.setShaderColor(1,1,1,1);
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/mana_icon_mask.png"), width - 48, height - 48, 0, 0, 48, 48, 48, 48);
                        gui.drawCenteredString(mc.font, "" + (int) mana, width -24, height-30, 0xFFFFFF);
                        gui. drawCenteredString(mc.font, "" + (int) maxMana, width - 24, height - 8, 0xFFFFFF);
                    }

                    int effectAmount = statsI.getActiveEffectList().size();
                    int i = 0;

                        System.out.print("");

                        if (effectAmount <= 24) {
                            for (Map.Entry<MobEffect, StatusEffectInstanceEntry> entry : statsI.getActiveEffectList().entrySet()) {
                                if (i < effectAmount) {
                                    Optional<ResourceKey<MobEffect>> optional = BuiltInRegistries.MOB_EFFECT.getResourceKey(entry.getKey());
                                    if (optional.isPresent()) {
                                        ResourceKey<MobEffect> resourceKey = optional.get();
                                        Holder<MobEffect> holder = optional.get().getOrThrow(player);
                                        if (!holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                                            int ix = (int) ((i/6) + 1);
                                            int iy = i - ((ix - 1) * 6) + 1;

                                            System.out.print("");


                                            if (holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_neutral_effect_icon_background.png"), width - 23 * ix, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                            } else if (holder.is(ModTags.SPECIAL_BUFFS)) {
                                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_buff_icon_background.png"), width - 23 * ix, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                            } else if (holder.is(ModTags.SPECIAL_DEBUFFS)) {
                                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_debuff_icon_background.png"), width - 23 * ix,  23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                            } else {
                                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/effect_icon_background.png"), width - 23 * ix, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                            }

                                            ResourceLocation effectImage = ModGeneralUtils.getEffectIcon(entry.getKey());

                                            if (resourceKey.location().getNamespace().equals("minecraft")) {
                                                gui.blit(effectImage, width - 23 * ix + 2, 23 * (iy - 1) + 1 + 2, 0, 0, 18, 18, 18, 18);
                                            } else {
                                                gui.blit(effectImage, width - 23 * ix + 3, 23 * (iy - 1) + 1 + 3, 0, 0, 16, 16, 16, 16);
                                            }

                                            if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.SPECIAL_DEBUFFS) || holder.is(ModTags.CC_DEBUFFS)) {
                                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/debuff_icon_background.png"), width - 23 * ix, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                            } else if (holder.is(ModTags.BUFFS) || holder.is(ModTags.SPECIAL_BUFFS)) {
                                                gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/buff_icon_background.png"), width - 23 * ix, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                            }

                                            i = 1 + i;
                                        }
                                    }
                                }
                        }
                    } else if (effectAmount <= 42) {
                        for (Map.Entry<MobEffect, StatusEffectInstanceEntry> entry : statsI.getActiveEffectList().entrySet()) {
                            if (i < effectAmount) {
                                Optional<ResourceKey<MobEffect>> optional = BuiltInRegistries.MOB_EFFECT.getResourceKey(entry.getKey());
                                if (optional.isPresent()) {
                                    ResourceKey<MobEffect> resourceKey = optional.get();
                                    Holder<MobEffect> holder = optional.get().getOrThrow(player);
                                    if (!holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                                        int ix = (int) ((i/6) + 1);
                                        int iy = i - ((ix - 1) * 6) + 1;

                                        System.out.print("");

                                        if (holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_neutral_effect_icon_background.png"), width - 12 * ix - 11, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else if (holder.is(ModTags.SPECIAL_BUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_buff_icon_background.png"), width - 12 * ix - 11, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else if (holder.is(ModTags.SPECIAL_DEBUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_debuff_icon_background.png"), width - 12 * ix - 11, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/effect_icon_background.png"), width - 12 * ix - 11, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        }

                                        ResourceLocation effectImage = ModGeneralUtils.getEffectIcon(entry.getKey());

                                        if (resourceKey.location().getNamespace().equals("minecraft")) {
                                            gui.blit(effectImage, width - 12 * ix + 2 - 11, 23 * (iy - 1) + 1 + 2, 0, 0, 18, 18, 18, 18);
                                        } else {
                                            gui.blit(effectImage, width - 12 * ix + 3 - 11, 23 * (iy - 1) + 1 + 3, 0, 0, 16, 16, 16, 16);
                                        }

                                        if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.SPECIAL_DEBUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/debuff_icon_background.png"), width - 12 * ix - 11, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else if (holder.is(ModTags.BUFFS) || holder.is(ModTags.SPECIAL_BUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/buff_icon_background.png"), width - 12 * ix - 11, 23 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        }

                                        i = 1 + i;
                                    }
                                }
                            }
                        }
                    } else {
                        effectAmount = Math.min(77, effectAmount);
                        for (Map.Entry<MobEffect, StatusEffectInstanceEntry> entry : statsI.getActiveEffectList().entrySet()) {
                            if (i < effectAmount) {
                                Optional<ResourceKey<MobEffect>> optional = BuiltInRegistries.MOB_EFFECT.getResourceKey(entry.getKey());
                                if (optional.isPresent()) {
                                    ResourceKey<MobEffect> resourceKey = optional.get();
                                    Holder<MobEffect> holder = optional.get().getOrThrow(player);
                                    if (!holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                                        int ix = (int) ((i/6) + 1);
                                        int iy = i - ((ix - 1) * 6) + 1;

                                        System.out.print("");

                                        if (holder.is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_neutral_effect_icon_background.png"), width - 12 * ix - 12, 12 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else if (holder.is(ModTags.SPECIAL_BUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_buff_icon_background.png"), width - 12 * ix - 12, 12 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else if (holder.is(ModTags.SPECIAL_DEBUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/special_debuff_icon_background.png"), width - 12 * ix -12, 12 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/effect_icon_background.png"), width - 12 * ix - 12, 12 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        }

                                        ResourceLocation effectImage = ModGeneralUtils.getEffectIcon(entry.getKey());

                                        if (resourceKey.location().getNamespace().equals("minecraft")) {
                                            gui.blit(effectImage, width - 12 * ix + 2 - 12, 12 * (iy - 1) + 1 + 2, 0, 0, 18, 18, 18, 18);
                                        } else {
                                            gui.blit(effectImage, width - 12 * ix + 3 - 12, 12 * (iy - 1) + 1 + 3, 0, 0, 16, 16, 16, 16);
                                        }

                                        if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.SPECIAL_DEBUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/debuff_icon_background.png"), width - 12 * ix - 12, 12 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        } else if (holder.is(ModTags.BUFFS) || holder.is(ModTags.SPECIAL_BUFFS)) {
                                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/mob_effect/buff_icon_background.png"), width - 12 * ix - 12, 12 * (iy - 1) + 1, 0, 0, 22, 22, 22, 22);
                                        }

                                        i = 1 + i;
                                    }
                                }
                            }
                        }

                    }





                    });
                });
            });
    }

    @SubscribeEvent
    public static void interceptVanillaShielding (ShieldBlockEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntitySave(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide && entity instanceof LivingEntity living) {
            living.addTag("capsToDeserialize");
            living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IIndependentStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.BASE_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IBaseStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.ADD_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IAddStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.MULT_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IMultStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IFinalStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IDirtyStats", cap.serializeNBT());
            });
            if (entity.getTags().contains("indicator")) {
                living.getCapability(ModCapabilities.INDICATOR_STATS).ifPresent(cap -> {
                    living.getPersistentData().put("IIndicatorStats", cap.serializeNBT());
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin (EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living && !entity.level().isClientSide && entity.getTags().contains("capsToDeserialize")) {

            living.removeTag("capsToDeserialize");

            if (living.getPersistentData().contains("IIndependentStats")) {
                living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IIndependentStats"), living);
                });
            }
            if (living.getPersistentData().contains("IBaseStats") && living.getPersistentData().getCompound("IBaseStats").contains("BaseStats")) {
                living.getCapability(ModCapabilities.BASE_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IBaseStats"));
                });
            }
            if (living.getPersistentData().contains("IAddStats") && living.getPersistentData().getCompound("IAddStats").contains("AddeStats")) {
                living.getCapability(ModCapabilities.ADD_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IAddStats"));
                });
            }
            if (living.getPersistentData().contains("IMultStats") && living.getPersistentData().getCompound("IMultStats").contains("MultStats")) {
                living.getCapability(ModCapabilities.MULT_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IMultStats"));
                });
            }
            if (living.getPersistentData().contains("IFinalStats") && living.getPersistentData().getCompound("IFinalStats").contains("FinalStats")) {
                living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IFinalStats"));
                });
            }
            if (living.getPersistentData().contains("IDirtyStats") && living.getPersistentData().getCompound("IDirtyStats").contains("DirtyStats")) {
                living.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IDirtyStats"));
                });
            }
            if (living.getTags().contains("indicator") && living.getPersistentData().contains("IIndicatorStats")) {
                living.getCapability(ModCapabilities.INDICATOR_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IIndicatorStats"));
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut (PlayerEvent.SaveToFile event) {
        File file = event.getPlayerFile("");
        Player living = event.getEntity();
        try {
            Path pPath = Path.of(file.getPath());
            CompoundTag rootTag = file.exists() ? NbtIo.readCompressed(pPath, NbtAccounter.unlimitedHeap()) : new CompoundTag();
            living.addTag("capsToDeserialize");
            living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(cap -> {
                rootTag.put("IIndependentStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.BASE_STATS).ifPresent(cap -> {
                rootTag.put("IBaseStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.ADD_STATS).ifPresent(cap -> {
                rootTag.put("IAddStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.MULT_STATS).ifPresent(cap -> {
                rootTag.put("IMultStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(cap -> {
                rootTag.put("IFinalStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(cap -> {
                rootTag.put("IDirtyStats", cap.serializeNBT());
            });
            living.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(cap -> {
                rootTag.put("IPlayerStats", cap.serializeNBT());
            });

            NbtIo.writeCompressed(rootTag, pPath);

        } catch (IOException e) {
            System.out.println("Failed to save player stats");
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoad (PlayerEvent.LoadFromFile event) {
        Player living = event.getEntity();
        if (living.level().isClientSide) return;
        File file = event.getPlayerFile("");
        if (!file.exists()) return;

        try {
            CompoundTag rootTag = NbtIo.readCompressed(Path.of(file.getPath()), NbtAccounter.unlimitedHeap());

            living.removeTag("capsToDeserialize");

            if (rootTag.contains("IIndependentStats")) {
                living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(rootTag.getCompound("IIndependentStats"), living);
                });
            }
            if (rootTag.contains("IBaseStats") && rootTag.getCompound("IBaseStats").contains("BaseStats")) {
                living.getCapability(ModCapabilities.BASE_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(rootTag.getCompound("IBaseStats"));
                });
            }
            if (rootTag.contains("IAddStats") && rootTag.getCompound("IAddStats").contains("AddeStats")) {
                living.getCapability(ModCapabilities.ADD_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(rootTag.getCompound("IAddStats"));
                });
            }
            if (rootTag.contains("IMultStats") && rootTag.getCompound("IMultStats").contains("MultStats")) {
                living.getCapability(ModCapabilities.MULT_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(rootTag.getCompound("IMultStats"));
                });
            }
            if (rootTag.contains("IFinalStats") && rootTag.getCompound("IFinalStats").contains("FinalStats")) {
                living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(rootTag.getCompound("IFinalStats"));
                });
            }
            if (rootTag.contains("IDirtyStats") && rootTag.getCompound("IDirtyStats").contains("DirtyStats")) {
                living.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(rootTag.getCompound("IDirtyStats"));
                });
            }
            if (rootTag.contains("IPlayerStats")) {
                living.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(rootTag.getCompound("IPlayerStats"), living);
                });
            }

        } catch (IOException e) {
            System.out.println("Failed to read player stats");
        }
    }

    @SubscribeEvent
    public static void registerCustomCommands (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ModCommands.register(dispatcher);
    }

    @SubscribeEvent
    public static void regenerateMana (LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !(entity instanceof Player player)) return;

        LazyOptional<IIndependentStats> statsIL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);
        LazyOptional<IFinalStats> statsFL = player.getCapability(ModCapabilities.FINAL_STATS);

        if (!statsFL.isPresent() || !statsIL.isPresent()) return;

        IFinalStats statsF = statsFL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats statsI = statsIL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        double mana = statsI.getIndependentStat(IndependentStatType.MANA);
        double maxMana = statsF.getFinalStat(StatType.MAX_MANA);

        if (maxMana > 0 && mana < maxMana) {
            double manaRegen = statsF.getFinalStat(StatType.MANA_REGENERATION)/20;
            double newMana = Math.min (maxMana, manaRegen + mana);
            statsI.setIndependentStat(IndependentStatType.MANA, newMana);
        }
    }

    @SubscribeEvent
    public static void manaRestoration (ManaRestorationEvent event) {
        LivingEntity entity = event.getTarget();
        Level level = entity.level();
        if (level.isClientSide || !(entity instanceof Player player)) return;

        LazyOptional<IIndependentStats> statsIL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);
        LazyOptional<IFinalStats> statsFL = player.getCapability(ModCapabilities.FINAL_STATS);

        if (!statsFL.isPresent() || !statsIL.isPresent()) return;

        IFinalStats statsF = statsFL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats statsI = statsIL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        double mana = statsI.getIndependentStat(IndependentStatType.MANA);
        double maxMana = statsF.getFinalStat(StatType.MAX_MANA);

        if (maxMana > 0 && mana < maxMana) {
            double amountRestored = event.getRestoreAmount();
            double newMana = Math.min (maxMana, amountRestored + mana);
            double showMana = newMana - mana;
            statsI.setIndependentStat(IndependentStatType.MANA, newMana);

            entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
                Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                if (!stats.getActiveEffectList().isEmpty()) {
                    for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                        if (effectMap.containsKey(copiedEffect)) {
                            StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                            if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                                ((ICustomStatusEffect) copiedEffect).onManaRestore(entity, amountRestored);
                            }
                        }
                    }}
            });

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
            x = x + Math.random() - 0.5;
            y = y + 1 + Math.random();
            z = z + Math.random() - 0.5;
            showMana = (int) (showMana * 100);
            String text = "+" + showMana / 100;
            armorStand.getPersistentData().putString("adventurer_fantasy:name", text);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:x", x);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:y", y);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:z", z);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dx", 0);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dy", 0.03);
            armorStand.getPersistentData().putDouble("adventurer_fantasy:Dz", 0);
            armorStand.getPersistentData().putInt("adventurer_fantasy:lifetime", 20);
            armorStand.getPersistentData().putInt("adventurer_fantasy:color", 0x162385);
            armorStand.getPersistentData().putBoolean("adventurer_fantasy:isBold", false);
            armorStand.addTag("indicator");
            armorStand.addTag("indicatorToInitialise");
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Marker", true);
            armorStand.load(tag);
            armorStand.setPos(x, y, z);
            armorStand.isMarker();
            armorStand.setInvisible(true);
            armorStand.setNoGravity(true);
            armorStand.setNoBasePlate(true);
            armorStand.setShowArms(false);
            level.addFreshEntity(armorStand);
        }
    }

    @SubscribeEvent
    public static void manaConsumption (ManaConsumptionEvent event) {
        LivingEntity entity = event.getTarget();
        Level level = entity.level();
        if (level.isClientSide || !(entity instanceof Player player)) return;

        LazyOptional<IIndependentStats> statsIL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);

        if (!statsIL.isPresent()) return;

        IIndependentStats statsI = statsIL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        double mana = statsI.getIndependentStat(IndependentStatType.MANA);
        double amount = event.getAmount();
        double newMana = max(0, mana - amount);

        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
            if (!stats.getActiveEffectList().isEmpty()) {
                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                    if (effectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                            ((ICustomStatusEffect) copiedEffect).onManaConsume(entity, mana - newMana);
                        }
                    }
                }}
        });

        statsI.setIndependentStat(IndependentStatType.MANA, newMana);
    }

    @SubscribeEvent
    public static void onXPGain(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null || player.level().isClientSide) return;
        player.getCapability(ModCapabilities.FINAL_STATS).ifPresent(stats -> {
            double xpMult = stats.getFinalStat(StatType.XP_GAIN);
            int xp = event.getDroppedExperience();
            event.setDroppedExperience((int) (xpMult * xp));
        });
    }

    @SubscribeEvent
    public static void onEffectTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {

            if (!stats.getActiveEffectList().isEmpty()) {
                Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                    if (effectMap.containsKey(copiedEffect) && copiedEffect != null) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        StatusEffectInstanceEntry value = new StatusEffectInstanceEntry(effectInstance.duration(), effectInstance.amplifier(), effectInstance.stacks(), effectInstance.maxStacks(), effectInstance.applier(), effectInstance.data(), true);
                        if (effectInstance.duration() != Integer.MAX_VALUE) {
                            value = new StatusEffectInstanceEntry(effectInstance.duration() - 1, effectInstance.amplifier(), effectInstance.stacks(), effectInstance.maxStacks(), effectInstance.applier(), effectInstance.data(), true);
                        }
                        if (effectInstance.isInitialised()) {
                            effectMap.replace(copiedEffect, value);
                            if (copiedEffect instanceof ICustomStatusEffect) {
                                ((ICustomStatusEffect) copiedEffect).onTick(entity);
                            }
                        } else {
                            effectMap.replace(copiedEffect, value);
                            if (copiedEffect instanceof ICustomStatusEffect) {
                                ((ICustomStatusEffect) copiedEffect).onInitialisation(entity);
                                ((ICustomStatusEffect) copiedEffect).onTick(entity);
                            }

                            if (!(copiedEffect instanceof ICustomStatusEffect)) {
                                BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(copiedEffect)).ifPresent(effectHolder -> entity.addEffect(new MobEffectInstance(effectHolder, Integer.MAX_VALUE, (int) (effectInstance.amplifier()))));
                            }                        }
                        if (effectInstance.duration() <= 0) {
                            stats.removeActiveEffect(copiedEffect, entity);
                        }
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onEffectAttack (AttackCustomEvent event) {
        LivingEntity entity = event.getAttacker();
        if (entity == null || entity.level().isClientSide) return;

        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
            if (!stats.getActiveEffectList().isEmpty()) {
            for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                if (effectMap.containsKey(copiedEffect)) {
                    StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                    if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                        ((ICustomStatusEffect) copiedEffect).onAttack(entity);
                    }
                }
            }}
        });
    }

    @SubscribeEvent
    public static void modifyBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            player.getCapability(ModCapabilities.FINAL_STATS).ifPresent(stats -> {
                event.setNewSpeed((float) (event.getOriginalSpeed() * stats.getFinalStat(StatType.MINING_SPEED)));
            });
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity == null || entity.level().isClientSide) return;

        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
            if (!stats.getActiveEffectList().isEmpty()) {
                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                    if (effectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                            ((ICustomStatusEffect) copiedEffect).onJump(entity);
                        }
                    }
                }}
        });
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity == null || entity.level().isClientSide) return;

        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(stats -> {
            Map<MobEffect, StatusEffectInstanceEntry> effectMap = stats.getActiveEffectList();
            if (!stats.getActiveEffectList().isEmpty()) {
                for (MobEffect copiedEffect : (new HashMap<>(stats.getActiveEffectList())).keySet()) {
                    if (effectMap.containsKey(copiedEffect)) {
                        StatusEffectInstanceEntry effectInstance = effectMap.get(copiedEffect);
                        if (copiedEffect instanceof ICustomStatusEffect && effectInstance.isInitialised()) {
                            ((ICustomStatusEffect) copiedEffect).onFall(entity, event.getDistance());
                        }
                    }
                }}
        });
    }

    @SubscribeEvent
    public static void onTotemWielding(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
            if (statsI.getIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE) < 0) {
                statsI.setIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE, 0);
            }
            if (entity.getItemBySlot(EquipmentSlot.OFFHAND).getItem() == Items.TOTEM_OF_UNDYING || entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() == Items.TOTEM_OF_UNDYING) {
                if (!statsI.getActiveEffectList().containsKey(ModEffects.TOTEM_WARD_EFFECT.get())) {
                    statsI.addActiveEffectEntry(ModEffects.TOTEM_WARD_EFFECT.get(), new StatusEffectInstanceEntry(Integer.MAX_VALUE, 1, 0, 0, entity.getUUID(), new CompoundTag(), false), entity);
                    statsI.setIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE, statsI.getIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE) + 1);
                    entity.getCapability(ModCapabilities.ADD_STATS).ifPresent(statsA -> {
                        entity.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(statsD -> {
                            statsA.setAddStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE, statsA.getAddStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE) + 0.4);
                            statsD.setDirtyStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE, true);
                        });
                    });
                }

            } else if (statsI.getActiveEffectList().containsKey(ModEffects.TOTEM_WARD_EFFECT.get()) && !statsI.getActiveEffectList().containsKey(ModEffects.BLESSING_OF_UNDYING_EFFECT.get())) {
                statsI.removeActiveEffect(ModEffects.TOTEM_WARD_EFFECT.get(), entity);
                statsI.setIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE, statsI.getIndependentStat(IndependentStatType.DEATH_DEFIANCE_CHANCES_AVAILABLE) - 1);
                entity.getCapability(ModCapabilities.ADD_STATS).ifPresent(statsA -> {
                    entity.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(statsD -> {
                        statsA.setAddStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE, statsA.getAddStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE) - 0.4);
                        statsD.setDirtyStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE, true);
                    });
                });
            } else if (statsI.getActiveEffectList().containsKey(ModEffects.TOTEM_WARD_EFFECT.get()) && statsI.getActiveEffectList().containsKey(ModEffects.BLESSING_OF_UNDYING_EFFECT.get())) {
                statsI.removeActiveEffect(ModEffects.TOTEM_WARD_EFFECT.get(), entity);
                entity.getCapability(ModCapabilities.ADD_STATS).ifPresent(statsA -> {
                    entity.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(statsD -> {
                        statsA.setAddStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE, statsA.getAddStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE) - 0.4);
                        statsD.setDirtyStat(StatType.HEALTH_RESTORATION_ON_DEATH_DEFIANCE, true);
                    });
                });
            }
        });
    }

    @SubscribeEvent
    public static void onSleep(SleepFinishedTimeEvent event) {
        if (event.getLevel().isClientSide()) return;
        for (Player player : event.getLevel().players().stream().toList()) {
            if (player.isSleepingLongEnough()) {
                player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    statsI.setIndependentStat(IndependentStatType.BOND_OF_LIFE, 0);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onMilkDrinking(LivingEntityUseItemEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof Player player && event.getItem().getItem() == Items.MILK_BUCKET) {

            player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                        player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                            int time = statsP.getMilkDrinkingTime();
                            if (time == 0) {
                                Map<MobEffect, StatusEffectInstanceEntry> effectMap = statsI.getActiveEffectList();
                                if (!statsI.getActiveEffectList().isEmpty()) {
                                    for (MobEffect copiedEffect : (new HashMap<>(statsI.getActiveEffectList())).keySet()) {
                                        if (effectMap.containsKey(copiedEffect)) {
                                            ResourceKey<MobEffect> key = BuiltInRegistries.MOB_EFFECT.getResourceKey(copiedEffect).orElseThrow();
                                            if (key.getOrThrow(player).is(ModTags.DEBUFFS) && !(key.getOrThrow(player).is(ModTags.UNDISPELLABLE_EFFECTS))) {
                                                statsI.removeActiveEffectWithoutConsequences(copiedEffect, player);
                                                if (!player.isCreative()) {
                                                    player.getInventory().removeItem(event.getItem());
                                                    player.addItem(new ItemStack(Items.BUCKET));
                                                }

                                                statsP.setMilkDrinkingTime(-1);
                                                event.setCanceled(true);
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else if (time < 0) {
                                statsP.setMilkDrinkingTime(30);
                            }
                        });
                    }
                );
        }
    }

    @SubscribeEvent
    public static void onInterruptItemUsage (LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof Player player && event.getItem().getItem() == Items.MILK_BUCKET) {

            player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                statsP.setMilkDrinkingTime(-1);
            });
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.getEntityMounting() instanceof Player player && event.getEntityBeingMounted() instanceof LivingEntity living && !player.level().isClientSide) {
            player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> {
                living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF -> {
                        if (event.isMounting()) {
                            statsP.setMount(living);
                            statsP.setMountHealth(statsI.getIndependentStat(IndependentStatType.HEALTH));
                            statsP.setMountMaxHealth(statsF.getFinalStat(StatType.MAX_HEALTH));
                        }
                        if (event.isDismounting()) {
                            statsP.setMount(null);
                            statsP.setMountHealth(0);
                            statsP.setMountMaxHealth(0);
                        }
                    });
                });
            });
        }
    }

    @SubscribeEvent
    public static void onEffectReception (MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (entity.level().isClientSide) return;

        if (effectInstance.getEffect().is(ModTags.STACKABLE_EFFECT) || effectInstance.getEffect().is(ModTags.UNDISPELLABLE_EFFECTS) || effectInstance.getEffect().is(ModTags.NEUTRAL_EFFECTS) || effectInstance.getEffect().is(ModTags.DEBUFFS) || effectInstance.getEffect().is(ModTags.CC_DEBUFFS) || effectInstance.getEffect().is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI) || effectInstance.getEffect().is(ModTags.BUFFS) || effectInstance.getEffect().is(ModTags.DOT_EFFECTS) || effectInstance.getEffect().is(ModTags.SPECIAL_BUFFS) || effectInstance.getEffect().is(ModTags.SPECIAL_DEBUFFS) || effectInstance.getEffect().is(ModTags.SPECIAL_NEUTRAL_EFFECT)) {
            entity.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                if (!statsI.getActiveEffectList().containsKey(effectInstance.getEffect().get())) {
                    entity.removeEffect(effectInstance.getEffect());
                }
            });
        } else {
            entity.removeEffect(effectInstance.getEffect());
        }
    }
}