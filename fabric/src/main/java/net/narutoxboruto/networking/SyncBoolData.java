package net.narutoxboruto.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

public record SyncBoolData(String key, boolean value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncBoolData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "sync_bool"));

    public static final StreamCodec<FriendlyByteBuf, SyncBoolData> STREAM_CODEC =
            StreamCodec.of(SyncBoolData::encode, SyncBoolData::decode);

    private static void encode(FriendlyByteBuf buf, SyncBoolData msg) {
        buf.writeUtf(msg.key);
        buf.writeBoolean(msg.value);
    }

    private static SyncBoolData decode(FriendlyByteBuf buf) {
        return new SyncBoolData(buf.readUtf(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
