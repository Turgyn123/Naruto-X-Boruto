package net.narutoxboruto.networking.info;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.info.Rank;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncRank implements CustomPacketPayload {
    private String rank;

    public static final CustomPacketPayload.Type<SyncRank> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_rank"));

    public static final StreamCodec<FriendlyByteBuf, SyncRank> STREAM_CODEC = StreamCodec.of((buf, packet) -> buf.writeUtf(packet.rank), buf -> new SyncRank(buf.readUtf()));

    public SyncRank(String rank) {
        this.rank = rank;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null && Minecraft.getInstance().player != null) {
                LocalPlayer clientPlayer = Minecraft.getInstance().player;

                Rank rank = clientPlayer.getData(NeoForgeCapabilities.RANK);
                rank.setValue(this.rank);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
