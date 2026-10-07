package net.narutoxboruto.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.util.WallClimbing;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Walking on walls with Chakra Control, like a gravity mod: whatever the player walks on becomes the
 * floor. They stand on it with their head pointing away from it, the camera and the model turn with them,
 * W walks forward on it, S back and A/D sideways, and the mouse looks around in that sideways world.
 *
 * <h3>Surfaces</h3>
 * The surface is described by its normal: the direction from the surface toward the player. A wall has a
 * horizontal normal, the underside of a ceiling has {@code DOWN}. The ordinary world is {@code UP}, which
 * is simply "not active". Walking up a tall wall and into a ceiling turns the player onto the ceiling
 * (an inner corner), and walking off the edge of a surface wraps the player around it onto the next face
 * (an outer corner). On every turn the view swings along with the player, like the real mod.
 *
 * <h3>The frame</h3>
 * For a normal n the frame is a plain rotation of Minecraft's usual axes: up is n, and forward at yaw 0 is
 * straight up the sky for a wall, or south for a ceiling. Everything the game computes with yaw and pitch
 * is done in the usual axes and rotated into the world with {@link #toWorld}.
 *
 * <h3>Rendering</h3>
 * Only the local player is flipped, only on the client. The real hitbox stays upright against the surface,
 * and the player's rotation fields always hold the direction they really look, so the server, the crosshair
 * and the jutsu see the right direction. Everything the view needs (camera up, eye, model) is blended from
 * a snapshot taken when the surface last changed to the current surface, so every change is smooth.
 *
 * The camera, mouse and eye hooks live in {@code mixin.wallwalk}, in their own optional mixin config.
 */
public final class WallFrame {

    private WallFrame() {}

    /** Ticks every change of surface takes to turn the view. */
    private static final int BLEND_TICKS = 12;
    /** Blocks per tick along a surface. */
    private static final double SPEED = 0.22D;
    /** Pull toward the surface, so the body stays against it. */
    private static final double STICK = 0.1D;
    /** Ticks to keep walking over the top of a wall after it ends. */
    private static final int LEDGE_TICKS = 10;
    private static final double LEDGE_PUSH = 0.12D;
    /** Ticks after letting go before a wall can be grabbed again. */
    private static final int COOLDOWN_TICKS = 10;
    /** Height of the middle of the player's box. */
    private static final double CENTER = 0.9D;
    /** The third person camera's distance behind the player. */
    private static final double CAMERA_DISTANCE = 4.0D;
    private static final float MAX_PITCH = 89.5F;

    // ---- the surface the player walks on
    private static boolean active;
    /** Direction from the surface toward the player. */
    private static Direction normal = Direction.NORTH;
    /** Where the player looks, in the frame. Yaw 0 and pitch 0 look straight ahead along the surface. */
    private static float yaw;
    private static float pitch;
    /** The direction of the keys in the walking animation: forward and to the left, from -1 to 1. */
    private static float stepForward;
    private static float stepLeft;
    /** How far the middle of the box really is from the surface it walks on. */
    private static double reach = 0.3D;
    private static int ledgeTicks;
    private static Vec3 ledgeDir = Vec3.ZERO;
    private static int cooldown;
    private static boolean moving;

    // ---- the view: from what it was when the surface last changed, to the surface now
    private static boolean visible;
    private static int blendTicks = BLEND_TICKS;
    private static Vec3 fromUp = new Vec3(0, 1, 0);
    private static Vec3 fromShift = Vec3.ZERO;
    private static Vec3 fromFeet = Vec3.ZERO;
    /** Null is the game's own upright orientation. */
    private static Quaternionf fromQuat;
    private static Vec3 fromLook = new Vec3(0, 0, 1);
    /** The look direction once the player is back in the ordinary world. */
    private static Vec3 lastLook = new Vec3(0, 0, 1);

    // ---------------------------------------------------------------- state

    public static boolean isActive() {
        return active;
    }

    /** Is the local player flipped, or still turning back? */
    public static boolean appliesTo(Entity entity) {
        return visible && entity == Minecraft.getInstance().player;
    }

    private static float ease(float partialTick) {
        float t = Mth.clamp((blendTicks + partialTick) / BLEND_TICKS, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    // ---------------------------------------------------------------- the frame

    private static Vec3 vec(Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    /** Forward at yaw 0: up the sky on a wall, south on a floor or ceiling. */
    private static Vec3 frameZ(Direction n) {
        return n.getAxis() == Direction.Axis.Y ? new Vec3(0, 0, 1) : new Vec3(0, 1, 0);
    }

    private static Vec3 frameX(Direction n) {
        return vec(n).cross(frameZ(n));
    }

    /** A vector in Minecraft's usual axes, rotated into the frame. */
    private static Vec3 toWorld(Direction n, Vec3 v) {
        return frameX(n).scale(v.x).add(vec(n).scale(v.y)).add(frameZ(n).scale(v.z));
    }

    private static Vec3 toFrame(Direction n, Vec3 world) {
        return new Vec3(world.dot(frameX(n)), world.dot(vec(n)), world.dot(frameZ(n)));
    }

    private static Quaternionf frameRotation(Direction n) {
        Vec3 x = frameX(n);
        Vec3 y = vec(n);
        Vec3 z = frameZ(n);
        Matrix3f matrix = new Matrix3f(
                (float) x.x, (float) x.y, (float) x.z,
                (float) y.x, (float) y.y, (float) y.z,
                (float) z.x, (float) z.y, (float) z.z);
        return new Quaternionf().setFromNormalized(matrix);
    }

    /** Where the frame's yaw and pitch look, as a world direction. */
    private static Vec3 frameLook() {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        double cp = Math.cos(p);
        return toWorld(normal, new Vec3(-Math.sin(y) * cp, -Math.sin(p), Math.cos(y) * cp));
    }

    private static void setFrameLook(Direction n, Vec3 world) {
        Vec3 s = toFrame(n, world.normalize());
        yaw = (float) Math.toDegrees(Math.atan2(-s.x, s.z));
        pitch = Mth.clamp((float) -Math.toDegrees(Math.asin(Mth.clamp(s.y, -1.0, 1.0))), -MAX_PITCH, MAX_PITCH);
    }

    /** Half the size of the player's box along the normal: how far the box middle is from the surface. */
    private static double halfAlong(Direction n) {
        return n.getAxis() == Direction.Axis.Y ? 0.9D : 0.3D;
    }

    /** Where the feet are, relative to the entity position: on the surface, below the middle of the box. */
    private static Vec3 feetFor(Direction n) {
        return new Vec3(0.0, CENTER, 0.0).subtract(vec(n).scale(reach));
    }

    /**
     * How far the surface really is from the middle of the box, along the way it is walked on. Around an
     * edge the body is not flat against the surface, and the model has to stay on it anyway.
     */
    private static double measureReach(Player player, Direction n) {
        double fallback = halfAlong(n);
        Direction wall = n.getOpposite();
        Direction.Axis axis = wall.getAxis();
        Vec3 center = player.getBoundingBox().getCenter();
        double[][] offsets = {{0, 0}, {0.2, 0.2}, {0.2, -0.2}, {-0.2, 0.2}, {-0.2, -0.2}};
        double best = Double.MAX_VALUE;
        for (double[] o : offsets) {
            int k = 0;
            double dx = axis != Direction.Axis.X ? o[k++] : 0.0;
            double dy = axis != Direction.Axis.Y ? o[k++] : 0.0;
            double dz = axis != Direction.Axis.Z ? o[k] : 0.0;
            Vec3 from = center.add(dx, dy, dz);
            BlockHitResult hit = player.level().clip(new ClipContext(from, from.add(vec(wall).scale(1.5)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.MISS) best = Math.min(best, from.distanceTo(hit.getLocation()));
        }
        if (best == Double.MAX_VALUE) return fallback;
        return Mth.clamp(best, fallback * 0.8, 1.3);
    }

    private static void trackReach(Player player, boolean immediate) {
        double wanted = measureReach(player, normal);
        reach = immediate ? wanted : reach + (wanted - reach) * 0.4;
    }

    /** Where the eye is, relative to where the game puts it: out from the surface, not above the head. */
    private static Vec3 shiftFor(Direction n, double eyeHeight) {
        return feetFor(n).add(vec(n).scale(eyeHeight)).subtract(0.0, eyeHeight, 0.0);
    }

    private static Quaternionf vanillaQuat(float bodyYaw) {
        return new Quaternionf().rotationY((float) Math.toRadians(180.0 - bodyYaw));
    }

    // ---- the current target of the view, and the blend toward it

    private static Vec3 toUp() {
        return active ? vec(normal) : new Vec3(0, 1, 0);
    }

    private static Vec3 toShift(Entity entity) {
        return active ? shiftFor(normal, entity.getEyeHeight()) : Vec3.ZERO;
    }

    private static Vec3 toFeet() {
        return active ? feetFor(normal) : Vec3.ZERO;
    }

    private static Quaternionf toQuat(float bodyYaw) {
        return active
                ? frameRotation(normal).mul(new Quaternionf().rotationY((float) Math.toRadians(180.0 - yaw)), new Quaternionf())
                : vanillaQuat(bodyYaw);
    }

    private static Vec3 targetLook(Entity entity, float partialTick) {
        return active ? frameLook() : entity.getViewVector(partialTick);
    }

    /** The world look direction: turning from the old look into the target while the surface changes. */
    private static Vec3 lookDirection(Entity entity, float partialTick) {
        Vec3 target = targetLook(entity, partialTick);
        float e = ease(partialTick);
        if (e >= 0.999F) return target;
        Vec3 mix = fromLook.scale(1.0F - e).add(target.scale(e));
        return mix.lengthSqr() < 1.0e-6 ? target : mix.normalize();
    }

    /** Blends two up vectors. Opposite ones (flipping from a ceiling to the floor) roll around the look direction. */
    private static Vec3 mixUp(Vec3 a, Vec3 b, float e, Vec3 look) {
        if (a.dot(b) > -0.98) {
            Vec3 mix = a.scale(1.0F - e).add(b.scale(e));
            return mix.lengthSqr() < 1.0e-6 ? b : mix.normalize();
        }
        double angle = Math.PI * e;
        Vec3 k = look.normalize();
        return a.scale(Math.cos(angle)).add(k.cross(a).scale(Math.sin(angle))).add(k.scale(k.dot(a) * (1.0 - Math.cos(angle))));
    }

    /** Remembers what the view looks like right now, to blend from it to the new surface. */
    private static void snapshot(Entity entity, float bodyYaw) {
        float e = ease(0.0F);
        Vec3 look = lookDirection(entity, 0.0F);
        Quaternionf a = fromQuat != null ? new Quaternionf(fromQuat) : vanillaQuat(bodyYaw);
        Quaternionf mixed = a.slerp(toQuat(bodyYaw), e, new Quaternionf());

        Vec3 up = mixUp(fromUp, toUp(), e, look);
        Vec3 shift = fromShift.scale(1.0F - e).add(toShift(entity).scale(e));
        Vec3 feet = fromFeet.scale(1.0F - e).add(toFeet().scale(e));

        fromUp = up;
        fromShift = shift;
        fromFeet = feet;
        fromQuat = mixed;
        fromLook = look;
        blendTicks = 0;
        visible = true;
    }

    /** What the player's rotation fields hold: the real look direction, as yaw and pitch. */
    private static void applyLook(Player player, Vec3 look) {
        double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        float newPitch = (float) -Math.toDegrees(Math.atan2(look.y, horizontal));
        float newYaw = horizontal > 1.0e-4 ? (float) Math.toDegrees(Math.atan2(-look.x, look.z)) : player.getYRot();
        player.setYRot(newYaw);
        player.setXRot(newPitch);
        player.yRotO = newYaw;
        player.xRotO = newPitch;
    }

    // ---------------------------------------------------------------- per tick

    /** Called once per client tick for the local player, after the input is read and before the player moves. */
    public static void tick(LocalPlayer player, Input input) {
        boolean allowed = WallClimbing.canCling(player);
        if (active && !allowed) {
            leave(player, 0, Vec3.ZERO, lookDirection(player, 0.0F));
        }

        if (!active) {
            if (cooldown > 0) cooldown--;
            if (allowed && cooldown == 0 && input.forwardImpulse > 0.0F && !input.jumping && !player.isShiftKeyDown()) {
                Direction facing = Direction.fromYRot(player.getYRot());
                if (WallClimbing.hasWall(player, facing) && WallClimbing.isTallWall(player, facing)) {
                    enter(player, facing.getOpposite());
                }
            }
        }

        if (active) {
            walk(player, input);
        } else {
            coast(player);
        }

        if (blendTicks < BLEND_TICKS) blendTicks++;
        if (!active && blendTicks >= BLEND_TICKS && visible) {
            // Back in the ordinary world for good
            visible = false;
            fromUp = new Vec3(0, 1, 0);
            fromShift = Vec3.ZERO;
            fromFeet = Vec3.ZERO;
            fromQuat = null;
        }
    }

    private static void enter(LocalPlayer player, Direction wallNormal) {
        // Start the turn from the body facing the wall, so the model tips straight back from it
        snapshot(player, wallNormal.getOpposite().toYRot());
        active = true;
        normal = wallNormal;
        trackReach(player, true);
        yaw = 0.0F;
        pitch = 0.0F;
        ledgeTicks = 0;
    }

    /** Back to the ordinary world, looking along {@code look}. */
    private static void leave(LocalPlayer player, int ledge, Vec3 direction, Vec3 look) {
        snapshot(player, player.yBodyRot);
        active = false;
        ledgeTicks = ledge;
        ledgeDir = direction;
        moving = false;
        lastLook = look;
        applyLook(player, look);
    }

    /**
     * Onto the next surface, turning the view with the player: the rotation that takes the old surface to
     * the new one is applied to the look direction, so the player keeps looking "forward" along the new
     * surface. {@code ledge} is the way to keep walking when the new surface is the ordinary world.
     */
    private static void changeSurface(LocalPlayer player, Direction newNormal, Vec3 ledge) {
        Vec3 look = lookDirection(player, 0.0F);
        Vec3 axis = vec(normal).cross(vec(newNormal));
        Vec3 turned = axis.cross(look).add(axis.scale(axis.dot(look)));

        if (newNormal == Direction.UP) {
            // The top of a wall, or the floor: the ordinary world
            leave(player, ledge.lengthSqr() > 0.0 ? LEDGE_TICKS : 0, ledge, turned);
            return;
        }

        snapshot(player, player.yBodyRot);
        fromLook = look;
        normal = newNormal;
        trackReach(player, true);
        setFrameLook(newNormal, turned);
        applyLook(player, look);
    }

    /**
     * The surface ends: move the body around the edge onto the next face, which faces the way it was
     * walking, and stand it against that face. The body is placed there at once and the view makes up the
     * difference, turning around the corner over the next ticks, so nothing slides. Returns false when
     * there is no room on the other side.
     */
    private static boolean hopAround(LocalPlayer player, Direction heading) {
        Direction wallSide = normal.getOpposite();
        Vec3 start = player.position();
        Direction faceSide = heading.getOpposite();
        // How far in along the old surface's thickness the middle of the body should end up
        for (double depth : new double[]{0.5, 0.35, 0.2}) {
            Vec3 in = start.add(vec(wallSide).scale(reach + depth));
            for (double push = 0.0; push <= 0.31; push += 0.05) {
                player.setPos(in.add(vec(heading).scale(push)));
                if (player.level().noCollision(player, player.getBoundingBox())
                        && WallClimbing.touches(player, faceSide)) {
                    Vec3 delta = player.position().subtract(start);
                    changeSurface(player, heading, Vec3.ZERO);
                    // Look the same until the view has turned: undo the jump in the view, and fade that out
                    fromFeet = fromFeet.subtract(delta);
                    fromShift = fromShift.subtract(delta);
                    player.setDeltaMovement(Vec3.ZERO);
                    player.setOldPosAndRot();
                    return true;
                }
            }
        }
        player.setPos(start);
        return false;
    }

    private static void walk(LocalPlayer player, Input input) {
        // Sneak lets go, jump hops off the surface
        if (player.isShiftKeyDown()) {
            leave(player, 0, Vec3.ZERO, lookDirection(player, 0.0F));
            cooldown = COOLDOWN_TICKS;
            return;
        }
        if (input.jumping) {
            Vec3 hop = vec(normal).scale(normal.getAxis() == Direction.Axis.Y ? 0.2 : 0.35);
            player.setDeltaMovement(hop.x, normal.getAxis() == Direction.Axis.Y ? hop.y : 0.35, hop.z);
            leave(player, 0, Vec3.ZERO, lookDirection(player, 0.0F));
            cooldown = COOLDOWN_TICKS;
            return;
        }

        // Move like the game does, in the frame: the keys give a direction relative to the frame's yaw
        float forward = input.forwardImpulse;
        float left = input.leftImpulse;
        input.forwardImpulse = 0.0F; // the game must not add its own movement on top
        input.leftImpulse = 0.0F;

        double length = Math.sqrt(forward * forward + left * left);
        Vec3 inFrame = Vec3.ZERO;
        if (length > 1.0e-4) {
            double scale = 1.0 / Math.max(length, 1.0);
            double f = forward * scale;
            double l = left * scale;
            double sin = Math.sin(Math.toRadians(yaw));
            double cos = Math.cos(Math.toRadians(yaw));
            inFrame = new Vec3(l * cos - f * sin, 0.0, f * cos + l * sin);
        }
        moving = length > 1.0e-4;
        stepForward = length > 1.0e-4 ? (float) (forward / Math.max(length, 1.0)) : 0.0F;
        stepLeft = length > 1.0e-4 ? (float) (left / Math.max(length, 1.0)) : 0.0F;

        Direction wallSide = normal.getOpposite();
        Vec3 stick = vec(wallSide).scale(STICK);
        Vec3 along = toWorld(normal, inFrame.scale(SPEED));

        Direction heading = along.lengthSqr() > 0.0004 ? Direction.getNearest(along.x, along.y, along.z) : null;

        // Something ahead to walk onto: a ceiling above, a wall in front, the floor below
        if (heading != null && WallClimbing.blockedAhead(player, heading)) {
            changeSurface(player, heading.getOpposite(), Vec3.ZERO);
            return;
        }

        // The surface ends: over the top of a wall, or around its edge onto the next face
        if (!WallClimbing.touches(player, wallSide)) {
            if (heading == Direction.UP) {
                changeSurface(player, Direction.UP, vec(wallSide).add(vec(heading).scale(0.6)));
            } else if (heading == null || !hopAround(player, heading)) {
                leave(player, 0, Vec3.ZERO, lookDirection(player, 0.0F));
            }
            return;
        }

        player.setDeltaMovement(along.add(stick));
        player.fallDistance = 0.0F;
        player.setSprinting(false);
        trackReach(player, false);
        applyLook(player, lookDirection(player, 0.0F));
    }

    /** Not on a surface: the last steps over the top of a wall. */
    private static void coast(LocalPlayer player) {
        if (ledgeTicks <= 0) return;
        if (player.onGround()) {
            ledgeTicks = 0;
            return;
        }
        player.setDeltaMovement(ledgeDir.x * LEDGE_PUSH, player.horizontalCollision ? LEDGE_PUSH : 0.0, ledgeDir.z * LEDGE_PUSH);
        player.fallDistance = 0.0F;
        ledgeTicks--;
    }

    // ---------------------------------------------------------------- hooks

    /** The mouse. Returns true when the turn was taken, and the game must not turn the player itself. */
    public static boolean turn(Entity entity, double yawDelta, double pitchDelta) {
        if (!active || !(entity instanceof LocalPlayer player)) return false;
        yaw = Mth.wrapDegrees(yaw + (float) (yawDelta * 0.15D));
        pitch = Mth.clamp(pitch + (float) (pitchDelta * 0.15D), -MAX_PITCH, MAX_PITCH);
        applyLook(player, lookDirection(player, 0.0F));
        return true;
    }

    /** The eye position for the crosshair and interactions. */
    public static Vec3 adjustEye(Entity entity, float partialTick, Vec3 vanillaEye) {
        if (!appliesTo(entity)) return vanillaEye;
        float e = ease(partialTick);
        Vec3 shift = fromShift.scale(1.0F - e).add(toShift(entity).scale(e));
        return vanillaEye.add(shift);
    }

    /**
     * The camera, after the game set it up: turn it to the frame's look and up, and place it out from the
     * surface. Changes {@code rotation} and the three axis vectors in place, and returns the new position,
     * or null when nothing has to change.
     *
     * In third person the game has already backed the camera off along its own idea of "behind", which is
     * wrong for a turned view (the ray hit the wall and the ground, and the camera jumped in close), so the
     * back-off is done again here along the real direction.
     */
    public static Vec3 adjustCamera(Entity entity, BlockGetter level, boolean detached, boolean reversed, float partialTick,
                                    Vec3 position, Quaternionf rotation, Vector3f forwards, Vector3f up, Vector3f left) {
        if (!appliesTo(entity)) return null;
        float e = ease(partialTick);

        Vec3 look = lookDirection(entity, partialTick);
        Vec3 wantedUp = mixUp(fromUp, toUp(), e, look);
        wantedUp = wantedUp.subtract(look.scale(wantedUp.dot(look)));
        if (wantedUp.lengthSqr() < 1.0e-6) return null;
        wantedUp = wantedUp.normalize();

        // Where the camera looks (the other way round in the front view of the third person camera)
        Vec3 target = reversed ? look.scale(-1.0) : look;

        // Turn what the game set up into what we want: first the look direction, then a roll around it
        Vector3f forwardTo = new Vector3f((float) target.x, (float) target.y, (float) target.z);
        Vector3f upTo = new Vector3f((float) wantedUp.x, (float) wantedUp.y, (float) wantedUp.z);
        Quaternionf turn = new Quaternionf().rotationTo(new Vector3f(forwards), forwardTo);
        Vector3f upNow = new Vector3f(up).rotate(turn);
        float sin = upNow.cross(upTo, new Vector3f()).dot(forwardTo);
        float cos = upNow.dot(upTo);
        Quaternionf roll = new Quaternionf().fromAxisAngleRad(forwardTo, (float) Math.atan2(sin, cos));
        Quaternionf delta = roll.mul(turn, new Quaternionf());

        rotation.premul(delta);
        forwards.rotate(delta);
        up.rotate(delta);
        left.rotate(delta);

        // The eye, out from the surface; first person looks from there
        Vec3 shift = fromShift.scale(1.0F - e).add(toShift(entity).scale(e));
        Vec3 eye = entity.getPosition(partialTick).add(0.0, entity.getEyeHeight(), 0.0).add(shift);
        if (!detached) return eye;

        // Third person: back off from the eye along the real direction, stopping at blocks in the way
        Vec3 back = target.scale(-1.0);
        double distance = CAMERA_DISTANCE;
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(back.scale(distance + 0.3)),
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
        if (hit.getType() != HitResult.Type.MISS) {
            distance = Math.max(0.0, hit.getLocation().distanceTo(eye) - 0.3);
        }
        return eye.add(back.scale(distance));
    }

    /**
     * The player model, in the entity's frame at the end of the game's own rotations: undo the game's
     * body yaw, then turn and move the model onto the surface, blended with how it stood before.
     */
    public static void transformModel(PoseStack poseStack, AbstractClientPlayer player, float bodyYaw, float partialTick) {
        if (!appliesTo(player)) return;
        float e = ease(partialTick);

        Quaternionf start = fromQuat != null ? new Quaternionf(fromQuat) : vanillaQuat(bodyYaw);
        Quaternionf both = start.slerp(toQuat(bodyYaw), e, new Quaternionf());
        Vec3 feet = fromFeet.scale(1.0F - e).add(toFeet().scale(e));

        poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180.0F));
        poseStack.translate(feet.x, feet.y, feet.z);
        poseStack.mulPose(both);
    }

    /** The head follows the frame's pitch, and the limbs swing while walking. */
    public static void poseModel(ModelPart head, ModelPart rightArm, ModelPart leftArm,
                                 ModelPart rightLeg, ModelPart leftLeg, float ageInTicks) {
        if (!active) return;
        head.yRot = 0.0F;
        head.xRot = (float) Math.toRadians(pitch);
        if (moving) {
            // Forward and back swing the limbs, a step to the side spreads the legs the way they move
            float swing = ageInTicks * 0.8F;
            float forward = Math.abs(stepForward);
            float side = Math.abs(stepLeft);
            float cos = Mth.cos(swing);
            float amount = Math.max(forward, side * 0.35F);
            rightArm.xRot = -cos * 1.0F * amount;
            leftArm.xRot = cos * 1.0F * amount;
            rightLeg.xRot = cos * 1.4F * forward;
            leftLeg.xRot = -cos * 1.4F * forward;
            float lead = 0.5F + 0.5F * cos;
            float trail = 1.0F - lead;
            if (stepLeft > 0.0F) {
                leftLeg.zRot = -0.5F * side * lead;
                rightLeg.zRot = -0.5F * side * trail;
            } else {
                rightLeg.zRot = 0.5F * side * lead;
                leftLeg.zRot = 0.5F * side * trail;
            }
        }
    }
}
