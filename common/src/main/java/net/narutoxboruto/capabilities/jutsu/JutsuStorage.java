package net.narutoxboruto.capabilities.jutsu;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.main.platform.Services;

import java.util.HashSet;
import java.util.Set;

public class JutsuStorage {
    public static final int STORAGE_SIZE = 54;

    public static final Codec<JutsuStorage> CODEC = CompoundTag.CODEC.xmap(
            JutsuStorage::fromNbt,
            JutsuStorage::toNbt
    );

    private final NonNullList<ItemStack> items;

    public JutsuStorage() {
        this.items = NonNullList.withSize(STORAGE_SIZE, ItemStack.EMPTY);
    }

    public JutsuStorage(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public static JutsuStorage fromNbt(CompoundTag tag) {
        JutsuStorage storage = new JutsuStorage();
        if (tag.contains("Items", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot < STORAGE_SIZE) {
                    String itemId = itemTag.getString("ItemId");
                    int count = itemTag.contains("Count") ? itemTag.getInt("Count") : 1;
                    if (!itemId.isEmpty()) {
                        ResourceLocation loc = ResourceLocation.tryParse(itemId);
                        if (loc != null) {
                            Item item = BuiltInRegistries.ITEM.get(loc);
                            if (item != null) {
                                storage.items.set(slot, new ItemStack(item, count));
                            }
                        }
                    }
                }
            }
        }
        return storage;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (int i = 0; i < STORAGE_SIZE; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                itemTag.putString("ItemId", itemId.toString());
                itemTag.putInt("Count", stack.getCount());
                listTag.add(itemTag);
            }
        }
        tag.put("Items", listTag);
        return tag;
    }

    public NonNullList<ItemStack> getItems() { return items; }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= STORAGE_SIZE) return ItemStack.EMPTY;
        return items.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < STORAGE_SIZE) {
            items.set(slot, stack);
        }
    }

    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    public boolean addJutsu(ItemStack stack) {
        if (hasJutsuByItem(stack.getItem())) return false;
        for (int i = 0; i < STORAGE_SIZE; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    public boolean addJutsuIfNotOwned(ItemStack stack, Player player) {
        Item jutsuItem = stack.getItem();
        if (hasJutsuByItem(jutsuItem)) return false;
        if (playerHasJutsuInInventory(player, jutsuItem)) return false;
        return addJutsu(stack);
    }

    public static boolean playerHasJutsuInInventory(Player player, Item jutsuItem) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == jutsuItem) return true;
        }
        return false;
    }

    public boolean hasJutsuByItem(Item jutsuItem) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() == jutsuItem) return true;
        }
        return false;
    }

    public boolean hasJutsu(Class<?> jutsuItemClass) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && jutsuItemClass.isInstance(stack.getItem())) return true;
        }
        return false;
    }

    public int removeDuplicates(Player player) {
        Set<Item> seenJutsus = new HashSet<>();
        int removed = 0;

        if (player != null) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty()) seenJutsus.add(stack.getItem());
            }
        }

        for (int i = 0; i < STORAGE_SIZE; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (seenJutsus.contains(item)) {
                    items.set(i, ItemStack.EMPTY);
                    removed++;
                } else {
                    seenJutsus.add(item);
                    if (stack.getCount() > 1) items.set(i, stack.copyWithCount(1));
                }
            }
        }

        if (removed > 0) compactStorage();
        return removed;
    }

    private void compactStorage() {
        int writeIndex = 0;
        for (int readIndex = 0; readIndex < STORAGE_SIZE; readIndex++) {
            ItemStack stack = items.get(readIndex);
            if (!stack.isEmpty()) {
                if (writeIndex != readIndex) {
                    items.set(writeIndex, stack);
                    items.set(readIndex, ItemStack.EMPTY);
                }
                writeIndex++;
            }
        }
    }

    public void copyFrom(JutsuStorage source) {
        for (int i = 0; i < STORAGE_SIZE; i++) {
            this.items.set(i, source.items.get(i).copy());
        }
    }

    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    public void syncToClient(ServerPlayer player) {
        Services.PLATFORM.syncJutsuStorage(player, this.toNbt());
    }
}
