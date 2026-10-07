package net.narutoxboruto.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.entities.ModeHandler;
import net.narutoxboruto.main.platform.Services;

/**
 * Wall climbing for players with Chakra Control switched on, the same chakra-on-the-feet trick as
 * walking on water.
 *
 * How it plays: walk into a wall and keep holding forward to run up it. Look steeply down to run
 * back down, hold sneak to slide down, and let go of everything to hang in place. Pressing back lets
 * go. At the top the player keeps walking over the ledge.
 *
 * The movement is driven by the client (like all player movement) from {@link #tickLocal}. The server
 * only does the upkeep in {@link #tickServer}: no fall damage while on a wall, and a little chakra.
 * Chakra Control already turns itself off when the chakra runs out, so that ends the climb too.
 */
public final class WallClimbing {

    private WallClimbing() {}

    /** Blocks per tick, the same speed as a ladder. */
    private static final double CLIMB_SPEED = 0.2D;
    private static final double SLIDE_SPEED = 0.12D;
    /** Looking further down than this turns "forward" into "down the wall". */
    private static final float LOOK_DOWN_PITCH = 40.0F;
    /** How close a block has to be to count as a wall. */
    private static final double WALL_REACH = 0.12D;
    /** The wall probe skips the bottom and top of the body so the floor and ceiling don't count. */
    private static final double PROBE_MARGIN = 0.1D;
    /** Ticks to keep walking forward after the top of the wall, so the player ends up on the ledge. */
    private static final int LEDGE_TICKS = 10;
    private static final double LEDGE_LIFT = 0.12D;

    private static final int CLING_DRAIN_INTERVAL = 40;
    private static final int PARTICLE_INTERVAL = 8;

    // State of the local player. Only touched from the client thread through tickLocal().
    private static boolean clinging;
    private static boolean climbedUp;
    private static int ledgeTicks;

    /** True when this player's chakra control would let them hold on to a wall right now. */
    public static boolean canCling(Player player) {
        if (!(player instanceof ModeHandler modes) || !modes.$getChakraControl()) return false;
        return !player.isSpectator() && !player.isDeadOrDying()
                && !player.getAbilities().flying && !player.isFallFlying() && !player.isPassenger()
                && !player.isInWater() && !player.isInLava() && !player.onClimbable();
    }

    /** True while the player is up in the air with a wall at their side. Used for the climbing pose. */
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

    /** A block right in front of the player, in the direction they are facing. */
    private static boolean wallInFront(Player player) {
        Vec3 facing = Vec3.directionFromRotation(0.0F, player.getYRot());
        AABB box = player.getBoundingBox();
        return hasBlockCollision(player, new AABB(
                box.minX + facing.x * WALL_REACH, box.minY + PROBE_MARGIN, box.minZ + facing.z * WALL_REACH,
                box.maxX + facing.x * WALL_REACH, box.maxY - PROBE_MARGIN, box.maxZ + facing.z * WALL_REACH));
    }

    private static boolean hasBlockCollision(Player player, AABB area) {
        for (VoxelShape shape : player.level().getBlockCollisions(player, area)) {
            if (!shape.isEmpty()) return true;
        }
        return false;
    }

    private static void reset() {
        clinging = false;
        climbedUp = false;
        ledgeTicks = 0;
    }

    /**
     * Client side, once per tick for the local player, after the input was read and before the
     * player moves.
     *
     * @param forward  forward impulse, positive when walking forward and negative when walking back
     * @param jumping  jump key held
     * @param sneaking sneak key held
     */
    public static void tickLocal(Player player, float forward, boolean jumping, boolean sneaking) {
        if (!canCling(player)) {
            reset();
            return;
        }

        boolean onWall = touchingWall(player);
        if (clinging && (!onWall || forward < 0.0F)) {
            // Past the top of the wall: keep walking forward for a moment so the player gets onto the ledge
            if (!onWall && climbedUp && forward > 0.0F) {
                ledgeTicks = LEDGE_TICKS;
            }
            clinging = false;
        }
        if (!clinging && forward > 0.0F && wallInFront(player)) {
            clinging = true;
        }

        double wanted;
        if (sneaking) {
            wanted = -SLIDE_SPEED;
        } else if (forward > 0.0F) {
            wanted = player.getXRot() > LOOK_DOWN_PITCH ? -CLIMB_SPEED : CLIMB_SPEED;
        } else if (jumping) {
            wanted = CLIMB_SPEED;
        } else {
            wanted = 0.0D;
        }

        // Back on the floor with nothing to climb: just walk
        if (clinging && player.onGround() && wanted <= 0.0D) {
            clinging = false;
        }

        Vec3 motion = player.getDeltaMovement();
        if (clinging) {
            player.setDeltaMovement(motion.x, wanted, motion.z);
            player.fallDistance = 0.0F;
            climbedUp = wanted > 0.0D;
            ledgeTicks = 0;
        } else if (ledgeTicks > 0) {
            if (forward > 0.0F && !player.onGround()) {
                // Hover level with the ledge, and nudge up if its edge is still in the way
                player.setDeltaMovement(motion.x, player.horizontalCollision ? LEDGE_LIFT : 0.0D, motion.z);
                player.fallDistance = 0.0F;
                ledgeTicks--;
            } else {
                ledgeTicks = 0;
            }
        }
    }

    /** Server side, once per tick for every player. */
    public static void tickServer(ServerPlayer player) {
        if (!canCling(player) || player.onGround() || !touchingWall(player)) return;

        // The client holds on to the wall and moves itself, so a climb must never count as a fall
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
