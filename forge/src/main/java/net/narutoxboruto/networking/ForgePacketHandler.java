package net.narutoxboruto.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.narutoxboruto.main.Main;

public class ForgePacketHandler {
    private static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "main"))
                .networkProtocolVersion(1)
                .simpleChannel();

        // Clientbound
        CHANNEL.messageBuilder(SyncIntData.class)
                .encoder(SyncIntData::encode).decoder(SyncIntData::decode)
                .consumerMainThread(SyncIntData::handle).add();
        CHANNEL.messageBuilder(SyncStringData.class)
                .encoder(SyncStringData::encode).decoder(SyncStringData::decode)
                .consumerMainThread(SyncStringData::handle).add();
        CHANNEL.messageBuilder(SyncBoolData.class)
                .encoder(SyncBoolData::encode).decoder(SyncBoolData::decode)
                .consumerMainThread(SyncBoolData::handle).add();
        CHANNEL.messageBuilder(SyncNbtData.class)
                .encoder(SyncNbtData::encode).decoder(SyncNbtData::decode)
                .consumerMainThread(SyncNbtData::handle).add();

        // Serverbound
        CHANNEL.messageBuilder(ServerActionPacket.class)
                .encoder(ServerActionPacket::encode).decoder(ServerActionPacket::decode)
                .consumerMainThread(ServerActionPacket::handle).add();
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        CHANNEL.send(msg, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(Object msg) {
        CHANNEL.send(msg, PacketDistributor.SERVER.noArg());
    }
}
