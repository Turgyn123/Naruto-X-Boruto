package net.narutoxboruto.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

public record ServerActionPacket(String action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerActionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "server_action"));

    public static final StreamCodec<FriendlyByteBuf, ServerActionPacket> STREAM_CODEC =
            StreamCodec.of(ServerActionPacket::encode, ServerActionPacket::decode);

    private static void encode(FriendlyByteBuf buf, ServerActionPacket msg) {
        buf.writeUtf(msg.action);
    }

    private static ServerActionPacket decode(FriendlyByteBuf buf) {
        return new ServerActionPacket(buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
