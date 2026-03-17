package net.narutoxboruto.networking.info;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.info.ChakraControl;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncChakraControl implements CustomPacketPayload {
    private final boolean value;

    public static final CustomPacketPayload.Type<SyncChakraControl> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "sync_chakra_control"));

    public static final StreamCodec<FriendlyByteBuf, SyncChakraControl> STREAM_CODEC = StreamCodec.of((buf, packet) -> buf.writeBoolean(packet.value), buf -> new SyncChakraControl(buf.readBoolean()));

    public SyncChakraControl(boolean value) { this.value = value; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null && Minecraft.getInstance().player != null) {
                LocalPlayer clientPlayer = Minecraft.getInstance().player;
                clientPlayer.setData(NeoForgeCapabilities.CHAKRA_CONTROL, new ChakraControl(this.value));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
