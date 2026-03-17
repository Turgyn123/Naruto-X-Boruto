package net.narutoxboruto.items.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.main.Main;

import java.util.AbstractMap;
import java.util.List;
import java.util.function.Supplier;

public class RandomDna extends Item {

    public RandomDna(Properties properties) {
        super(properties);
    }

    // Shared logic for item use
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player == null || level.isClientSide()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            // Custom logic for random item giving
            handleRandomItemGive(player);

            // Reduce the stack size of the original item
            heldItem.shrink(1);

            // Indicate successful use and return the modified item
            return InteractionResultHolder.success(heldItem);
        }

        return InteractionResultHolder.pass(heldItem);
    }

    private void handleRandomItemGive(Player player) {
        RandomSource random = RandomSource.create();
        float chance = random.nextFloat();

        // Probability thresholds and corresponding items
        List<AbstractMap.SimpleEntry<Float, Supplier<Item>>> weightedItems = List.of(
                new AbstractMap.SimpleEntry<>(25f, () -> itemFromKey(ModItems.EARTH_DNA)),
                new AbstractMap.SimpleEntry<>(25f, () -> itemFromKey(ModItems.WATER_DNA)),
                new AbstractMap.SimpleEntry<>(25f, () -> itemFromKey(ModItems.LIGHTNING_DNA)),
                new AbstractMap.SimpleEntry<>(25f, () -> itemFromKey(ModItems.FIRE_DNA)),
                new AbstractMap.SimpleEntry<>(25f, () -> itemFromKey(ModItems.WIND_DNA)),
                new AbstractMap.SimpleEntry<>(15f, () -> itemFromKey(ModItems.YANG_DNA)),
                new AbstractMap.SimpleEntry<>(15f, () -> itemFromKey(ModItems.YIN_DNA))
        );

        // Calculate the total weight
        float totalWeight = 0f;
        for (AbstractMap.SimpleEntry<Float, Supplier<Item>> entry : weightedItems) {
            totalWeight += entry.getKey();
        }
        float accumulatedChance = 0f;
        for (AbstractMap.SimpleEntry<Float, Supplier<Item>> entry : weightedItems) {
            accumulatedChance += entry.getKey() / totalWeight; // Use getKey() for the probability
            if (chance < accumulatedChance) {
                // Add the item to the player's inventory
                ItemStack newItem = new ItemStack(entry.getValue().get()); // Use getValue() for the item
                player.addItem(newItem);
                break;
            }
        }
    }

    private static Item itemFromKey(String key) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, key));
    }
}
