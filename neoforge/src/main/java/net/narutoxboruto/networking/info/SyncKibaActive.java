package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.client.PlayerData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncKibaActive implements CustomPacketPayload {
    private final boolean value;

    public static final CustomPacketPayload.Type<SyncKibaActive> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_kiba_active"));

    public static final StreamCodec<FriendlyByteBuf, SyncKibaActive> STREAM_CODEC = StreamCodec.of((buf, packet) -> buf.writeBoolean(packet.value), buf -> new SyncKibaActive(buf.readBoolean()));

    public SyncKibaActive(boolean value) { this.value = value; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerData.setKibaActive(this.value);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
