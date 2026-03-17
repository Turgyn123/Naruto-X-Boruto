package net.narutoxboruto.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private static final String NBT_KEY = "NarutoxBorutoData";
    private static final ConcurrentHashMap<UUID, PlayerCapData> PLAYER_DATA = new ConcurrentHashMap<>();

    public static PlayerCapData get(Player player) {
        return PLAYER_DATA.computeIfAbsent(player.getUUID(), k -> new PlayerCapData());
    }

    public static void save(ServerPlayer player) {
        PlayerCapData data = PLAYER_DATA.get(player.getUUID());
        if (data != null) {
            player.getPersistentData().put(NBT_KEY, data.toNbt());
        }
    }

    public static void load(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData().getCompound(NBT_KEY);
        PlayerCapData data = get(player);
        data.loadFromNbt(tag);
    }

    public static void copyFrom(ServerPlayer original, ServerPlayer newPlayer) {
        PlayerCapData source = get(original);
        PlayerCapData dest = get(newPlayer);
        dest.copyFrom(source);
    }

    public static void remove(UUID uuid) {
        PLAYER_DATA.remove(uuid);
    }
}
