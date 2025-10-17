package com.w1therx.adventurerfantasy.network.packet;

import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import com.w1therx.adventurerfantasy.effect.ModEffects;
import com.w1therx.adventurerfantasy.event.custom.ShieldingEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Objects;


public class InteractInputReceiver {



    public InteractInputReceiver() {
    }

    // Encode (send)
    public static void encode(InteractInputReceiver msg, FriendlyByteBuf buf) {
    }

    // Decode (receive)
    public static InteractInputReceiver decode(FriendlyByteBuf buf) {
        return new InteractInputReceiver();
    }

    // Handle on client (called in consumerMainThread in registration)
    public void handle (CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork( ()-> {
        Player player = ctx.getSender();
        if (player != null && player.level() instanceof ServerLevel level) {
            LazyOptional<IIndependentStats> independentStatsL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);
            LazyOptional<IBaseStats> baseStatsL = player.getCapability(ModCapabilities.BASE_STATS);
            LazyOptional<IAddStats> addStatsL = player.getCapability(ModCapabilities.ADD_STATS);
            LazyOptional<IMultStats> multStatsL = player.getCapability(ModCapabilities.MULT_STATS);
            LazyOptional<IDirtyStats> dirtyStatsL = player.getCapability(ModCapabilities.DIRTY_STATS);

            if ((!independentStatsL.isPresent()) || (!baseStatsL.isPresent()) || (!addStatsL.isPresent()) || (!multStatsL.isPresent()) || (!dirtyStatsL.isPresent()))
                return;

            IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IBaseStats baseStats = baseStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IAddStats addStats = addStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IMultStats multStats = multStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IDirtyStats dirtyStats = dirtyStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));



            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.SHIELD || player.getItemInHand(InteractionHand.OFF_HAND).getItem() == Items.SHIELD) {
                     if (independentStats.getIndependentStat(IndependentStatType.SHIELD_TIME) < 2 && ModEffects.CAUTION_EFFECT.getKey() != null) {
                         MinecraftForge.EVENT_BUS.post(new ShieldingEvent(player, player, 5, 5));
                         Holder<MobEffect> cautionHolder = BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ModEffects.CAUTION_EFFECT.getKey());

                         if (player.hasEffect(cautionHolder)) {
                             int amp = Objects.requireNonNull(player.getEffect(cautionHolder)).getAmplifier();
                             addStats.setAddStat(StatType.KNOCKBACK_RES, addStats.getAddStat(StatType.KNOCKBACK_RES) - 0.5);
                             addStats.setAddStat(StatType.ARMOR, addStats.getAddStat(StatType.ARMOR) - 0.5 * amp);

                             dirtyStats.setDirtyStat(StatType.KNOCKBACK_RES, true);
                             dirtyStats.setDirtyStat(StatType.ARMOR, true);
                         }

                         player.addEffect(new MobEffectInstance(cautionHolder, 5, 10));

                     }
                 }



        }
    }


);
}}
