package com.w1therx.adventurerfantasy.network.packet;

import com.w1therx.adventurerfantasy.capability.IFinalStats;
import com.w1therx.adventurerfantasy.capability.IIndependentStats;
import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.network.CustomPayloadEvent;



public class DashInputReceiver {



    public DashInputReceiver() {
    }

    // Encode (send)
    public static void encode(DashInputReceiver msg, FriendlyByteBuf buf) {
    }

    // Decode (receive)
    public static DashInputReceiver decode(FriendlyByteBuf buf) {
        return new DashInputReceiver();
    }

    // Handle on client (called in consumerMainThread in registration)
    public void handle (CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork( ()-> {
        Player player = ctx.getSender();
        if (player != null && player.level() instanceof ServerLevel level) {
            LazyOptional<IIndependentStats> independentStatsL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);
            LazyOptional<IFinalStats> statsL = player.getCapability(ModCapabilities.FINAL_STATS);
            if (!statsL.isPresent() || !independentStatsL.isPresent()) return;
            IIndependentStats independentStats = independentStatsL.orElseThrow(()-> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IFinalStats stats = statsL.orElseThrow(()-> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

            if (independentStats.getIndependentStat(IndependentStatType.DASH_AVAILABLE) > 0) {
                independentStats.setIndependentStat(IndependentStatType.DASH_AVAILABLE,independentStats.getIndependentStat(IndependentStatType.DASH_AVAILABLE)-1);
                double v = stats.getFinalStat(StatType.DASH_LENGTH) * 0.454;
                if (!player.onGround()) {
                    v =  v * 0.55;
                }
                Vec3 look = player.getLookAngle();
                Vec3 baseMov = player.getDeltaMovement();
                Vec3 mov = new Vec3(look.x, 0, look.z).normalize();

                if (player.isCrouching()) {
                    mov = mov.scale(-1);
                }

                player.setDeltaMovement(baseMov.add(mov.scale(v)));
                player.hurtMarked = true;
                independentStats.setIndependentStat(IndependentStatType.INVULNERABLE_TIME, (int) stats.getFinalStat(StatType.INVULNERABLE_DURATION));
                level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY()+1, player.getZ(), (int) (Math.random()*6 +10), Math.random()*0.5,Math.random(),Math.random()*0.5, Math.random()*0.5);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.8F, 0.7F);
            }


        }
    }


);
}}
