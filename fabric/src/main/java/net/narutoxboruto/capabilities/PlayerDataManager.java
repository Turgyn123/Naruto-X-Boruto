package net.narutoxboruto.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.narutoxboruto.main.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private static final String NBT_KEY = "NarutoxBorutoData";
    private static final ConcurrentHashMap<UUID, PlayerCapData> PLAYER_DATA = new ConcurrentHashMap<>();

    public static PlayerCapData get(Player player) {
        return PLAYER_DATA.computeIfAbsent(player.getUUID(), k -> new PlayerCapData());
    }

    private static Path getPlayerDataFile(ServerPlayer player) {
        Path dir = player.getServer().getWorldPath(LevelResource.ROOT).resolve("narutoxboruto_data");
        return dir.resolve(player.getUUID().toString() + ".dat");
    }

    public static void save(ServerPlayer player) {
        PlayerCapData data = PLAYER_DATA.get(player.getUUID());
        if (data != null) {
            try {
                Path file = getPlayerDataFile(player);
                Files.createDirectories(file.getParent());
                CompoundTag root = new CompoundTag();
                root.put(NBT_KEY, data.toNbt());
                NbtIo.writeCompressed(root, file);
            } catch (IOException e) {
                Main.LOG.error("Failed to save player data for {}", player.getUUID(), e);
            }
        }
    }

    public static void load(ServerPlayer player) {
        try {
            Path file = getPlayerDataFile(player);
            if (Files.exists(file)) {
                CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
                CompoundTag tag = root.getCompound(NBT_KEY);
                PlayerCapData data = get(player);
                data.loadFromNbt(tag);
            }
        } catch (IOException e) {
            Main.LOG.error("Failed to load player data for {}", player.getUUID(), e);
        }
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
