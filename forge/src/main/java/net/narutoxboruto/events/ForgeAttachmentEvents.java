package net.narutoxboruto.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.jutsus.AbstractJutsuItem;
import net.narutoxboruto.util.JutsuGrantHelper;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ForgeAttachmentEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
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
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerDataManager.save(serverPlayer);
            PlayerDataManager.remove(serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public static void onJoinWorldSyncCap(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
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

            data.getJutsuStorage().syncToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer
                && event.getOriginal() instanceof ServerPlayer original) {

            PlayerDataManager.copyFrom(original, newPlayer);
            PlayerCapData data = PlayerDataManager.get(newPlayer);

            // Reset chakra on death
            data.getChakra().reset(newPlayer);

            // Preserve jutsu items from inventory on death
            if (event.isWasDeath()) {
                preserveJutsuItemsOnDeath(original, newPlayer);
            }
        }
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

    public static void syncAllStatsToClient(ServerPlayer player) {
        PlayerCapData data = PlayerDataManager.get(player);
        data.getNinjutsu().syncValue(player);
        data.getShurikenjutsu().syncValue(player);
        data.getKinjutsu().syncValue(player);
        data.getSummoning().syncValue(player);
        data.getMedical().syncValue(player);
        data.getKenjutsu().syncValue(player);
        data.getTaijutsu().syncValue(player);
        data.getSenjutsu().syncValue(player);
        data.getSpeed().syncValue(player);
        data.getGenjutsu().syncValue(player);
        data.getClan().syncValue(player);
        data.getShinobiPoints().syncValue(player);
    }

    @SubscribeEvent
    public static void onReplenishChakra(TickEvent.PlayerTickEvent.Pre event) {
        if (event.player instanceof ServerPlayer serverPlayer && serverPlayer.isSleepingLongEnough()) {
            Chakra chakra = PlayerDataManager.get(serverPlayer).getChakra();
            chakra.replenish(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerDataManager.save(serverPlayer);
        }
    }
}
