package net.narutoxboruto.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.jutsus.AbstractJutsuItem;
import net.narutoxboruto.items.jutsus.LightningChakraMode;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;
import net.narutoxboruto.util.ClanItemHelper;
import net.narutoxboruto.util.JutsuGrantHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Iterator;

public class ForgeJutsuItemEvents {

    private static boolean isProtectedJutsuItem(ItemStack stack) {
        return stack.getItem() instanceof AbstractJutsuItem ||
                stack.getItem() instanceof LightningChakraMode ||
                ClanItemHelper.isClanItem(stack);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        Player player = event.getPlayer();

        if (isProtectedJutsuItem(stack)) {
            event.setCanceled(true);
            event.getEntity().discard();
            ItemStack returnStack = stack.copy();

            if (!player.getInventory().add(returnStack)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    returnJutsuToStorage(serverPlayer, returnStack);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        AbstractContainerMenu container = event.getContainer();

        if (container instanceof JutsuStorageMenu) {
            return;
        }

        for (int i = 0; i < container.slots.size(); i++) {
            Slot slot = container.slots.get(i);
            ItemStack stack = slot.getItem();

            if (isProtectedJutsuItem(stack)) {
                if (!(slot.container instanceof Inventory)) {
                    slot.set(ItemStack.EMPTY);

                    if (!serverPlayer.getInventory().add(stack)) {
                        returnJutsuToStorage(serverPlayer, stack);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();
            ItemStack stack = itemEntity.getItem();

            if (isProtectedJutsuItem(stack)) {
                iterator.remove();

                JutsuStorage storage = PlayerDataManager.get(player).getJutsuStorage();
                storage.addJutsu(stack);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % 40 == 0) {
            JutsuGrantHelper.verifyAndRestoreMissingJutsus(player, true);
            ClanItemHelper.ensureFumaClanItemPresent(player);
        }

        AbstractContainerMenu container = player.containerMenu;
        boolean hasContainerOpen = container != null && !(container instanceof JutsuStorageMenu)
                && container != player.inventoryMenu;

        if (!hasContainerOpen && player.tickCount % 20 != 0) {
            return;
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

    private static void returnJutsuToStorage(ServerPlayer player, ItemStack jutsuStack) {
        JutsuStorage storage = PlayerDataManager.get(player).getJutsuStorage();

        if (!storage.hasJutsu(jutsuStack.getItem().getClass())) {
            storage.addJutsu(jutsuStack);
        }

        storage.syncToClient(player);
    }
}
