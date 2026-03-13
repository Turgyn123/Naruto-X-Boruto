package net.narutoxboruto.networking.release;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.release.YangList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncYangList implements CustomPacketPayload {
    private final String yangList;

    public static final CustomPacketPayload.Type<SyncYangList> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_yang_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncYangList> STREAM_CODEC = StreamCodec.ofMember(SyncYangList::toBytes, SyncYangList::new);

    public SyncYangList(String yangList) { this.yangList = yangList; }

    public SyncYangList(FriendlyByteBuf buf) {
        this.yangList = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(yangList);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                YangList releaseListAttachment = player.getData(NeoForgeCapabilities.YANGLIST);
                releaseListAttachment.setValue(this.yangList);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
