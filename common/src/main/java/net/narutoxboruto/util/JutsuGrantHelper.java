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

        // Everyone who can mold a nature of chakra can learn the Rasengan, which needs none
        grantSingleJutsu(serverPlayer, storage, ModItems.RASENGAN_JUTSU, "Rasengan");

        switch (releaseType.toLowerCase()) {
            case "fire":
                grantMultipleJutsus(serverPlayer, storage,
                        ModItems.FLAME_BULLET_JUTSU, "Flame Bullet",
                        ModItems.FIRE_BALL_JUTSU, "Great Fireball Technique",
                        ModItems.GREAT_FIRE_ANNIHILATION_JUTSU, "Great Fire Annihilation",
                        ModItems.ASH_PILE_BURNING_JUTSU, "Fire Release: Ash Pile Burning");
                break;
            case "earth":
                grantMultipleJutsus(serverPlayer, storage,
                        ModItems.EARTH_WALL_JUTSU, "Earth Wall",
                        ModItems.EARTH_WAVE_JUTSU, "Earth Wave",
                        ModItems.EARTH_SANDWICH_JUTSU, "Earth Sandwich",
                        ModItems.EARTH_SPEAR_JUTSU, "Earth Spear");
                break;
            case "water":
                grantMultipleJutsus(serverPlayer, storage,
                        ModItems.WATER_PRISON_JUTSU, "Water Prison",
                        ModItems.SHARK_BOMB_JUTSU, "Shark Bomb",
                        ModItems.WATER_DRAGON_JUTSU, "Water Dragon",
                        ModItems.WATER_CLONE_JUTSU, "Water Clone Technique");
                break;
            case "lightning":
                grantSingleJutsu(serverPlayer, storage,
                        ModItems.LIGHTNING_CHAKRA_MODE, "Lightning Chakra Mode");
                break;
            case "wind":
                grantMultipleJutsus(serverPlayer, storage,
                        ModItems.RASENSHURIKEN_JUTSU, "Rasenshuriken",
                        ModItems.VACUUM_SPHERE_JUTSU, "Vacuum Sphere",
                        ModItems.GREAT_BREAKTHROUGH_JUTSU, "Great Breakthrough");
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

    /** Any number of jutsus, as pairs of an item id and a name. */
    private static void grantMultipleJutsus(ServerPlayer serverPlayer, JutsuStorage storage, String... idsAndNames) {
        boolean any = false;
        boolean[] granted = new boolean[idsAndNames.length / 2];
        for (int i = 0; i < granted.length; i++) {
            granted[i] = storage.addJutsuIfNotOwned(fromRegistry(idsAndNames[i * 2]), serverPlayer);
            any |= granted[i];
        }
        if (any) {
            Services.PLATFORM.setJutsuStorage(serverPlayer, storage);
            storage.syncToClient(serverPlayer);
        }
        for (int i = 0; i < granted.length; i++) {
            sendMasteryMessage(serverPlayer, idsAndNames[i * 2 + 1], granted[i]);
        }
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

        // The Rasengan is for everyone with a release
        modified |= restoreIfMissing(serverPlayer, storage, ModItems.RASENGAN_JUTSU, checkInventory);

        if (playerReleases.toLowerCase().contains("wind")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.RASENSHURIKEN_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.VACUUM_SPHERE_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.GREAT_BREAKTHROUGH_JUTSU, checkInventory);
        }

        if (playerReleases.toLowerCase().contains("fire")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.FLAME_BULLET_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.FIRE_BALL_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.GREAT_FIRE_ANNIHILATION_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.ASH_PILE_BURNING_JUTSU, checkInventory);
        }

        if (playerReleases.toLowerCase().contains("earth")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.EARTH_WALL_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.EARTH_WAVE_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.EARTH_SANDWICH_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.EARTH_SPEAR_JUTSU, checkInventory);
        }

        if (playerReleases.toLowerCase().contains("water")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.WATER_PRISON_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.SHARK_BOMB_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.WATER_DRAGON_JUTSU, checkInventory);
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.WATER_CLONE_JUTSU, checkInventory);
        }

        if (playerReleases.toLowerCase().contains("lightning")) {
            modified |= restoreIfMissing(serverPlayer, storage, ModItems.LIGHTNING_CHAKRA_MODE, checkInventory);
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
