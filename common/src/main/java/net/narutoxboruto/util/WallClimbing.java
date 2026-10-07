package net.narutoxboruto.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    /** The probe for a side skips this much of the other sides of the body, so a floor or ceiling doesn't count as a wall. */
    private static final double PROBE_MARGIN = 0.1D;

    private static final int CLING_DRAIN_INTERVAL = 40;

    /** True when this player's chakra control would let them hold on to a wall right now. */
    public static boolean canCling(Player player) {
        if (!(player instanceof ModeHandler modes) || !modes.$getChakraControl()) return false;
        return !player.isSpectator() && !player.isDeadOrDying()
                && !player.getAbilities().flying && !player.isFallFlying() && !player.isPassenger()
                && !player.isInWater() && !player.isInLava() && !player.onClimbable();
    }

    /** Any block close to the sides of the player's body. */
    public static boolean touchingWall(Player player) {
        AABB box = player.getBoundingBox();
        return hasBlockCollision(player, new AABB(
                box.minX - WALL_REACH, box.minY + PROBE_MARGIN, box.minZ - WALL_REACH,
                box.maxX + WALL_REACH, box.maxY - PROBE_MARGIN, box.maxZ + WALL_REACH));
    }

    /** A block close to the body on the given side: a wall, the floor or the ceiling. */
    public static boolean touches(Player player, Direction side) {
        AABB box = player.getBoundingBox()
                .deflate(side.getAxis() == Direction.Axis.X ? 0.0 : PROBE_MARGIN,
                         side.getAxis() == Direction.Axis.Y ? 0.0 : PROBE_MARGIN,
                         side.getAxis() == Direction.Axis.Z ? 0.0 : PROBE_MARGIN)
                .move(side.getStepX() * WALL_REACH, side.getStepY() * WALL_REACH, side.getStepZ() * WALL_REACH);
        return hasBlockCollision(player, box);
    }

    /**
     * A block close to the body on a wall side or above it. The floor does not count: standing on it is not
     * holding on to anything, and it would cancel every fall.
     */
    public static boolean touchingAnySide(Player player) {
        for (Direction side : Direction.values()) {
            if (side != Direction.DOWN && touches(player, side)) return true;
        }
        return false;
    }

    /**
     * A solid block just ahead of the middle of the body in the given direction: something to walk onto
     * (a ceiling above, a wall in front). It is checked from the middle so a small bump at the edge of the
     * body does not count.
     */
    public static boolean blockedAhead(Player player, Direction ahead) {
        double halfSize = ahead.getAxis() == Direction.Axis.Y ? 0.9 : 0.3;
        Vec3 center = player.getBoundingBox().getCenter().add(
                ahead.getStepX() * (halfSize + 0.15), ahead.getStepY() * (halfSize + 0.15), ahead.getStepZ() * (halfSize + 0.15));
        return hasBlockCollision(player, new AABB(center, center).inflate(0.15));
    }

    /** A block right next to the player on the given horizontal side. */
    public static boolean hasWall(Player player, Direction side) {
        return touches(player, side);
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
        if (!canCling(player) || player.onGround() || !touchingAnySide(player)) return;

        // The client walks on the wall and moves itself, so that must never count as a fall
        player.fallDistance = 0.0F;

        int tick = player.tickCount;
        if (tick % CLING_DRAIN_INTERVAL == 0) {
            Chakra chakra = Services.PLATFORM.getChakra(player);
            if (chakra.getValue() > 0) {
                chakra.subValue(1, player);
            }
        }
    }
}
