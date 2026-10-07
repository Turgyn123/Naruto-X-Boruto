package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.util.ServerActions;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EquipDojutsuPacket implements CustomPacketPayload {

    private final String slot; // "left" or "right"
    private final String dojutsuType; // e.g. "1_tomoe_sharingan" or "" to unequip

    public static final Type<EquipDojutsuPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("narutoxboruto", "equip_dojutsu")
    );

    public static final StreamCodec<FriendlyByteBuf, EquipDojutsuPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> { buf.writeUtf(value.slot); buf.writeUtf(value.dojutsuType); },
            buf -> new EquipDojutsuPacket(buf.readUtf(), buf.readUtf())
    );

    public EquipDojutsuPacket(String slot, String dojutsuType) {
        this.slot = slot;
        this.dojutsuType = dojutsuType;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ServerActions.equipDojutsu(serverPlayer, slot, dojutsuType);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
