package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncReleaseList implements CustomPacketPayload {

    private final String releaseList;

    public static final CustomPacketPayload.Type<SyncReleaseList> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_release_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncReleaseList> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SyncReleaseList::getReleaseList, SyncReleaseList::new);

    public SyncReleaseList(String releaseList) {
        this.releaseList = releaseList;
    }

    public SyncReleaseList(FriendlyByteBuf buf) {
        this.releaseList = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(releaseList);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                ReleaseList releaseListAttachment = player.getData(NeoForgeCapabilities.RELEASE_LIST);
                releaseListAttachment.setValue(this.releaseList);
            }
        });
    }

    public String getReleaseList() {
        return releaseList;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
