package net.narutoxboruto.networking.info;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.info.Dojutsu;
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
                Dojutsu dojutsu = serverPlayer.getData(NeoForgeCapabilities.DOJUTSU);
                if (slot.startsWith("offset_")) {
                    String[] parts = dojutsuType.split(",");
                    if (parts.length == 2) {
                        int ox = Integer.parseInt(parts[0]);
                        int oy = Integer.parseInt(parts[1]);
                        if ("offset_left".equals(slot)) {
                            dojutsu.setLeftEyeOffset(ox, oy);
                        } else if ("offset_right".equals(slot)) {
                            dojutsu.setRightEyeOffset(ox, oy);
                        }
                    }
                } else if ("scale".equals(slot)) {
                    dojutsu.setEyeScale(Float.parseFloat(dojutsuType));
                } else if ("hide".equals(slot)) {
                    dojutsu.setEyesVisible(false);
                } else if ("show".equals(slot)) {
                    dojutsu.setEyesVisible(true);
                } else if ("reset".equals(slot)) {
                    dojutsu.resetEyeVisuals();
                } else if ("left".equals(slot)) {
                    dojutsu.setLeftEye(dojutsuType);
                } else if ("right".equals(slot)) {
                    dojutsu.setRightEye(dojutsuType);
                }
                serverPlayer.setData(NeoForgeCapabilities.DOJUTSU, dojutsu);
                dojutsu.syncValue(serverPlayer);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
