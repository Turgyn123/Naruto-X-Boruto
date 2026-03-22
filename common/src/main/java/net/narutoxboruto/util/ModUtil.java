package net.narutoxboruto.util;

import net.narutoxboruto.main.platform.Services;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import javax.management.Attribute;
import java.util.*;

public class ModUtil {
    public static final List<String> CLAN_LIST = Arrays.asList("fuma", "nara", "shiin", "shirogane", "uzumaki",
            "uchiha", "hyuuga", "chinoike");

    // Weighted clan map for random selection (total weight: 800)
    // uchiha/hyuuga/uzumaki = 5% each (40), chinoike = 7.5% (60), others = 19.375% each (155)
    public static final Map<String, Integer> CLAN_MAP = new LinkedHashMap<>();
    static {
        CLAN_MAP.put("fuma", 155);
        CLAN_MAP.put("nara", 155);
        CLAN_MAP.put("shiin", 155);
        CLAN_MAP.put("shirogane", 155);
        CLAN_MAP.put("uzumaki", 40);
        CLAN_MAP.put("uchiha", 40);
        CLAN_MAP.put("hyuuga", 40);
        CLAN_MAP.put("chinoike", 60);
    }
    public static final List<String> RANK_LIST = Arrays.asList("civilian", "student", "genin", "chuunin", "jounin",
            "special_jounin", "anbu", "sage", "kage", "rogue");

    public static final List<String> AFF_LIST = Arrays.asList("cloud", "leaf", "mist", "rain", "sand", "sound",
            "stone");
    public static final List<String> STAT_LIST = Arrays.asList("taijutsu", "ninjutsu", "genjutsu", "kenjutsu",
            "kinjutsu", "medical", "senjutsu", "shurikenjutsu", "speed", "summoning");

    public static final Random RANDOM = new Random();

