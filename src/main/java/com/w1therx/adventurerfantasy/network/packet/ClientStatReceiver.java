package com.w1therx.adventurerfantasy.network.packet;

import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.HashMap;


public class ClientStatReceiver {

    private final float saturation;
    private final CompoundTag independentStats;
    private final CompoundTag finalStats;
    private final CompoundTag playerStats;
    private final CompoundTag baseStats;
    private final CompoundTag addStats;
    private final CompoundTag multStats;



    public ClientStatReceiver(float saturation, CompoundTag independentStats, CompoundTag finalStats, CompoundTag playerStats, CompoundTag baseStats, CompoundTag addStats, CompoundTag multStats) {
        this.saturation = saturation;
        this.independentStats = independentStats;
        this.finalStats = finalStats;
        this.playerStats = playerStats;
        this.baseStats = baseStats;
        this.addStats = addStats;
        this.multStats = multStats;
    }

    // Encode (send)
    public static void encode(ClientStatReceiver msg, FriendlyByteBuf buf) {

        CompoundTag nbt = new CompoundTag();

        nbt.putFloat("Saturation", msg.saturation);
        nbt.put("IIndependentStats", msg.independentStats);
        nbt.put("IFinalStats", msg.finalStats);
        nbt.put("IPlayerStats", msg.playerStats);
        nbt.put("IBaseStats", msg.baseStats);
        nbt.put("IAddStats", msg.addStats);
        nbt.put("IMultStats", msg.multStats);

        buf.writeNbt(nbt);
    }

    // Decode (receive)
    public static ClientStatReceiver decode(FriendlyByteBuf buf) {
        CompoundTag nbt = buf.readNbt();
        CompoundTag independentStats = new CompoundTag();
        CompoundTag finalStats = new CompoundTag();
        CompoundTag playerStats = new CompoundTag();
        CompoundTag baseStats = new CompoundTag();
        CompoundTag addStats = new CompoundTag();
        CompoundTag multStats = new CompoundTag();
        float saturation = 0;

        if (nbt instanceof CompoundTag) {
            saturation = nbt.getFloat("Saturation");
            independentStats = nbt.getCompound("IIndependentStats");
            finalStats = nbt.getCompound("IFinalStats");
            playerStats = nbt.getCompound("IPlayerStats");
            baseStats = nbt.getCompound("IBaseStats");
            addStats = nbt.getCompound("IAddStats");
            multStats = nbt.getCompound("IMultStats");

        }

        return new ClientStatReceiver(saturation, independentStats, finalStats, playerStats, baseStats, addStats, multStats);
    }

    // Handle on client (called in consumerMainThread in registration)
    public void handle (CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork( ()-> {
                Player player = Minecraft.getInstance().player;
                if (player == null) { return;}
                player.getFoodData().setSaturation(this.saturation);
                player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                    statsI.replaceActiveEffectList(new HashMap<>(), player);
                    statsI.deserializeNBT(this.independentStats, player);
                });
                player.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF -> statsF.deserializeNBT(this.finalStats));
                player.getCapability(ModCapabilities.PLAYER_STATS).ifPresent(statsP -> statsP.deserializeNBT(this.playerStats, player));
                player.getCapability(ModCapabilities.BASE_STATS).ifPresent(statsB -> statsB.deserializeNBT(this.baseStats));
                player.getCapability(ModCapabilities.ADD_STATS).ifPresent(statsA -> statsA.deserializeNBT(this.addStats));
                player.getCapability(ModCapabilities.MULT_STATS).ifPresent(statsM -> statsM.deserializeNBT(this.multStats));
        }



);
}}
