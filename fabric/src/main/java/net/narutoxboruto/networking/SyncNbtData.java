package net.narutoxboruto.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

public record SyncNbtData(String key, CompoundTag nbt) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncNbtData> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "sync_nbt"));

    public static final StreamCodec<FriendlyByteBuf, SyncNbtData> STREAM_CODEC =
            StreamCodec.of(SyncNbtData::encode, SyncNbtData::decode);

    private static void encode(FriendlyByteBuf buf, SyncNbtData msg) {
        buf.writeUtf(msg.key);
        buf.writeNbt(msg.nbt);
    }

    private static SyncNbtData decode(FriendlyByteBuf buf) {
        return new SyncNbtData(buf.readUtf(), buf.readNbt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
