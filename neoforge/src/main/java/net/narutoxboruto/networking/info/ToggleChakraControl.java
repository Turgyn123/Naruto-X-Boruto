package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.util.ServerActions;
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
                ServerActions.toggleChakraControl(serverPlayer);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
