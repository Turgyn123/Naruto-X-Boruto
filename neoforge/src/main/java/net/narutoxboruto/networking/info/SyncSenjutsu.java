package net.narutoxboruto.networking.info;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.info.Senjutsu;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncSenjutsu implements CustomPacketPayload {
    private final int value;

    public static final CustomPacketPayload.Type<SyncSenjutsu> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_senjutsu"));

    public static final StreamCodec<FriendlyByteBuf, SyncSenjutsu> STREAM_CODEC = StreamCodec.of((buf, packet) -> buf.writeInt(packet.value), buf -> new SyncSenjutsu(buf.readInt()));

    public SyncSenjutsu(int value) { this.value = value; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null && Minecraft.getInstance().player != null) {
                LocalPlayer clientPlayer = Minecraft.getInstance().player;
                clientPlayer.setData(NeoForgeCapabilities.SENJUTSU, new Senjutsu(this.value));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
