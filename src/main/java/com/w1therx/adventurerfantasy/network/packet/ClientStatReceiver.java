package com.w1therx.adventurerfantasy.network.packet;

import ca.weblite.objc.Message;
import com.w1therx.adventurerfantasy.capability.IFinalStats;
import com.w1therx.adventurerfantasy.capability.IIndependentStats;
import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import com.w1therx.adventurerfantasy.common.enums.ElementType;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ClientStatReceiver {

    private final float saturation;
    private final ElementType element;
    private final ItemStack item;
    private final Map<StatType, Double> stats;
    private final Map<IndependentStatType, Double> independentStats;



    public ClientStatReceiver(Map<IndependentStatType, Double> independentStats, float saturation, ElementType element, ItemStack item, Map<StatType, Double> stats) {
        this.independentStats = independentStats;
        this.saturation = saturation;
        this.element = element;
        this.item = item;
        this.stats = stats;
    }

    // Encode (send)
    public static void encode(ClientStatReceiver msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.independentStats.size());
        for (Map.Entry<IndependentStatType, Double> entry : msg.independentStats.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeDouble(entry.getValue());
        }
        buf.writeFloat(msg.saturation);
        buf.writeVarInt(msg.element.ordinal());
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(msg.item.getItem()));
        buf.writeVarInt(msg.stats.size());
        for (Map.Entry<StatType, Double> entry : msg.stats.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeDouble(entry.getValue());
        }
    }

    // Decode (receive)
    public static ClientStatReceiver decode(FriendlyByteBuf buf) {
        int sizeI = buf.readVarInt();
        Map<IndependentStatType, Double> independentStats = new HashMap<>();
        for (int II = 0; II < sizeI; II++) {
            IndependentStatType key = buf.readEnum(IndependentStatType.class);
            double value = buf.readDouble();
            independentStats.put(key, value);
        }
        float saturation = buf.readFloat();
        ElementType element = ElementType.values() [buf.readVarInt()];
        Item i = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
        ItemStack item = new ItemStack(i);

        int size = buf.readVarInt();
        Map<StatType, Double> stats = new HashMap<>();
        for (int I = 0; I < size; I++) {
            StatType key = buf.readEnum(StatType.class);
            double value = buf.readDouble();
            stats.put(key, value);
        }
        return new ClientStatReceiver(independentStats, saturation, element, item, stats);
    }

    // Handle on client (called in consumerMainThread in registration)
    public void handle (CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork( ()-> {
                Player player = Minecraft.getInstance().player;
                if (player == null) {
                    return;}
                player.getFoodData().setSaturation(this.saturation);
                LazyOptional<IIndependentStats> independentStatsL = player.getCapability(ModCapabilities.INDEPENDENT_STATS);
                LazyOptional<IFinalStats> statsL = player.getCapability(ModCapabilities.FINAL_STATS);
                if (!statsL.isPresent() || !independentStatsL.isPresent()) return;
                IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                IFinalStats stats = statsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

                IndependentStatType[] independentStatList = IndependentStatType.values();
                for (IndependentStatType stat : Arrays.stream(independentStatList).toList()) {
                    double value = this.independentStats.getOrDefault(stat, stat.getBaseValue());
                    independentStats.setIndependentStat(stat, value);
                }
                independentStats.setElementType(this.element);
                independentStats.setWeapon(this.item);
                StatType[] statList = StatType.values();

                for (StatType stat : Arrays.stream(statList).toList()) {
                    double value = this.stats.getOrDefault(stat, stat.getBaseValue());
                    stats.setFinalStat(stat, value);
                }


        }



);
}}