    /**
     * Selects a random clan using weighted probabilities from CLAN_MAP.
     */
    public static String getWeightedRandomClan() {
        int totalWeight = CLAN_MAP.values().stream().mapToInt(Integer::intValue).sum();
        int roll = RANDOM.nextInt(totalWeight);
        int cumulative = 0;
        for (Map.Entry<String, Integer> entry : CLAN_MAP.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        return CLAN_LIST.get(0); // fallback
    }

    public static final List<String> RELEASES_LIST = Arrays.asList("earth", "fire", "lightning", "water", "wind",
            "yang", "yin");

    /**
     * Get the chakra growth multiplier for a player based on their clan.
     * Uzumaki clan gets 3x multiplier (15 per point instead of 5).
     * @param serverPlayer The player to check
     * @return The multiplier (1 for normal, 3 for Uzumaki)
     */
    public static int getChakraGrowthMultiplier(ServerPlayer serverPlayer) {
        String clan = Services.PLATFORM.getClan(serverPlayer).getValue();
        return "uzumaki".equals(clan) ? 3 : 1;
    }

    /**
     * Ensures current chakra doesn't exceed max chakra.
     * Call this after any operation that might change max chakra.
     * @param serverPlayer The player to cap chakra for
     */
    /**
     * Ensures current chakra doesn't exceed max chakra.
     */
    public static void capChakraToMax(ServerPlayer serverPlayer) {
        var maxChakra = Services.PLATFORM.getMaxChakra(serverPlayer);
        var currentChakra = Services.PLATFORM.getChakra(serverPlayer);
        if (maxChakra != null && currentChakra != null && currentChakra.getValue() > maxChakra.getValue()) {
            currentChakra.setValue(maxChakra.getValue());
            Services.PLATFORM.syncChakra(serverPlayer, currentChakra.getValue());
        }
    }

    /**
     * Recalculates max chakra when switching to/from Uzumaki clan.
     */
    private static void recalculateMaxChakraForClanChange(ServerPlayer serverPlayer, boolean leavingUzumaki) {
        int ninjutsuValue = Services.PLATFORM.getNinjutsu(serverPlayer).getValue();
        int chakraDifference = ninjutsuValue * 10;
        if (chakraDifference > 0) {
            var maxChakra = Services.PLATFORM.getMaxChakra(serverPlayer);
            var currentChakra = Services.PLATFORM.getChakra(serverPlayer);
            if (leavingUzumaki) {
                maxChakra.subValue(chakraDifference, serverPlayer);
                if (currentChakra.getValue() > maxChakra.getValue()) {
                    currentChakra.setValue(maxChakra.getValue());
                }
            } else {
                maxChakra.addValue(chakraDifference, serverPlayer);
            }
            Services.PLATFORM.syncMaxChakra(serverPlayer, maxChakra.getValue());
            Services.PLATFORM.syncChakra(serverPlayer, currentChakra.getValue());
        }
    }

    public static void giveClanStatBonuses(ServerPlayer serverPlayer) {
        String clan = Services.PLATFORM.getClan(serverPlayer).getValue();
        switch (clan) {
            case "fuma" -> {
                Services.PLATFORM.getShurikenjutsu(serverPlayer).incrementValue(25, serverPlayer);
                serverPlayer.addItem(new net.minecraft.world.item.ItemStack(net.narutoxboruto.items.ModItems.FUMA_SHURIKEN_ITEM));
            }
            case "nara" -> {
                Services.PLATFORM.getNinjutsu(serverPlayer).incrementValue(15, serverPlayer);
                Services.PLATFORM.getShurikenjutsu(serverPlayer).incrementValue(10, serverPlayer);
                Services.PLATFORM.getKinjutsu(serverPlayer).incrementValue(5, serverPlayer);
            }
            case "shiin" -> {
                Services.PLATFORM.getKinjutsu(serverPlayer).incrementValue(15, serverPlayer);
            }
            case "shirogane" -> {
                Services.PLATFORM.getSummoning(serverPlayer).incrementValue(20, serverPlayer);
                Services.PLATFORM.getNinjutsu(serverPlayer).incrementValue(10, serverPlayer);
            }
            case "uzumaki" -> {
                Services.PLATFORM.getNinjutsu(serverPlayer).incrementValue(15, serverPlayer);
                Services.PLATFORM.getMedical(serverPlayer).incrementValue(10, serverPlayer);
                Services.PLATFORM.getKenjutsu(serverPlayer).incrementValue(5, serverPlayer);
                recalculateMaxChakraForClanChange(serverPlayer, false);
            }
            case "uchiha" -> {
                Services.PLATFORM.getGenjutsu(serverPlayer).incrementValue(20, serverPlayer);
                Services.PLATFORM.getKenjutsu(serverPlayer).incrementValue(10, serverPlayer);
                Services.PLATFORM.getTaijutsu(serverPlayer).incrementValue(10, serverPlayer);
            }
            case "hyuuga" -> {
                Services.PLATFORM.getTaijutsu(serverPlayer).incrementValue(15, serverPlayer);
                Services.PLATFORM.getGenjutsu(serverPlayer).incrementValue(20, serverPlayer);
                Services.PLATFORM.getMedical(serverPlayer).incrementValue(5, serverPlayer);
            }
            case "chinoike" -> {
                Services.PLATFORM.getGenjutsu(serverPlayer).incrementValue(20, serverPlayer);
                Services.PLATFORM.getKenjutsu(serverPlayer).incrementValue(5, serverPlayer);
            }
        }
        // Reset dojutsu timer for the new clan
        var dojutsu = Services.PLATFORM.getDojutsu(serverPlayer);
        dojutsu.resetTimer();
        dojutsu.syncValue(serverPlayer);
        syncAllStatsToClient(serverPlayer);
    }

    public static void removeClanStatBonuses(ServerPlayer serverPlayer) {
        String clan = Services.PLATFORM.getClan(serverPlayer).getValue();
        if ("uzumaki".equals(clan)) {
            recalculateMaxChakraForClanChange(serverPlayer, true);
        }
        if ("fuma".equals(clan)) {
            ClanItemHelper.removeClanItems(serverPlayer);
        }
        switch (clan) {
            case "fuma" -> {
                Services.PLATFORM.getShurikenjutsu(serverPlayer).subValue(25, serverPlayer);
            }
            case "nara" -> {
                Services.PLATFORM.getNinjutsu(serverPlayer).subValue(15, serverPlayer);
                Services.PLATFORM.getShurikenjutsu(serverPlayer).subValue(10, serverPlayer);
                Services.PLATFORM.getKinjutsu(serverPlayer).subValue(5, serverPlayer);
            }
            case "shiin" -> {
                Services.PLATFORM.getKinjutsu(serverPlayer).subValue(15, serverPlayer);
            }
            case "shirogane" -> {
                Services.PLATFORM.getSummoning(serverPlayer).subValue(20, serverPlayer);
                Services.PLATFORM.getNinjutsu(serverPlayer).subValue(10, serverPlayer);
            }
            case "uzumaki" -> {
                Services.PLATFORM.getNinjutsu(serverPlayer).subValue(15, serverPlayer);
                Services.PLATFORM.getMedical(serverPlayer).subValue(10, serverPlayer);
                Services.PLATFORM.getKenjutsu(serverPlayer).subValue(5, serverPlayer);
            }
            case "uchiha" -> {
                Services.PLATFORM.getGenjutsu(serverPlayer).subValue(20, serverPlayer);
                Services.PLATFORM.getKenjutsu(serverPlayer).subValue(10, serverPlayer);
                Services.PLATFORM.getTaijutsu(serverPlayer).subValue(10, serverPlayer);
            }
            case "hyuuga" -> {
                Services.PLATFORM.getTaijutsu(serverPlayer).subValue(15, serverPlayer);
                Services.PLATFORM.getGenjutsu(serverPlayer).subValue(20, serverPlayer);
                Services.PLATFORM.getMedical(serverPlayer).subValue(5, serverPlayer);
            }
            case "chinoike" -> {
                Services.PLATFORM.getGenjutsu(serverPlayer).subValue(20, serverPlayer);
                Services.PLATFORM.getKenjutsu(serverPlayer).subValue(5, serverPlayer);
            }
        }
        syncAllStatsToClient(serverPlayer);
    }

    /**
     * Syncs all stats from server to client via platform-specific packets.
     */
    public static void syncAllStatsToClient(ServerPlayer player) {
        Services.PLATFORM.syncNinjutsu(player, Services.PLATFORM.getNinjutsu(player).getValue());
        Services.PLATFORM.syncShurikenjutsu(player, Services.PLATFORM.getShurikenjutsu(player).getValue());
        Services.PLATFORM.syncKinjutsu(player, Services.PLATFORM.getKinjutsu(player).getValue());
        Services.PLATFORM.syncSummoning(player, Services.PLATFORM.getSummoning(player).getValue());
        Services.PLATFORM.syncMedical(player, Services.PLATFORM.getMedical(player).getValue());
        Services.PLATFORM.syncKenjutsu(player, Services.PLATFORM.getKenjutsu(player).getValue());
        Services.PLATFORM.syncTaijutsu(player, Services.PLATFORM.getTaijutsu(player).getValue());
        Services.PLATFORM.syncSenjutsu(player, Services.PLATFORM.getSenjutsu(player).getValue());
        Services.PLATFORM.syncSpeed(player, Services.PLATFORM.getSpeed(player).getValue());
        Services.PLATFORM.syncGenjutsu(player, Services.PLATFORM.getGenjutsu(player).getValue());
        Services.PLATFORM.syncClan(player, Services.PLATFORM.getClan(player).getValue());
        Services.PLATFORM.syncShinobiPoints(player, Services.PLATFORM.getShinobiPoints(player).getValue());
    }

    public static int getPlayerStatistics(ServerPlayer serverPlayer, ResourceLocation stat) {
        return serverPlayer.getStats().getValue(Stats.CUSTOM.get(stat));
    }

    public static String concatAndFormat(String pList, String value) {
        return (pList + (pList.isEmpty() ? "" : ", ") + value).toLowerCase();
    }

    public static List<String> getArrayFrom(String s) {
        return List.of(s.replace(" ", "").split(","));
    }

    public static void displayTranslatableMessage(ServerPlayer serverPlayer, String msg, String msg2, String s, boolean b) {
        Component message = Component.translatable("msg.narutoxboruto." + msg,
                b ? s : Component.translatable(msg2 + ".narutoxboruto." + s));
        serverPlayer.displayClientMessage(message, false);
    }

    public static void displayTranslatableMessage(ServerPlayer serverPlayer, String msg, String s, boolean b) {
        displayTranslatableMessage(serverPlayer, msg, msg, s, b);
    }

    public static String getRandomIndex(List<String> array) {
        return array.get(RANDOM.nextInt(array.size()));
    }

    public static void displayColoredMessage(Player player, String pKey, String pArg, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(pKey, Component.translatable(pArg)).withStyle(color), true);
    }

    public static void displayColoredMessage(Player player, String s, ChatFormatting chatFormatting) {
        displayColoredMessage(player, s, "", chatFormatting);
    }

  //  public static void msgPlayerInfo(ServerPlayer serverPlayer) {
  //      displayTranslatableMessage(serverPlayer, "affiliation", getAffiliation(), false);
  //      displayTranslatableMessage(serverPlayer, "clan", getClan(), false);
  //      displayTranslatableMessage(serverPlayer, "rank", getRank(), false);
  //  }

    public static double getEntitySpeed(Entity entity) {
        double motionX = entity.getX() - entity.xo;
        double motionY = entity.getY() - entity.yo;
        double motionZ = entity.getZ() - entity.zo;
        return Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
    }
}

