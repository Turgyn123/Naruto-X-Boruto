package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.util.ServerActions;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RechargeChakra implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RechargeChakra> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("narutoxboruto", "recharge_chakra"));

    public static final StreamCodec<FriendlyByteBuf, RechargeChakra> STREAM_CODEC = StreamCodec.ofMember(RechargeChakra::toBytes, RechargeChakra::new);

    public RechargeChakra() {}
    public RechargeChakra(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ServerActions.rechargeChakra(serverPlayer);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
