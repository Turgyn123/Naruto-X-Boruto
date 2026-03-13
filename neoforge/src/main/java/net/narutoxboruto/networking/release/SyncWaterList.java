package net.narutoxboruto.networking.release;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.release.WaterList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncWaterList implements CustomPacketPayload {
    private final String waterList;

    public static final CustomPacketPayload.Type<SyncWaterList> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_water_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncWaterList> STREAM_CODEC = StreamCodec.ofMember(SyncWaterList::toBytes, SyncWaterList::new);

    public SyncWaterList(String waterList) { this.waterList = waterList; }

    public SyncWaterList(FriendlyByteBuf buf) {
        this.waterList = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(waterList);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                WaterList releaseListAttachment = player.getData(NeoForgeCapabilities.WATERLIST);
                releaseListAttachment.setValue(this.waterList);
            }
        });
    }
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}