package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ChakraControl;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ToggleChakraControl implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleChakraControl> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "toggle_chakra_control"));

    public static final StreamCodec<FriendlyByteBuf, ToggleChakraControl> STREAM_CODEC = StreamCodec.ofMember(ToggleChakraControl::toBytes, ToggleChakraControl::new);

    public ToggleChakraControl() {}
    public ToggleChakraControl(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Chakra chakra = serverPlayer.getData(NeoForgeCapabilities.CHAKRA);
                if (chakra.getValue() > 0) {
                    ChakraControl control = serverPlayer.getData(NeoForgeCapabilities.CHAKRA_CONTROL);
                    control.setValue(!control.isActive(), serverPlayer);
                } else {
                    serverPlayer.displayClientMessage(Component.translatable("msg.no_chakra"), true);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
