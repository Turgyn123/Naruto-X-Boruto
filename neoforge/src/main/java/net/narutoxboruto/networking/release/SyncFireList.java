package net.narutoxboruto.networking.release;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.release.FireList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncFireList implements CustomPacketPayload {
    private final String fireList;

    public static final CustomPacketPayload.Type<SyncFireList> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_fire_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncFireList> STREAM_CODEC = StreamCodec.ofMember(SyncFireList::toBytes, SyncFireList::new);

    public SyncFireList(String fireList) { this.fireList = fireList; }

    public SyncFireList(FriendlyByteBuf buf) {
        this.fireList = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(fireList);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                FireList releaseListAttachment = player.getData(NeoForgeCapabilities.FIRELIST);
                releaseListAttachment.setValue(this.fireList);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
