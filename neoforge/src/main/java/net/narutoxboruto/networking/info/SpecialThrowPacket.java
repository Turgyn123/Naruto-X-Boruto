package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.util.ServerActions;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpecialThrowPacket implements CustomPacketPayload {

    public static final Type<SpecialThrowPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "special_throw"));

    public static final StreamCodec<FriendlyByteBuf, SpecialThrowPacket> STREAM_CODEC = StreamCodec.ofMember(SpecialThrowPacket::toBytes, SpecialThrowPacket::new);

    public SpecialThrowPacket() {}

    public SpecialThrowPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ServerActions.specialThrow(serverPlayer);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
