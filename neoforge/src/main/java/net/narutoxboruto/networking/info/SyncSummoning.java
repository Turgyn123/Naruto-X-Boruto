package net.narutoxboruto.networking.info;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.stats.Summoning;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncSummoning implements CustomPacketPayload {
    private final int value;

    public static final CustomPacketPayload.Type<SyncSummoning> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_summoning"));

    public static final StreamCodec<FriendlyByteBuf, SyncSummoning> STREAM_CODEC = StreamCodec.of((buf, packet) -> buf.writeInt(packet.value), buf -> new SyncSummoning(buf.readInt()));

    public SyncSummoning(int value) { this.value = value; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null && Minecraft.getInstance().player != null) {
                LocalPlayer clientPlayer = Minecraft.getInstance().player;
                clientPlayer.setData(NeoForgeCapabilities.SUMMONING, new Summoning(this.value));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
