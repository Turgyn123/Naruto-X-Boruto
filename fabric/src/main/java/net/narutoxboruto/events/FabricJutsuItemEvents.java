package net.narutoxboruto.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.jutsus.AbstractJutsuItem;
import net.narutoxboruto.items.jutsus.LightningChakraMode;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;
import net.narutoxboruto.util.ClanItemHelper;
import net.narutoxboruto.util.JutsuGrantHelper;

public class FabricJutsuItemEvents {

    private static boolean isProtectedJutsuItem(ItemStack stack) {
        return stack.getItem() instanceof AbstractJutsuItem ||
                stack.getItem() instanceof LightningChakraMode ||
                ClanItemHelper.isClanItem(stack);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                // Every 40 ticks: verify and restore missing jutsus + clan items
                if (player.tickCount % 40 == 0) {
                    JutsuGrantHelper.verifyAndRestoreMissingJutsus(player, true);
                    ClanItemHelper.ensureFumaClanItemPresent(player);
                }

                // Check open containers for protected jutsu items
                AbstractContainerMenu container = player.containerMenu;
                boolean hasContainerOpen = container != null && !(container instanceof JutsuStorageMenu)
                        && container != player.inventoryMenu;

                if (!hasContainerOpen && player.tickCount % 20 != 0) {
                    continue;
                }

                if (hasContainerOpen) {
                    for (int i = 0; i < container.slots.size(); i++) {
                        Slot slot = container.slots.get(i);
                        ItemStack stack = slot.getItem();

                        if (isProtectedJutsuItem(stack)) {
                            if (!(slot.container instanceof Inventory)) {
                                slot.set(ItemStack.EMPTY);

                                if (!player.getInventory().add(stack)) {
                                    returnJutsuToStorage(player, stack);
                                }
                            }
                        }
                    }
                }

                // Validate jutsu items in inventory against player releases
                Inventory inventory = player.getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (stack.getItem() instanceof AbstractJutsuItem jutsu) {
                        String requiredRelease = jutsu.getRequiredRelease();

                        if (requiredRelease != null && !requiredRelease.isEmpty()) {
                            String playerReleases = PlayerDataManager.get(player).getReleaseList().getValue();
                            if (!playerReleases.toLowerCase().contains(requiredRelease.toLowerCase())) {
                                inventory.setItem(i, ItemStack.EMPTY);
                                returnJutsuToStorage(player, stack);
                            }
                        }
                    }
                }
            }
        });
    }

    public static void returnJutsuToStorage(ServerPlayer serverPlayer, ItemStack stack) {
        JutsuStorage storage = PlayerDataManager.get(serverPlayer).getJutsuStorage();

        if (!storage.hasJutsu(stack.getItem().getClass())) {
            storage.addJutsu(stack);
        }
    }
}
