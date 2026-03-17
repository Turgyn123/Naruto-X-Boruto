package net.narutoxboruto.networking.jutsu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.jutsus.AbstractJutsuItem;
import net.narutoxboruto.items.jutsus.LightningChakraMode;
import net.narutoxboruto.menu.ForgeMenuTypes;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class JutsuStorageMenu extends AbstractContainerMenu {
    private static final int STORAGE_ROWS = 6;
    private static final int STORAGE_COLS = 9;
    private static final int STORAGE_SIZE = STORAGE_ROWS * STORAGE_COLS;

    private final ItemStackHandler storageHandler;
    private final Player player;
    private int currentPage = 0;

    public JutsuStorageMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new ItemStackHandler(STORAGE_SIZE));
    }

    public JutsuStorageMenu(int containerId, Inventory playerInventory, ItemStackHandler storageHandler) {
        super(ForgeMenuTypes.JUTSU_STORAGE.get(), containerId);
        this.player = playerInventory.player;
        this.storageHandler = storageHandler;

        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLS; col++) {
                this.addSlot(new JutsuSlot(storageHandler, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    private static class JutsuSlot extends SlotItemHandler {
        public JutsuSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof AbstractJutsuItem
                    || stack.getItem() instanceof LightningChakraMode;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index < STORAGE_SIZE) {
                if (!this.moveItemStackTo(stackInSlot, STORAGE_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.getItem() instanceof AbstractJutsuItem
                        || stackInSlot.getItem() instanceof LightningChakraMode) {
                    if (!this.moveItemStackTo(stackInSlot, 0, STORAGE_SIZE, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void setCurrentPage(int page) { this.currentPage = page; }
    public int getMaxPages() { return 1; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            JutsuStorage storage = PlayerDataManager.get(player).getJutsuStorage();
            for (int i = 0; i < STORAGE_SIZE; i++) {
                storage.setItem(i, storageHandler.getStackInSlot(i));
            }
        }
    }
}
