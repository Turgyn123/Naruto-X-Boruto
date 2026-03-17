package net.narutoxboruto.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.jutsus.AbstractJutsuItem;
import net.narutoxboruto.util.JutsuGrantHelper;

import java.util.ArrayList;
import java.util.List;

public class FabricAttachmentEvents {

    public static void register() {
        // Player logged in
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer serverPlayer = handler.getPlayer();
            PlayerDataManager.load(serverPlayer);
            PlayerCapData data = PlayerDataManager.get(serverPlayer);

            data.getAffiliation().syncValue(serverPlayer);
            data.getClan().syncValue(serverPlayer);
            data.getChakra().syncValue(serverPlayer);
            data.getMaxChakra().syncValue(serverPlayer);
            data.getShinobiPoints().syncValue(serverPlayer);
            data.getRank().syncValue(serverPlayer);
            data.getReleaseList().syncValue(serverPlayer);

            data.getGenjutsu().syncValue(serverPlayer);
            data.getKenjutsu().syncValue(serverPlayer);
            data.getKinjutsu().syncValue(serverPlayer);
            data.getMedical().syncValue(serverPlayer);
            data.getNinjutsu().syncValue(serverPlayer);
            data.getSenjutsu().syncValue(serverPlayer);
            data.getShurikenjutsu().syncValue(serverPlayer);
            data.getSpeed().syncValue(serverPlayer);
            data.getSummoning().syncValue(serverPlayer);
            data.getTaijutsu().syncValue(serverPlayer);

            JutsuGrantHelper.cleanupDuplicateJutsus(serverPlayer);
            data.getJutsuStorage().syncToClient(serverPlayer);
        });

        // Dimension change - re-sync all data to client
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            PlayerCapData data = PlayerDataManager.get(player);
            syncAllData(player, data);
        });

        // Player logged out
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer serverPlayer = handler.getPlayer();
            PlayerDataManager.save(serverPlayer);
            PlayerDataManager.remove(serverPlayer.getUUID());
        });

        // Player respawn/clone
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerDataManager.copyFrom(oldPlayer, newPlayer);
            PlayerCapData data = PlayerDataManager.get(newPlayer);

            // Reset chakra on death
            if (!alive) {
                data.getChakra().reset(newPlayer);
                preserveJutsuItemsOnDeath(oldPlayer, newPlayer);
            }
        });

        // Sync after respawn
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PlayerCapData data = PlayerDataManager.get(newPlayer);
            syncAllData(newPlayer, data);
        });

        // Server tick - replenish chakra when sleeping
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                if (serverPlayer.isSleepingLongEnough()) {
                    Chakra chakra = PlayerDataManager.get(serverPlayer).getChakra();
                    chakra.replenish(serverPlayer);
                }
            }
        });
    }

    private static void preserveJutsuItemsOnDeath(ServerPlayer original, ServerPlayer newPlayer) {
        Inventory originalInv = original.getInventory();
        List<ItemStack> jutsuItems = new ArrayList<>();

        for (int i = 0; i < originalInv.getContainerSize(); i++) {
            ItemStack stack = originalInv.getItem(i);
            if (stack.getItem() instanceof AbstractJutsuItem) {
                jutsuItems.add(stack.copy());
            }
        }

        for (ItemStack jutsu : jutsuItems) {
            if (!newPlayer.getInventory().add(jutsu)) {
                JutsuStorage storage = PlayerDataManager.get(newPlayer).getJutsuStorage();
                storage.addJutsu(jutsu);
            }
        }
    }

    public static void syncAllData(ServerPlayer player, PlayerCapData data) {
        data.getAffiliation().syncValue(player);
        data.getClan().syncValue(player);
        data.getChakra().syncValue(player);
        data.getMaxChakra().syncValue(player);
        data.getShinobiPoints().syncValue(player);
        data.getRank().syncValue(player);
        data.getReleaseList().syncValue(player);

        data.getGenjutsu().syncValue(player);
        data.getKenjutsu().syncValue(player);
        data.getKinjutsu().syncValue(player);
        data.getMedical().syncValue(player);
        data.getNinjutsu().syncValue(player);
        data.getSenjutsu().syncValue(player);
        data.getShurikenjutsu().syncValue(player);
        data.getSpeed().syncValue(player);
        data.getSummoning().syncValue(player);
        data.getTaijutsu().syncValue(player);

        data.getJutsuStorage().syncToClient(player);
    }
}
