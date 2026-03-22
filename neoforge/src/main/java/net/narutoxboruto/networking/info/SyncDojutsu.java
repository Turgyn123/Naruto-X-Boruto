package net.narutoxboruto.networking.info;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.client.PlayerData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncDojutsu implements CustomPacketPayload {

    private final CompoundTag data;

    public static final Type<SyncDojutsu> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_dojutsu")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncDojutsu> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeNbt(value.data),
            buf -> new SyncDojutsu(buf.readNbt())
    );

    public SyncDojutsu(CompoundTag data) {
        this.data = data;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                Dojutsu dojutsu = Dojutsu.fromNbt(this.data);
                player.setData(NeoForgeCapabilities.DOJUTSU, dojutsu);
                PlayerData.setDojutsuUnlockedList(dojutsu.getUnlockedListRaw());
                PlayerData.setDojutsuLeftEye(dojutsu.getLeftEye());
                PlayerData.setDojutsuRightEye(dojutsu.getRightEye());
                PlayerData.setDojutsuTimer(dojutsu.getTimer());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
