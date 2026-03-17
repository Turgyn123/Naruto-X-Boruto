package net.narutoxboruto.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

public record SyncIntData(String key, int value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncIntData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "sync_int"));

    public static final StreamCodec<FriendlyByteBuf, SyncIntData> STREAM_CODEC =
            StreamCodec.of(SyncIntData::encode, SyncIntData::decode);

    private static void encode(FriendlyByteBuf buf, SyncIntData msg) {
        buf.writeUtf(msg.key);
        buf.writeInt(msg.value);
    }

    private static SyncIntData decode(FriendlyByteBuf buf) {
        return new SyncIntData(buf.readUtf(), buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
