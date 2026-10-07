package net.narutoxboruto.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.entities.ModeHandler;
import net.narutoxboruto.main.platform.Services;

/**
 * The shared rules for walking up walls with Chakra Control switched on, the same chakra-on-the-feet
 * trick as walking on water.
 *
 * The walking itself is client side (see {@code WallFrame}): the player's view turns so the wall
 * becomes the floor, and the movement follows it. This class holds what the client and the server both
 * need: when a player may hold on to a wall, what counts as a wall, and the server upkeep (no fall
 * damage on a wall and a little chakra). Chakra Control already turns itself off when the chakra runs
 * out, which ends the walk too.
 */
public final class WallClimbing {

    private WallClimbing() {}

    /** A wall has to be at least this many blocks tall, from the player's feet, to walk up it. */
    public static final int MIN_WALL_HEIGHT = 3;

    /** How close a block has to be to count as a wall. */
    private static final double WALL_REACH = 0.12D;
    /** The wall probe skips the bottom and top of the body so the floor and ceiling don't count. */
    private static final double PROBE_MARGIN = 0.1D;

    private static final int CLING_DRAIN_INTERVAL = 40;
    private static final int PARTICLE_INTERVAL = 8;

    /** True when this player's chakra control would let them hold on to a wall right now. */
    public static boolean canCling(Player player) {
        if (!(player instanceof ModeHandler modes) || !modes.$getChakraControl()) return false;
        return !player.isSpectator() && !player.isDeadOrDying()
                && !player.getAbilities().flying && !player.isFallFlying() && !player.isPassenger()
                && !player.isInWater() && !player.isInLava() && !player.onClimbable();
    }

    /** True while the player is up in the air with a wall at their side. Used for the pose other players see. */
    public static boolean isHangingOnWall(Player player) {
        return canCling(player) && !player.onGround() && touchingWall(player);
    }

    /** Any block close to the sides of the player's body. */
    public static boolean touchingWall(Player player) {
        AABB box = player.getBoundingBox();
        return hasBlockCollision(player, new AABB(
                box.minX - WALL_REACH, box.minY + PROBE_MARGIN, box.minZ - WALL_REACH,
                box.maxX + WALL_REACH, box.maxY - PROBE_MARGIN, box.maxZ + WALL_REACH));
    }

    /** A block right next to the player on the given horizontal side. */
    public static boolean hasWall(Player player, Direction side) {
        AABB box = player.getBoundingBox();
        double dx = side.getStepX() * WALL_REACH;
        double dz = side.getStepZ() * WALL_REACH;
        return hasBlockCollision(player, new AABB(
                box.minX + dx, box.minY + PROBE_MARGIN, box.minZ + dz,
                box.maxX + dx, box.maxY - PROBE_MARGIN, box.maxZ + dz));
    }

    /** The wall on that side reaches {@link #MIN_WALL_HEIGHT} blocks up from the player's feet. */
    public static boolean isTallWall(Player player, Direction side) {
        BlockPos base = player.blockPosition().relative(side);
        for (int up = 0; up < MIN_WALL_HEIGHT; up++) {
            BlockPos pos = base.above(up);
            BlockState state = player.level().getBlockState(pos);
            if (state.getCollisionShape(player.level(), pos).isEmpty()) return false;
        }
        return true;
    }

    private static boolean hasBlockCollision(Player player, AABB area) {
        for (VoxelShape shape : player.level().getBlockCollisions(player, area)) {
            if (!shape.isEmpty()) return true;
        }
        return false;
    }

    /** Server side, once per tick for every player. */
    public static void tickServer(ServerPlayer player) {
        if (!canCling(player) || player.onGround() || !touchingWall(player)) return;

        // The client walks on the wall and moves itself, so that must never count as a fall
        player.fallDistance = 0.0F;

        int tick = player.tickCount;
        if (tick % CLING_DRAIN_INTERVAL == 0) {
            Chakra chakra = Services.PLATFORM.getChakra(player);
            if (chakra.getValue() > 0) {
                chakra.subValue(1, player);
            }
        }
        if (tick % PARTICLE_INTERVAL == 0 && player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(), player.getY() + 0.1D, player.getZ(), 2, 0.25D, 0.05D, 0.25D, 0.0D);
        }
    }
}
