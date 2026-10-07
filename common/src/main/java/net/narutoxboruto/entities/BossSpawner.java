package net.narutoxboruto.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides when one of the shinobi bosses shows up in the overworld.
 *
 * A normal spawn weight can't make a mob rare: vanilla rolls for a spawn thousands of times per
 * second, so even a tiny weight gives a boss every few minutes. Instead every player gets one roll
 * per {@link #CHECK_INTERVAL_TICKS}. It behaves the same on NeoForge, Fabric and Forge, and these
 * constants are all there is to tune.
 */
public final class BossSpawner {

    private BossSpawner() {}

    /** How often each player gets a roll: 5 minutes. */
    private static final int CHECK_INTERVAL_TICKS = 20 * 60 * 5;
    /** Chance per roll. 4% per 5 minutes is one boss about every 2 hours of play. */
    private static final int SPAWN_CHANCE_PERCENT = 4;
    /** No new boss while another one is alive within this many blocks. */
    private static final int BOSS_FREE_RADIUS = 192;
    private static final int MIN_SPAWN_DISTANCE = 30;
    private static final int MAX_SPAWN_DISTANCE = 50;
    private static final int PLACEMENT_ATTEMPTS = 12;

    /** Call once per tick for every server player. */
    public static void tick(ServerPlayer player) {
        if (player.tickCount == 0 || player.tickCount % CHECK_INTERVAL_TICKS != 0) return;
        if (player.isSpectator()) return;

        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        if (level.random.nextInt(100) >= SPAWN_CHANCE_PERCENT) return;

        List<EntityType<? extends AbstractShinobiMob>> bosses = availableBosses();
        if (bosses.isEmpty()) return;
        if (bossNearby(level, player.blockPosition())) return;

        EntityType<? extends AbstractShinobiMob> type = bosses.get(level.random.nextInt(bosses.size()));
        BlockPos spawnPos = findSpawnPos(level, player, type);
        if (spawnPos == null) return;

        AbstractShinobiMob boss = type.create(level);
        if (boss == null) return;

        boss.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
        level.addFreshEntityWithPassengers(boss);
    }

    /** Add new bosses here once their entity type is assigned in {@link ModEntities}. */
    private static List<EntityType<? extends AbstractShinobiMob>> availableBosses() {
        List<EntityType<? extends AbstractShinobiMob>> bosses = new ArrayList<>();
        if (ModEntities.ZABUZA_MOMOCHI != null) bosses.add(ModEntities.ZABUZA_MOMOCHI);
        if (ModEntities.JINPACHI_MUNASHI != null) bosses.add(ModEntities.JINPACHI_MUNASHI);
        if (ModEntities.KISAME_HOSHIGAKI != null) bosses.add(ModEntities.KISAME_HOSHIGAKI);
        return bosses;
    }

    private static boolean bossNearby(ServerLevel level, BlockPos center) {
        AABB area = new AABB(center).inflate(BOSS_FREE_RADIUS);
        return !level.getEntitiesOfClass(AbstractShinobiMob.class, area, Entity::isAlive).isEmpty();
    }

    /** A free spot on the surface a short walk from the player, or null if none was found. */
    private static BlockPos findSpawnPos(ServerLevel level, ServerPlayer player, EntityType<?> type) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);

            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;

            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (level.getFluidState(surface.below()).is(FluidTags.LAVA)) continue;
            if (!level.noCollision(type.getSpawnAABB(surface.getX() + 0.5D, surface.getY(), surface.getZ() + 0.5D))) continue;

            return surface;
        }
        return null;
    }
}
