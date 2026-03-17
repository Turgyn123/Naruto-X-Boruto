package net.narutoxboruto.networking.jutsu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.NeoForgePlatformHelper;
import net.narutoxboruto.util.JutsuGrantHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent from client when player wants to open their Jutsu Storage.
 * Server responds by opening the JutsuStorageMenu.
 */
public class OpenJutsuStoragePacket implements CustomPacketPayload {

    public static final Type<OpenJutsuStoragePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "open_jutsu_storage")
    );

    public static final StreamCodec<FriendlyByteBuf, OpenJutsuStoragePacket> STREAM_CODEC = StreamCodec.ofMember(OpenJutsuStoragePacket::toBytes, OpenJutsuStoragePacket::new);

    public OpenJutsuStoragePacket() {}
    public OpenJutsuStoragePacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenJutsuStoragePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Clean up any duplicate jutsus before opening
                JutsuGrantHelper.cleanupDuplicateJutsus(serverPlayer);

                JutsuStorage storage = serverPlayer.getData(NeoForgeCapabilities.JUTSU_STORAGE);

                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, player) ->
                                new JutsuStorageMenu(containerId, playerInventory, NeoForgePlatformHelper.toItemStackHandler(storage)),
                        Component.translatable("container.narutoxboruto.jutsu_storage")
                ));
            }
        });
    }
}
