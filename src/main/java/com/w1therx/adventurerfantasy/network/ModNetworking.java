package com.w1therx.adventurerfantasy.network;


import com.w1therx.adventurerfantasy.network.packet.AdditionalJumpInputReceiver;
import com.w1therx.adventurerfantasy.network.packet.ClientStatReceiver;
import com.w1therx.adventurerfantasy.network.packet.DashInputReceiver;
import com.w1therx.adventurerfantasy.network.packet.InteractInputReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;

public class ModNetworking {

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath("adventurerfantasy", "main"))
            .networkProtocolVersion(1) // Force Supplier<String>
            .clientAcceptedVersions(Channel.VersionTest.exact(1))  // Predicate<String>
            .serverAcceptedVersions(Channel.VersionTest.exact(1))  // Predicate<String>
            .simpleChannel();

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(DashInputReceiver.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DashInputReceiver::encode)
                .decoder(DashInputReceiver::decode)
                .consumerMainThread(DashInputReceiver::handle)
                .add();

        CHANNEL.messageBuilder(InteractInputReceiver.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(InteractInputReceiver::encode)
                .decoder(InteractInputReceiver::decode)
                .consumerMainThread(InteractInputReceiver::handle)
                .add();

        CHANNEL.messageBuilder(AdditionalJumpInputReceiver.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AdditionalJumpInputReceiver::encode)
                .decoder(AdditionalJumpInputReceiver::decode)
                .consumerMainThread(AdditionalJumpInputReceiver::handle)
                .add();

        CHANNEL.messageBuilder(ClientStatReceiver.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientStatReceiver::encode)
                .decoder(ClientStatReceiver::decode)
                .consumerMainThread(ClientStatReceiver::handle)
                .add();
    }
    public static void sendToServer(Object message) {
            ClientPacketListener con = Minecraft.getInstance().getConnection();
            if (con == null || Minecraft.getInstance().player == null) {
                System.out.println("Cannot handle ClientStatReceiver packet. No connection found");
                return;
            }
            Connection connection = con.getConnection();
            ModNetworking.CHANNEL.send(message, connection);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
    ModNetworking.CHANNEL.send(message, player.connection.getConnection());
    }
}
