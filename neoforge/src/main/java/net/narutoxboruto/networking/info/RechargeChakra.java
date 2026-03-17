package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.info.Chakra;
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
                Chakra chakra = serverPlayer.getData(NeoForgeCapabilities.CHAKRA);
                chakra.addValue(1, serverPlayer);
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 1, false, true));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
