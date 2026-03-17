package net.narutoxboruto.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

public record SyncStringData(String key, String value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncStringData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "sync_string"));

    public static final StreamCodec<FriendlyByteBuf, SyncStringData> STREAM_CODEC =
            StreamCodec.of(SyncStringData::encode, SyncStringData::decode);

    private static void encode(FriendlyByteBuf buf, SyncStringData msg) {
        buf.writeUtf(msg.key);
        buf.writeUtf(msg.value);
    }

    private static SyncStringData decode(FriendlyByteBuf buf) {
        return new SyncStringData(buf.readUtf(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
