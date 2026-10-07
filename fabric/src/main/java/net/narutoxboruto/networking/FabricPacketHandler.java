package net.narutoxboruto.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;
import net.narutoxboruto.util.JutsuGrantHelper;
import net.narutoxboruto.util.ServerActions;

public class FabricPacketHandler {

    public static void registerServer() {
        // Register payload types (both sides need these in Fabric 1.21.1)
        PayloadTypeRegistry.playS2C().register(SyncIntData.TYPE, SyncIntData.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncStringData.TYPE, SyncStringData.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncBoolData.TYPE, SyncBoolData.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncNbtData.TYPE, SyncNbtData.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerActionPacket.TYPE, ServerActionPacket.STREAM_CODEC);

        // Register serverbound handler
        ServerPlayNetworking.registerGlobalReceiver(ServerActionPacket.TYPE, (payload, context) -> {
            ServerPlayer serverPlayer = context.player();
            context.server().execute(() -> handleServerAction(payload.action(), serverPlayer));
        });
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncIntData.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var player = context.client().player;
                if (player != null) {
                    PlayerDataManager.get(player).setIntValue(payload.key(), payload.value());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncStringData.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var player = context.client().player;
                if (player != null) {
                    PlayerDataManager.get(player).setStringValue(payload.key(), payload.value());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncBoolData.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var player = context.client().player;
                if (player != null) {
                    PlayerDataManager.get(player).setBoolValue(payload.key(), payload.value());
                    switch (payload.key()) {
                        case "kiba_active" -> PlayerData.setKibaActive(payload.value());
                        case "lightning_chakra_mode_active" -> PlayerData.setLightningChakraModeActive(payload.value());
                        case "chakra_control" -> PlayerData.setChakraControlActive(payload.value());
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncNbtData.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var player = context.client().player;
                if (player != null) {
                    if ("jutsu_storage".equals(payload.key())) {
                        PlayerDataManager.get(player).setJutsuStorage(JutsuStorage.fromNbt(payload.nbt()));
                    } else if ("dojutsu".equals(payload.key())) {
                        net.narutoxboruto.capabilities.info.Dojutsu dojutsu = net.narutoxboruto.capabilities.info.Dojutsu.fromNbt(payload.nbt());
                        PlayerDataManager.get(player).setDojutsu(dojutsu);
                        PlayerData.setDojutsuUnlockedList(dojutsu.getUnlockedListRaw());
                        PlayerData.setDojutsuLeftEye(dojutsu.getLeftEye());
                        PlayerData.setDojutsuRightEye(dojutsu.getRightEye());
                        PlayerData.setDojutsuTimer(dojutsu.getTimer());
                    }
                }
            });
        });
    }

    public static void sendToPlayer(ServerPlayer player, Object payload) {
        if (payload instanceof SyncIntData p) ServerPlayNetworking.send(player, p);
        else if (payload instanceof SyncStringData p) ServerPlayNetworking.send(player, p);
        else if (payload instanceof SyncBoolData p) ServerPlayNetworking.send(player, p);
        else if (payload instanceof SyncNbtData p) ServerPlayNetworking.send(player, p);
    }

    public static void sendToServer(Object payload) {
        if (payload instanceof ServerActionPacket p) ClientPlayNetworking.send(p);
    }

    private static void handleServerAction(String action, ServerPlayer serverPlayer) {
        if ("open_jutsu_storage".equals(action)) {
            JutsuGrantHelper.cleanupDuplicateJutsus(serverPlayer);
            JutsuStorage storage = PlayerDataManager.get(serverPlayer).getJutsuStorage();
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, player) ->
                            new JutsuStorageMenu(containerId, playerInventory, storage),
                    Component.translatable("container.narutoxboruto.jutsu_storage")
            ));
            return;
        }

        // Everything else is shared with the other loaders and validated there.
        ServerActions.handleAction(serverPlayer, action);
    }
}
