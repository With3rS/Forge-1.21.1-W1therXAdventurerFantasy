package com.w1therx.adventurerfantasy.network.packet;

import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.effect.general.ModEffects;
import com.w1therx.adventurerfantasy.event.custom.EffectApplicationEvent;
import com.w1therx.adventurerfantasy.event.custom.ShieldingEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.network.CustomPayloadEvent;


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
            System.out.println("Player interacted");

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
                     if (!independentStats.getActiveEffectList().containsKey(ModEffects.CAUTION_EFFECT.get()) || independentStats.getActiveEffectList().get(ModEffects.CAUTION_EFFECT.get()).duration() < 2) {
                         MinecraftForge.EVENT_BUS.post(new ShieldingEvent(player, player, 5, 8));
                         MinecraftForge.EVENT_BUS.post(new EffectApplicationEvent(player, player, ModEffects.CAUTION_EFFECT.get(), 8, 1, 5, 5, 5, true));
                     }
                 }



        }
    }


);
}}
