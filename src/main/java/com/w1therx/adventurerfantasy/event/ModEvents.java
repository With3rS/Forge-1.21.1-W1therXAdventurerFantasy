package com.w1therx.adventurerfantasy.event;

import com.mojang.brigadier.CommandDispatcher;
import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.commands.CustomCommands;
import com.w1therx.adventurerfantasy.common.enums.*;
import com.w1therx.adventurerfantasy.effect.ModEffects;
import com.w1therx.adventurerfantasy.effect.combat.StatusEffectEntry;
import com.w1therx.adventurerfantasy.event.custom.*;
import com.w1therx.adventurerfantasy.network.ModNetworking;
import com.w1therx.adventurerfantasy.network.packet.AdditionalJumpInputReceiver;
import com.w1therx.adventurerfantasy.network.packet.ClientStatReceiver;
import com.w1therx.adventurerfantasy.network.packet.DashInputReceiver;
import com.w1therx.adventurerfantasy.network.packet.InteractInputReceiver;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
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
// AI overriding, teams, <healing>/<shielding>/<outgoing effects>/<natural regeneration calculators>, <capability setup>, <stat calculation logic>, ability for items/<gear>/effects to modify stats, GUIs


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

        System.out.println("[DEBUG] Entity targeted. ");

        LazyOptional<IFinalStats> targetStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        LazyOptional<IIndependentStats> independentTargetStatsL = target.getCapability(ModCapabilities.INDEPENDENT_STATS);

        if (!independentTargetStatsL.isPresent()) {
            System.out.println("[DEBUG] Couldn't find target independent stats. ");
            if (!targetStatsL.isPresent()) {
                System.out.println("[DEBUG] Couldn't find target final stats. ");
                return;
            }
            return;
        }

        if (!targetStatsL.isPresent()) {
            System.out.print("[DEBUG] Couldn't find target final stats. ");
            return;
        }

        System.out.print("[DEBUG] Target's stats found. ");

        IFinalStats targetStats = targetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        IIndependentStats independentTargetStats = independentTargetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        event.setAmount(0);

        int invulnerableTime = (int) independentTargetStats.getIndependentStat(IndependentStatType.INVULNERABLE_TIME);
        if (invulnerableTime >= 1) {
            return;
        }

        if (!(attacker instanceof LivingEntity)) {

            source = event.getSource();
            Holder<DamageType> type = source.typeHolder();

            System.out.println("[DEBUG] " + target.getName().getString() + " took damage from " + type);

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
            } else if (type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FALL) || type == damageTypeRegistry.getHolderOrThrow(DamageTypes.FLY_INTO_WALL)) {
                elementType = ElementType.WIND;
                elementalRes = targetStats.getFinalStat(StatType.WIND_DMG_RES);
                baseDmg = baseDmg * (1 - targetStats.getFinalStat(StatType.FALL_DMG_RES));
            } else {
                elementType = ElementType.PHYSICAL;
                elementalRes = targetStats.getFinalStat(StatType.PHYSICAL_DMG_RES);
            }
            DmgInstanceType dmgType = DmgInstanceType.DIRECT;
            double allRes = targetStats.getFinalStat(StatType.ALL_DMG_RES);
            double armor = targetStats.getFinalStat(StatType.ARMOR);
            if (elementType == ElementType.TRUE) {
                damage = baseDmg;
            } else {
                damage = (baseDmg * (1 - allRes) * (1 - elementalRes) - armor / 4);
            }
            damage = max(damage, 0.0);
            triggerIFrames = true;
        } else {

            if (attacker instanceof Player) {
                System.out.println("[DEBUG] Player attacked " + target.getName().getString());
            } else {
                System.out.println("[DEBUG]" + attacker.getName().getString() + " attacked " + target.getName().getString());
            }

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
                target.setDeltaMovement(direction);
            }

            List<StatusEffectEntry> EffectList = independentAttackerStats.getEffectList();
            double CCRes = targetStats.getFinalStat(StatType.CC_RES);
            double effectChance;
            for (StatusEffectEntry entry : EffectList) {
                if (!(entry.effect() == null)) {
                    double effectAmp = entry.amplifier();
                    if (!entry.effect().get().isBeneficial()) {
                        effectChance = entry.baseChance() + attackerStats.getFinalStat(StatType.EFFECT_HIT_RATE) - targetStats.getFinalStat(StatType.EFFECT_RES);
                        if (entry.effect().get() instanceof ICCDebuff) {
                            effectChance -= CCRes;
                            effectAmp *= attackerStats.getFinalStat(StatType.CC_DEBUFF_EFFICIENCY);
                        }
                        if (!(entry.effect().get() instanceof IDoTDebuff)) {
                            effectAmp *= attackerStats.getFinalStat(StatType.NON_DAMAGING_DEBUFF_EFFICIENCY);
                        }
                    } else {
                        effectChance = entry.baseChance() + attackerStats.getFinalStat(StatType.EFFECT_HIT_RATE);
                    }
                    double roll = Math.random();

                    if (roll <= effectChance) {
                        if (entry.effect().getKey() != null) {
                            Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(entry.effect().getKey());
                            target.addEffect(new MobEffectInstance(effectHolder, (int) (entry.duration() * attackerStats.getFinalStat(StatType.EFFECT_DURATION)), (int) (effectAmp)));
                        }
                    }
                }
            }
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
            triggerIFrames = false;
            System.out.println("[DEBUG] Triggered iFrames for " + target.getName().getString());
        }


        if (elementType == ElementType.FIRE && ElementType.WATER.getEffect().getHolder().isPresent() && target.hasEffect(ElementType.WATER.getEffect().getHolder().get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            target.removeEffect(ElementType.WATER.getEffect().getHolder().get());
            if (!(attacker instanceof LivingEntity)) {
                damage = damage * 1.15;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);
            } else {
                damage = damage * (1.15 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));
            }

            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
            ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
            armorStand.addTag("indicator");
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 6 + 6), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.45);
            }
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
        } else if (elementType == ElementType.WATER && ElementType.FIRE.getEffect().getHolder().isPresent() && target.hasEffect(ElementType.FIRE.getEffect().getHolder().get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            target.removeEffect(ElementType.FIRE.getEffect().getHolder().get());
            if (!(attacker instanceof LivingEntity)) {
                damage = damage * 1.2;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);
            } else {
                damage = damage * (1.2 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));
            }

            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 6 + 6), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.45);
            }
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


        } else if (elementType == ElementType.FIRE && ElementType.ICE.getEffect().getHolder().isPresent() && target.hasEffect(ElementType.ICE.getEffect().getHolder().get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            target.removeEffect(ElementType.ICE.getEffect().getHolder().get());
            if (!(attacker instanceof LivingEntity)) {
                damage = damage * 1.2;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);
            } else {
                damage = damage * (1.2 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 8 + 8), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.2 + 0.1);
            }
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

        } else if (elementType == ElementType.ICE && ElementType.FIRE.getEffect().getHolder().isPresent() && target.hasEffect(ElementType.FIRE.getEffect().getHolder().get()) && (independentTargetStats.getIndependentStat(IndependentStatType.REACTION_TIME) <= 0)) {
            target.removeEffect(ElementType.FIRE.getEffect().getHolder().get());
            if (!(attacker instanceof LivingEntity)) {
                damage = damage * 1.15;
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, 40);
            } else {
                damage = damage * (1.15 + attackerStats.getFinalStat(StatType.ELEMENTAL_MASTERY) / 2.50);
                independentTargetStats.setIndependentStat(IndependentStatType.REACTION_TIME, independentAttackerStats.getIndependentStat(IndependentStatType.REACTION_COOLDOWN));
            }

            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.3F, 0.5F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, (int) (Math.random() * 8 + 8), Math.random() * 0.5, Math.random(), Math.random() * 0.5, Math.random() * 0.2 + 0.1);
            }
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

            Holder<MobEffect> elementalHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(elementType.getEffect().getKey());
            target.addEffect(new MobEffectInstance(elementalHolder, 120));
        }


        ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
        armorStand.addTag("indicator");
        x = x + Math.random() - 0.5;
        y = y + 1 + Math.random();
        z = z + Math.random() - 0.5;
        System.out.println("Indicator pos: " + x + ", " + y + ", " + z);
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
            currentHP = Math.max(0, currentHP - damage);
            if (!(lostShield == 0)) {
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
            shield = Math.max(0, shield - damage);
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
        System.out.println("[DEBUG] Created new damage indicator.");

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
            ModNetworking.sendToPlayer(new ClientStatReceiver(independentTargetStats.getIndependentStatMap(), player.getFoodData().getSaturationLevel(), independentTargetStats.getElementType(), independentTargetStats.getWeapon(), targetStats.getFinalStatMap()), player);
        }

        if (attacker instanceof LivingEntity livingAttacker) {
            MinecraftForge.EVENT_BUS.post(new HurtCustomEvent(livingAttacker, target, damage));
        } else {
            MinecraftForge.EVENT_BUS.post(new HurtCustomEvent(null, target, damage));
        }

        if (currentHP <= 0) {
            if (attacker instanceof LivingEntity) {
                MinecraftForge.EVENT_BUS.post(new DeathHandlerEvent(target, null, (LivingEntity) attacker));
            } else {
                MinecraftForge.EVENT_BUS.post(new DeathHandlerEvent(target, source, null));
            }
            System.out.println(target.getName().getString() + " is gonna die.");
        } else {
            System.out.println(target.getName().getString() + "'s hp is now " + currentHP);
            independentTargetStats.setIndependentStat(IndependentStatType.HEALTH, currentHP);
            independentTargetStats.setIndependentStat(IndependentStatType.SHIELD, shield);
            RegistryAccess registryAccess1 = level.registryAccess();
            Registry<DamageType> damageTypeRegistry1 = registryAccess1.registryOrThrow(Registries.DAMAGE_TYPE);
            Holder<DamageType> damageHolder1 = damageTypeRegistry1.getHolderOrThrow(DamageTypes.GENERIC);
            DamageSource damageSourceFinal1 = new DamageSource(damageHolder1, (Vec3) null);
            target.hurt(damageSourceFinal1, 0.01F);
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

        System.out.println("[DEBUG] Initialised Indicator.");

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

        double maxHealth = stats.getFinalStat(StatType.MAX_HEALTH);
        independentStats.setIndependentStat(IndependentStatType.HEALTH, maxHealth);
        independentStats.setIndependentStat(IndependentStatType.SHIELD, 0);
        independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, 0);
        target.invulnerableTime = 0;

        if (killer == null) {
            if (source == null) {
                RegistryAccess registryAccess = level.registryAccess();
                Registry<DamageType> damageTypeRegistry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
                Holder<DamageType> damageHolder = damageTypeRegistry.getHolderOrThrow(DamageTypes.GENERIC);
                DamageSource damageSourceFinal = new DamageSource(damageHolder, (Vec3) null);
                target.hurt(damageSourceFinal, Float.MAX_VALUE);
            } else {
                Holder<DamageType> type = source.typeHolder();
                RegistryAccess registryAccess = level.registryAccess();
                Registry<DamageType> damageTypeRegistry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
                Holder<DamageType> damageHolder = damageTypeRegistry.getHolderOrThrow(DamageTypes.GENERIC);
                DamageSource damageSourceFinal = new DamageSource(damageHolder, (Vec3) null);
                target.hurt(damageSourceFinal, Float.MAX_VALUE);
                if (!(target instanceof ServerPlayer)) return;
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
                    String message = ((ServerPlayer) target).getGameProfile().getName() + "was prickled to death.";
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
            if (!(target instanceof ServerPlayer)) return;
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
        System.out.println("[DEBUG] Changed Equipment. Actual Defense: " + addStats.getAddStat(StatType.ARMOR));
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
                        ((Player) entity).getFoodData().setSaturation(Math.max(0, saturation - 1));
                    } else {
                        ((Player) entity).getFoodData().setFoodLevel(Math.max(0, food - 1));
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
        if (dashTime > 0) {
            stats.setIndependentStat(IndependentStatType.DASH_TIME, (int) (dashTime - 1));
        } else if (stats.getIndependentStat(IndependentStatType.DASH_AVAILABLE) < (int) finalStats.getFinalStat(StatType.DASH_COUNT)) {
            stats.setIndependentStat(IndependentStatType.DASH_AVAILABLE, stats.getIndependentStat(IndependentStatType.DASH_AVAILABLE) + 1);
            stats.setIndependentStat(IndependentStatType.DASH_TIME, (int) finalStats.getFinalStat(StatType.DASH_COOLDOWN));
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

        ItemStack weapon = ItemStack.EMPTY;
        Item mainItem;
        Item offItem;
        ItemStack mainHandItem = entity.getMainHandItem();
        ItemStack offHandItem = entity.getOffhandItem();

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

        if (entity instanceof Player) {
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
                double multStat = Math.max(0, multStats.getMultStat(stat));


                if ((stat == StatType.ATK_SPEED) || (stat == StatType.ATK_COOLDOWN)) {
                    double atkSpeed = (baseStats.getBaseStat(StatType.ATK_SPEED) + addStats.getAddStat(StatType.ATK_SPEED) * multStats.getMultStat(StatType.ATK_SPEED));
                    finalStats.setFinalStat(StatType.ATK_SPEED, atkSpeed);
                    double atkCooldown = (baseStats.getBaseStat(StatType.ATK_COOLDOWN) + addStats.getAddStat(StatType.ATK_COOLDOWN)) * multStats.getMultStat(StatType.ATK_COOLDOWN) / (1 + finalStats.getFinalStat(StatType.ATK_SPEED));
                    finalStats.setFinalStat(StatType.ATK_COOLDOWN, atkCooldown);
                    dirtyStats.setDirtyStat(StatType.ATK_SPEED, false);
                    dirtyStats.setDirtyStat(StatType.ATK_COOLDOWN, false);
                } else if (stat == StatType.ELEMENTAL_AFFINITY) {
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                    newStat = Math.min(200, newStat);
                    finalStats.setFinalStat(stat, newStat);
                    dirtyStats.setDirtyStat(stat, false);
                    int reactionCooldown = (int) (40 - 19 * newStat);
                    reactionCooldown = Math.max(2, reactionCooldown);

                    independentStats.setIndependentStat(IndependentStatType.REACTION_COOLDOWN, reactionCooldown);
                } else if (stat == StatType.MOVEMENT_SPEED) {
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                    finalStats.setFinalStat(stat, newStat);
                    dirtyStats.setDirtyStat(stat, false);
                    if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                        Objects.requireNonNull(entity.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(newStat / 20);
                    }
                     } else if (stat == StatType.MAX_HEALTH) {
                    double oldStat = finalStats.getFinalStat(StatType.MAX_HEALTH);
                double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
                finalStats.setFinalStat(stat, newStat);
                independentStats.setIndependentStat(IndependentStatType.HEALTH, independentStats.getIndependentStat(IndependentStatType.HEALTH)*newStat/oldStat);
                dirtyStats.setDirtyStat(stat, false);
                baseStats.setBaseStat(StatType.MAX_BOND_OF_LIFE, 2 * newStat);
                dirtyStats.setDirtyStat(StatType.MAX_BOND_OF_LIFE, true);
            } else if (stat == StatType.MAX_BOND_OF_LIFE) {
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat))* multStat;
                    double oldStat = finalStats.getFinalStat(stat);
                    finalStats.setFinalStat(StatType.MAX_BOND_OF_LIFE, newStat);
                    independentStats.setIndependentStat(IndependentStatType.BOND_OF_LIFE, independentStats.getIndependentStat(IndependentStatType.BOND_OF_LIFE) * newStat/oldStat);
                } else {
                    double newStat = (baseStats.getBaseStat(stat) + addStats.getAddStat(stat)) * multStat;
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
            ModNetworking.sendToPlayer(new ClientStatReceiver(independentStats.getIndependentStatMap(), player.getFoodData().getSaturationLevel(), independentStats.getElementType(), independentStats.getWeapon(), finalStats.getFinalStatMap()), player);
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
                if (bondOfLife > 0) {
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

                if (bondOfLife > 0) {
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
    public static void EffectCastingEvent(EffectCastingEvent event) {
        Level level = event.getTarget().level();
        if (level.isClientSide) return;
        LivingEntity target = event.getTarget();
        LivingEntity caster = event.getCaster();
        MobEffect effect = event.getEffect().get();
        if (target == null) return;
        LazyOptional<IFinalStats> targetStatsL = target.getCapability(ModCapabilities.FINAL_STATS);
        if (!targetStatsL.isPresent()) return;
        IFinalStats targetStats = targetStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
        if (caster == null) {
            double baseChance = event.getEffectChance();
            double effectRes = targetStats.getFinalStat(StatType.EFFECT_RES);
            int duration = event.getEffectDuration();
            int amplifier = event.getEffectAmp();
            double effectChance;
            if (!effect.isBeneficial()) {
                if (effect instanceof ICCDebuff) {
                    baseChance -= targetStats.getFinalStat(StatType.CC_RES);
                }
                effectChance = baseChance - effectRes;
            } else {
                effectChance = baseChance;
            }
            double chance = Math.random();
            if (chance <= effectChance) {
                if (event.getEffect().getKey() != null) {
                    Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(event.getEffect().getKey());
                    target.addEffect(new MobEffectInstance(effectHolder, duration, amplifier));
                }
            }
        } else {
            LazyOptional<IFinalStats> casterStatsL = caster.getCapability(ModCapabilities.FINAL_STATS);
            if (!casterStatsL.isPresent()) return;
            IFinalStats casterStats = casterStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            double baseChance = event.getEffectChance();
            double effectRes = targetStats.getFinalStat(StatType.EFFECT_RES);

            double effectHitRate = casterStats.getFinalStat(StatType.EFFECT_HIT_RATE);
            double effectAmp = casterStats.getFinalStat(StatType.EFFECT_EFFICIENCY);

            double effectChance;
            int duration = event.getEffectDuration();
            int amplifier = event.getEffectAmp();
            if (!effect.isBeneficial()) {
                if (effect instanceof ICCDebuff) {
                    baseChance -= targetStats.getFinalStat(StatType.CC_RES);
                    effectAmp *= casterStats.getFinalStat(StatType.CC_DEBUFF_EFFICIENCY);
                }
                if (!(effect instanceof IDoTDebuff)) {
                    effectAmp *= casterStats.getFinalStat(StatType.NON_DAMAGING_DEBUFF_EFFICIENCY);
                }
                effectChance = baseChance - effectRes + effectHitRate;
            } else {
                effectChance = baseChance + effectHitRate;
            }
            double chance = Math.random();
            if (chance <= effectChance) {
                if (event.getEffect().getKey() != null) {
                    Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(event.getEffect().getKey());
                    target.addEffect(new MobEffectInstance(effectHolder, (int) (duration * effectAmp), (int) (amplifier * casterStats.getFinalStat(StatType.EFFECT_DURATION))));
                }
            }
        }
    }

    @SubscribeEvent
    public static void environmentalElementApplication(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity.level().isClientSide()) return;

        if (entity.isInWaterRainOrBubble() && !(ModEffects.WET_EFFECT.getKey() == null)) {
            Holder<MobEffect> elementalHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ModEffects.WET_EFFECT.getKey());
            entity.addEffect(new MobEffectInstance(elementalHolder, 120));
        }
        if (entity.isInPowderSnow && !(ModEffects.FROSTED_EFFECT.getKey() == null)) {
            Holder<MobEffect> elementalHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ModEffects.FROSTED_EFFECT.getKey());
            entity.addEffect(new MobEffectInstance(elementalHolder, 120));
        }
        if (entity.isInLava() && !(ModEffects.MOLTEN_EFFECT.getKey() == null)) {
            Holder<MobEffect> elementalHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ModEffects.MOLTEN_EFFECT.getKey());
            entity.addEffect(new MobEffectInstance(elementalHolder, 120));
        }
    }

    @SubscribeEvent
    public static void breakDenial(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = event.getPlayer().level();
        if (player == null || (level.isClientSide())) return;
        if (ModEffects.CREATIVE_SHOCK_EFFECT.getHolder().isPresent() && player.hasEffect(ModEffects.CREATIVE_SHOCK_EFFECT.getHolder().get())) {
            event.setCanceled(true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.3F, 0.7F);
        }
    }

    @SubscribeEvent
    public static void placementDenial(BlockEvent.EntityPlaceEvent event) {
        Entity player = event.getEntity();
        if (player instanceof Player) {
            Level level = event.getEntity().level();
            if ((level.isClientSide())) return;
            if (ModEffects.CREATIVE_SHOCK_EFFECT.getHolder().isPresent() && ((Player) player).hasEffect(ModEffects.CREATIVE_SHOCK_EFFECT.getHolder().get())) {
                event.setCanceled(true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.3F, 0.7F);
            }
        }
    }

    @SubscribeEvent
    public static void buttonPress(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (ModKeyBindings.DASH_KEY.consumeClick() && Minecraft.getInstance().screen == null) {
                ModNetworking.sendToServer(new DashInputReceiver());
            }
            if (Minecraft.getInstance().options.keyJump.consumeClick() && Minecraft.getInstance().screen == null) {
                ModNetworking.sendToServer(new AdditionalJumpInputReceiver());
            }
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

            if (!player.isFallFlying() && !player.onGround() && !player.isInLiquid()) {
                Vec3 mov = player.getDeltaMovement().add(0, 0.08 + stats.getFinalStat(StatType.GRAVITY_ACCELERATION) ,0);
                player.setDeltaMovement(mov);
                player.hurtMarked = true;
            }

            if (!(player instanceof Player)) return;
            if (player.onGround() || player.isInLava() || player.isInWater()) {
                player.getTags().add("onGround");
                independentStats.setIndependentStat(IndependentStatType.ADDITIONAL_JUMP_AVAILABLE, (int) stats.getFinalStat(StatType.ADDITIONAL_JUMP));

            } else {
                player.getTags().remove("onGround");
            }
        }
    }

    @SubscribeEvent
    public static void onJump (LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity == null || entity.level().isClientSide) return;

        LazyOptional<IFinalStats> statsL = entity.getCapability(ModCapabilities.FINAL_STATS);

        if (!statsL.isPresent()) return;

        IFinalStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

        Vec3 mov = entity.getDeltaMovement();
        Vec3 jump = new Vec3(mov.x,  Math.sqrt(stats.getFinalStat(StatType.JUMP_STRENGTH)* 2 * 0.08) + mov.y - 0.42, mov.z);
        if (entity.isSprinting()) {
            Vec3 jumpI = new Vec3(mov.x, 0, mov.z).scale(0.3);
            jump = jump.add(jumpI);
        }
        entity.setDeltaMovement(jump);
        entity.hurtMarked = true;
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
        if (mc.gameMode.getPlayerMode() == GameType.ADVENTURE || mc.gameMode.getPlayerMode() == GameType.SURVIVAL) {
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/health_bar_background.png"), width / 2 - 101, height - 42, 0, 0, 91, 11, 91, 11);
            player.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF-> {
                player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    double maxHP = statsF.getFinalStat(StatType.MAX_HEALTH);
                    double hp = statsI.getIndependentStat(IndependentStatType.HEALTH);
                    double hpPerc = hp/maxHP;
                    hp = (double) ((int) (hp * 100)) /100;
                    maxHP = (double) ((int) (maxHP * 100)) /100;
                    gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/health_bar.png"), width / 2 - 89, height - 40, 0, 0, (int) (77 * hpPerc), 7, 77, 7);
                    gui.drawCenteredString(mc.font, hp + "/" + maxHP, width / 2 - 50, height - 40, 0xFFFFFF);
                    double shield = statsI.getIndependentStat(IndependentStatType.SHIELD);
                    if (shield > 0) {
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/shield_background.png"), width/2 - 101, height - 41, 0, 0, 9, 9, 9, 9);
                        double shieldPerc = Math.min(maxHP, shield)/maxHP;
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/shield.png"), width/2 - 92, height - 43, 0, 0, (int) (83 * shieldPerc), 13, 83, 13);
                    }

                    double hunger = player.getFoodData().getFoodLevel();
                    double sat = player.getFoodData().getSaturationLevel();
                    double saturation = Math.min(sat, 20);
                    double armor = (double) ((int) (statsF.getFinalStat(StatType.ARMOR) * 10)) /10;


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

                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation_background.png"), width/2 + 92, height - 41, 0, 0, 9, 9, 9, 9);
                        if (saturation != 20) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation.png"), (int) ((double) width / 2 + 90 - (saturation / 20 * 83)), height - 43, height - 42, 0, 0, (int) (83 * saturation / 20), 13, 83, 13);
                        } else {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation.png"), (int) ((double) width / 2 + 9), height - 43, height - 43, 0, 0, 83, 13, 83, 13);
                        }
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation_start.png"), width/2 + 88, height - 43, 0, 0, 3, 13, 3, 13);
                        if (saturation == 20) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/saturation_end.png"), width/2 + 9, height - 43, 0, 0, 3, 13, 3, 13);
                        }
                    }


                    int oxygen = Math.max(0, player.getAirSupply());
                    int maxO2 = player.getMaxAirSupply();
                    if (oxygen < maxO2) {
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/air_bar_background.png"), width / 2 + 20, height - 55, 0, 0, 91, 11, 91, 11);
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/air_bar.png"), (int) ((double) width / 2 + 100 - ((double) oxygen / maxO2 * 77)), height - 53, 0, 0, (int) (77 * oxygen / maxO2), 7, 77, 7);
                        gui.drawCenteredString(mc.font,  oxygen/20 + "/" + maxO2/20, width / 2 + 60, height - 53, 0xFFFFFF);
                    }

                    double bondOfLife = statsI.getIndependentStat(IndependentStatType.BOND_OF_LIFE);
                    int bondOfLifePerc = ((int) (bondOfLife / maxHP * 100))/100;
                    if (bondOfLifePerc > 0) {
                        gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar_background.png"), width / 2 - 110, height - 55, 0, 0, 91, 11, 91, 11);
                        if (bondOfLifePerc <= 1) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar.png"), width / 2 - 98, height - 53, 0, 0, (int) (77 * bondOfLifePerc), 7, 77, 7);
                        } else if (bondOfLifePerc <= 2) {
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar.png"), width / 2 - 98, height - 53, 0, 0, 77, 7, 77, 7);
                            gui.blit(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "textures/gui/hud/bond_of_life_bar_2.png"), width / 2 - 98, height - 53, 0, 0, (int) (77 * (bondOfLifePerc - 1)), 7, 77, 7);
                        }
                        gui.drawCenteredString(mc.font,   bondOfLifePerc*100 + "%", width / 2 - 60, height - 53, 0xFFFFFF);
                    }
                });
                    }
            );
        }
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
                System.out.println("Saved Independent Stats' data for " + entity.getName());
            });
            living.getCapability(ModCapabilities.BASE_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IBaseStats", cap.serializeNBT());
                System.out.println("Saved Base Stats' data for " + entity.getName());
            });
            living.getCapability(ModCapabilities.ADD_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IAddStats", cap.serializeNBT());
                System.out.println("Saved Add Stats' data for " + entity.getName());
            });
            living.getCapability(ModCapabilities.MULT_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IMultStats", cap.serializeNBT());
                System.out.println("Saved Mult Stats' data for " + entity.getName());
            });
            living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IFinalStats", cap.serializeNBT());
                System.out.println("Saved Final Stats' data for " + entity.getName());
            });
            living.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(cap -> {
                living.getPersistentData().put("IDirtyStats", cap.serializeNBT());
                System.out.println("Saved Dirty Stats' data for " + entity.getName());
            });
            if (entity.getTags().contains("indicator")) {
                living.getCapability(ModCapabilities.INDICATOR_STATS).ifPresent(cap -> {
                    living.getPersistentData().put("IIndicatorStats", cap.serializeNBT());
                    System.out.println("Saved Indicator Stats' data for " + entity.getName());
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
                    cap.deserializeNBT(living.getPersistentData().getCompound("IIndependentStats"));
                    System.out.println("Loaded Independent Stats' data for " + entity.getName());
                });
            }
            if (living.getPersistentData().contains("IBaseStats") && living.getPersistentData().getCompound("IBaseStats").contains("BaseStats")) {
                living.getCapability(ModCapabilities.BASE_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IBaseStats"));
                    System.out.println("Loaded Base Stats' data for " + entity.getName());
                });
            }
            if (living.getPersistentData().contains("IAddStats") && living.getPersistentData().getCompound("IAddStats").contains("AddeStats")) {
                living.getCapability(ModCapabilities.ADD_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IAddStats"));
                    System.out.println("Loaded Add Stats' data for " + entity.getName());
                });
            }
            if (living.getPersistentData().contains("IMultStats") && living.getPersistentData().getCompound("IMultStats").contains("MultStats")) {
                living.getCapability(ModCapabilities.MULT_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IMultStats"));
                    System.out.println("Loaded Mult Stats' data for " + entity.getName());
                });
            }
            if (living.getPersistentData().contains("IFinalStats") && living.getPersistentData().getCompound("IFinalStats").contains("FinalStats")) {
                living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IFinalStats"));
                    System.out.println("Loaded Final Stats' data for " + entity.getName());
                });
            }
            if (living.getPersistentData().contains("IDirtyStats") && living.getPersistentData().getCompound("IDirtyStats").contains("DirtyStats")) {
                living.getCapability(ModCapabilities.DIRTY_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IDirtyStats"));
                    System.out.println("Loaded Dirty Stats' data for " + entity.getName());
                });
            }
            if (living.getTags().contains("indicator") && living.getPersistentData().contains("IIndicatorStats")) {
                living.getCapability(ModCapabilities.INDICATOR_STATS).ifPresent(cap -> {
                    cap.deserializeNBT(living.getPersistentData().getCompound("IIndicatorStats"));
                    System.out.println("Loaded Indicator Stats' data for " + entity.getName());
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
                    cap.deserializeNBT(rootTag.getCompound("IIndependentStats"));
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

        } catch (IOException e) {
            System.out.println("Failed to read player stats");
        }
    }

    @SubscribeEvent
    public static void registerCustomCommands (RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CustomCommands.register(dispatcher);
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
        double newMana = Math.max(0, mana - amount);
        statsI.setIndependentStat(IndependentStatType.MANA, newMana);
    }
}