package net.narutoxboruto.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;


public class JutsuGrantHelper {

    public static void grantJutsusForReleases(ServerPlayer serverPlayer, String releaseList) {
        if (releaseList == null || releaseList.isEmpty()) return;
        String[] releases = releaseList.split(",");
        for (String release : releases) {
            grantJutsuForRelease(serverPlayer, release.trim());
        }
    }

    public static void grantJutsuForRelease(ServerPlayer serverPlayer, String releaseType) {
        JutsuStorage storage = Services.PLATFORM.getJutsuStorage(serverPlayer);

        switch (releaseType.toLowerCase()) {
            case "fire":
                grantSingleJutsu(serverPlayer, storage,
                        ModItems.FIRE_BALL_JUTSU, "Fire Ball");
                break;
            case "earth":
                grantMultipleJutsus(serverPlayer, storage,
                        ModItems.EARTH_WALL_JUTSU, "Earth Wall",
                        ModItems.EARTH_WAVE_JUTSU, "Earth Wave");
                break;
            case "water":
                grantMultipleJutsus(serverPlayer, storage,
                        ModItems.WATER_PRISON_JUTSU, "Water Prison",
                        ModItems.SHARK_BOMB_JUTSU, "Shark Bomb",
                        ModItems.WATER_DRAGON_JUTSU, "Water Dragon");
                break;
            case "lightning":
                grantSingleJutsu(serverPlayer, storage,
                        ModItems.LIGHTING_CHAKRA_MODE, "Lightning Chakra Mode");
                break;
        }
    }

    private static ItemStack fromRegistry(String itemId) {
        return new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, itemId)
        ));
    }

    private static void grantSingleJutsu(ServerPlayer serverPlayer, JutsuStorage storage,
                                         String itemId, String name) {
        ItemStack stack = fromRegistry(itemId);
        if (storage.addJutsuIfNotOwned(stack, serverPlayer)) {
            Services.PLATFORM.setJutsuStorage(serverPlayer, storage);
            storage.syncToClient(serverPlayer);
            sendMasteryMessage(serverPlayer, name, true);
        }
    }

    private static void grantMultipleJutsus(ServerPlayer serverPlayer, JutsuStorage storage,
                                            String itemId1, String name1, String itemId2, String name2) {
        boolean granted1 = storage.addJutsuIfNotOwned(fromRegistry(itemId1), serverPlayer);
        boolean granted2 = storage.addJutsuIfNotOwned(fromRegistry(itemId2), serverPlayer);

        if (granted1 || granted2) {
            Services.PLATFORM.setJutsuStorage(serverPlayer, storage);
            storage.syncToClient(serverPlayer);
        }

        sendMasteryMessage(serverPlayer, name1, granted1);
        sendMasteryMessage(serverPlayer, name2, granted2);
    }

    private static void grantMultipleJutsus(ServerPlayer serverPlayer, JutsuStorage storage,
                                            String itemId1, String name1, String itemId2, String name2, String itemId3, String name3) {
        boolean granted1 = storage.addJutsuIfNotOwned(fromRegistry(itemId1), serverPlayer);
        boolean granted2 = storage.addJutsuIfNotOwned(fromRegistry(itemId2), serverPlayer);
        boolean granted3 = storage.addJutsuIfNotOwned(fromRegistry(itemId3), serverPlayer);

        if (granted1 || granted2 || granted3) {
            Services.PLATFORM.setJutsuStorage(serverPlayer, storage);
            storage.syncToClient(serverPlayer);
        }

        sendMasteryMessage(serverPlayer, name1, granted1);
        sendMasteryMessage(serverPlayer, name2, granted2);
        sendMasteryMessage(serverPlayer, name3, granted3);
    }

    private static void sendMasteryMessage(ServerPlayer serverPlayer, String jutsuName, boolean granted) {
        if (granted) {
            serverPlayer.sendSystemMessage(
                    Component.literal("You mastered ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(jutsuName).withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(", (Press ").withStyle(ChatFormatting.GREEN))
                            .append(Component.literal("Z").withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(")").withStyle(ChatFormatting.GREEN))
            );
        }
    }

    public static void cleanupDuplicateJutsus(ServerPlayer serverPlayer) {
        JutsuStorage storage = Services.PLATFORM.getJutsuStorage(serverPlayer);
        int removed = storage.removeDuplicates(serverPlayer);

        if (removed > 0) {
            Services.PLATFORM.setJutsuStorage(serverPlayer, storage);
            storage.syncToClient(serverPlayer);
        }
    }

    public static void verifyAndRestoreMissingJutsus(ServerPlayer serverPlayer, boolean checkInventory) {
        String playerReleases = Services.PLATFORM.getReleaseList(serverPlayer).getValue();
        if (playerReleases == null || playerReleases.isEmpty()) return;

        JutsuStorage storage = Services.PLATFORM.getJutsuStorage(serverPlayer);
        boolean modified = false;

        if (playerReleases.toLowerCase().contains("fire")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.FIRE_BALL_JUTSU, checkInventory);
        }

        if (playerReleases.toLowerCase().contains("earth")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.EARTH_WALL_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.EARTH_WAVE_JUTSU, checkInventory);
        }

        if (playerReleases.toLowerCase().contains("water")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.WATER_PRISON_JUTSU, checkInventory);
        }

        if (playerReleases.toLowerCase().contains("lightning")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.LIGHTING_CHAKRA_MODE, checkInventory);
        }

        if (modified) {
            Services.PLATFORM.setJutsuStorage(serverPlayer, storage);
            storage.syncToClient(serverPlayer);
        }
    }

    private static boolean restoreIfMissing(ServerPlayer player, JutsuStorage storage,
                                            String itemId, boolean checkInventory) {
        Item item = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, itemId)
        );
        if (!hasJutsuAnywhere(player, storage, item, checkInventory)) {
            storage.addJutsu(new ItemStack(item));
            return true;
        }
        return false;
    }

    private static boolean hasJutsuAnywhere(ServerPlayer player, JutsuStorage storage,
                                            Item jutsuItem, boolean checkInventory) {
        if (storage.hasJutsu(jutsuItem.getClass())) return true;

        if (checkInventory) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).getItem() == jutsuItem) return true;
            }
        }
        return false;
    }
}
