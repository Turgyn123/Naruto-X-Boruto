package net.narutoxboruto.networking.release;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.release.WindList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncWindList implements CustomPacketPayload {
    private final String windList;

    public static final CustomPacketPayload.Type<SyncWindList> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_wind_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncWindList> STREAM_CODEC = StreamCodec.ofMember(SyncWindList::toBytes, SyncWindList::new);

    public SyncWindList(String windList) { this.windList = windList; }

    public SyncWindList(FriendlyByteBuf buf) {
        this.windList = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(windList);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                WindList releaseListAttachment = player.getData(NeoForgeCapabilities.WINDLIST);
                releaseListAttachment.setValue(this.windList);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
