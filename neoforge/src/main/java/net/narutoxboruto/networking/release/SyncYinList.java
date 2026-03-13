package net.narutoxboruto.networking.release;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.release.YinList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncYinList implements CustomPacketPayload {
    private final String yinList;

    public static final CustomPacketPayload.Type<SyncYinList> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_yin_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncYinList> STREAM_CODEC = StreamCodec.ofMember(SyncYinList::toBytes, SyncYinList::new);

    public SyncYinList(String yinList) { this.yinList = yinList; }

    public SyncYinList(FriendlyByteBuf buf) {
        this.yinList = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(yinList);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                YinList releaseListAttachment = player.getData(NeoForgeCapabilities.YINLIST);
                releaseListAttachment.setValue(this.yinList);
            }
        });
    }
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
