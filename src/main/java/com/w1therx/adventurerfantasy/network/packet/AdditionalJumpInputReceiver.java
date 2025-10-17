package com.w1therx.adventurerfantasy.network.packet;

import com.w1therx.adventurerfantasy.capability.IFinalStats;
import com.w1therx.adventurerfantasy.capability.IIndependentStats;
import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Objects;


public class AdditionalJumpInputReceiver {



    public AdditionalJumpInputReceiver() {
    }

    // Encode (send)
    public static void encode(AdditionalJumpInputReceiver msg, FriendlyByteBuf buf) {
    }

    // Decode (receive)
    public static AdditionalJumpInputReceiver decode(FriendlyByteBuf buf) {
        return new AdditionalJumpInputReceiver();
    }

    // Handle on client (called in consumerMainThread in registration)
    public void handle (CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork( ()-> {
        Player player = ctx.getSender();
        if (player != null && player.level() instanceof ServerLevel level) {
                 LazyOptional<IIndependentStats> independentStatsL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);
                 LazyOptional<IFinalStats> statsL = player.getCapability(ModCapabilities.FINAL_STATS);
                 if (!statsL.isPresent() || !independentStatsL.isPresent()) return;
                 IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                 IFinalStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));



                 if (!player.getTags().contains("onGround")  && (independentStats.getIndependentStat(IndependentStatType.ADDITIONAL_JUMP_AVAILABLE) > 0)) {
                     independentStats.setIndependentStat(IndependentStatType.ADDITIONAL_JUMP_AVAILABLE, independentStats.getIndependentStat(IndependentStatType.ADDITIONAL_JUMP_AVAILABLE) - 1);



                     Vec3 mov = player.getDeltaMovement();


                     double jumpStrength = 0.7;

                     if (player.hasEffect(MobEffects.JUMP)) {
                         int amp = Objects.requireNonNull(player.getEffect(MobEffects.JUMP)).getAmplifier();
                         jumpStrength = jumpStrength + 0.17 * (amp + 1);
                     }


                     Vec3 jump = new Vec3(0, jumpStrength - Math.min(0, mov.y), 0);


                     mov = mov.add(jump);

                     player.setDeltaMovement(mov);


                     player.hurtMarked = true;
                     level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1, player.getZ(), (int) (Math.random() * 6 + 12), Math.random() * 0.8, Math.random() * 0.1 - 1, Math.random() * 0.8, Math.random() * 0.3);
                     level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.8F, 0.7F);
                 }



        }
    }


);
}}
