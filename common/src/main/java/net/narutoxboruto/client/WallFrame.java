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
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.util.WallClimbing;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Walking up a wall with Chakra Control, like a gravity mod: the wall becomes the floor. The player
 * stands on it with their head pointing away from the wall, the camera and the model turn with them,
 * W walks up the wall, S down and A/D along it, and the mouse looks around in that sideways world.
 *
 * Only the local player is flipped, and only on the client. The real hitbox stays upright against the
 * wall, and the player's rotation is always set to where they really look, so the server, the crosshair
 * and the jutsu see the right direction. The sideways world is called the frame: its "up" is the wall
 * normal (away from the wall) and its "forward" at yaw 0 is straight up the wall.
 *
 * The frame is a plain rotation of Minecraft's usual axes: x' = n × up, y' = n, z' = up (north of the
 * frame is up the sky). Everything the game computes with yaw and pitch is done in the usual axes and
 * then rotated into the world with {@link #toWorld}.
 *
 * The camera, mouse and eye hooks live in {@code mixin.wallwalk}, in their own optional mixin config.
 */
public final class WallFrame {

    private WallFrame() {}

    /** Ticks the flip takes in each direction. */
    private static final int BLEND_TICKS = 8;
    /** Blocks per tick along the wall. */
    private static final double SPEED = 0.22D;
    /** Pull toward the wall, so the body stays against it. */
    private static final double STICK = 0.1D;
    /** Ticks to keep walking over the top of a wall after it ends. */
    private static final int LEDGE_TICKS = 10;
    private static final double LEDGE_PUSH = 0.12D;
    /** Ticks after letting go before the wall can be grabbed again. */
    private static final int COOLDOWN_TICKS = 10;
    /** Height of the middle of the player's box, and the distance from it to a wall it stands against. */
    private static final double CENTER = 0.9D;
    private static final double HALF_WIDTH = 0.3D;

    private static boolean active;
    /** Horizontal direction from the wall toward the player. */
    private static Direction normal = Direction.NORTH;
    /** Where the player looks, in the frame. Yaw 0 and pitch 0 look straight up the wall. */
    private static float yaw;
    private static float pitch;
    private static int blendTicks;
    private static int ledgeTicks;
    private static int cooldown;
    private static Vec3 ledgeDir = Vec3.ZERO;
    /** The look direction when the flip began, and the last look direction once it ended. */
    private static Vec3 entryLook = new Vec3(0, 0, 1);
    private static Vec3 lastLook = new Vec3(0, 0, 1);
    private static boolean moving;

    // ---------------------------------------------------------------- state

    public static boolean isActive() {
        return active;
    }

    /** Is the local player flipped, or still turning back? */
    public static boolean appliesTo(Entity entity) {
        return blendTicks > 0 && entity == Minecraft.getInstance().player;
    }

    /** 0 upright, 1 fully on the wall, smooth in between. */
    public static float blend(float partialTick) {
        float t = (blendTicks + (active ? partialTick : -partialTick)) / BLEND_TICKS;
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    // ---------------------------------------------------------------- the frame

    private static Vec3 normalVec() {
        return new Vec3(normal.getStepX(), 0.0, normal.getStepZ());
    }

    /** The frame's x axis: normal × up. */
    private static Vec3 frameX() {
        return normalVec().cross(new Vec3(0, 1, 0));
    }

    /** A vector in Minecraft's usual axes, rotated into the frame. */
    private static Vec3 toWorld(Vec3 v) {
        return frameX().scale(v.x).add(normalVec().scale(v.y)).add(0.0, v.z, 0.0);
    }

    private static Quaternionf frameRotation() {
        Vec3 x = frameX();
        Vec3 n = normalVec();
        Matrix3f matrix = new Matrix3f(
                (float) x.x, (float) x.y, (float) x.z,
                (float) n.x, (float) n.y, (float) n.z,
                0.0F, 1.0F, 0.0F);
        return new Quaternionf().setFromNormalized(matrix);
    }

    /** Where the frame's yaw and pitch look, as a world direction. */
    private static Vec3 frameLook() {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        double cp = Math.cos(p);
        return toWorld(new Vec3(-Math.sin(y) * cp, -Math.sin(p), Math.cos(y) * cp));
    }

    /** The world look direction: turning from the old look into the frame's while it flips. */
    public static Vec3 lookDirection(float partialTick) {
        if (!active) return lastLook;
        Vec3 target = frameLook();
        float b = blend(partialTick);
        if (b >= 0.999F) return target;
        Vec3 mix = entryLook.scale(1.0F - b).add(target.scale(b));
        return mix.lengthSqr() < 1.0e-6 ? target : mix.normalize();
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
            exit(0, Vec3.ZERO);
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

        blendTicks = Mth.clamp(blendTicks + (active ? 1 : -1), 0, BLEND_TICKS);
    }

    private static void enter(LocalPlayer player, Direction wallNormal) {
        active = true;
        normal = wallNormal;
        yaw = 0.0F;
        pitch = 0.0F;
        entryLook = player.getLookAngle();
        ledgeTicks = 0;
    }

    private static void exit(int ledge, Vec3 direction) {
        lastLook = lookDirection(0.0F);
        active = false;
        ledgeTicks = ledge;
        ledgeDir = direction;
        moving = false;
    }

    private static void walk(LocalPlayer player, Input input) {
        // Sneak lets go, jump hops off the wall
        if (player.isShiftKeyDown()) {
            exit(0, Vec3.ZERO);
            cooldown = COOLDOWN_TICKS;
            return;
        }
        if (input.jumping) {
            player.setDeltaMovement(normal.getStepX() * 0.35, 0.35, normal.getStepZ() * 0.35);
            exit(0, Vec3.ZERO);
            cooldown = COOLDOWN_TICKS;
            return;
        }

        boolean touching = WallClimbing.hasWall(player, normal.getOpposite());

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

        Vec3 velocity = toWorld(inFrame.scale(SPEED)).add(-normal.getStepX() * STICK, 0.0, -normal.getStepZ() * STICK);
        boolean goingUp = velocity.y > 0.01;
        boolean goingDown = velocity.y < -0.01;

        if (!touching) {
            // The wall ended. Over the top: keep walking onto it. Off the side: just fall.
            exit(goingUp ? LEDGE_TICKS : 0, new Vec3(-normal.getStepX(), 0.0, -normal.getStepZ()));
            return;
        }
        if (goingDown && player.onGround()) {
            exit(0, Vec3.ZERO); // back on the floor
            return;
        }

        player.setDeltaMovement(velocity);
        player.fallDistance = 0.0F;
        player.setSprinting(false);
        applyLook(player, lookDirection(0.0F));
    }

    /** Not on a wall: the last steps over the top of one. */
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
        pitch = Mth.clamp(pitch + (float) (pitchDelta * 0.15D), -90.0F, 90.0F);
        applyLook(player, lookDirection(0.0F));
        return true;
    }

    /** Where the eye is in the frame, relative to where the game puts it: out from the wall, not above the head. */
    private static Vec3 eyeShift(Entity entity) {
        Vec3 n = normalVec();
        double eye = entity.getEyeHeight();
        return new Vec3(0.0, CENTER, 0.0).subtract(n.scale(HALF_WIDTH)).add(n.scale(eye)).subtract(0.0, eye, 0.0);
    }

    /** The eye position for the crosshair and interactions. */
    public static Vec3 adjustEye(Entity entity, float partialTick, Vec3 vanillaEye) {
        if (!appliesTo(entity)) return vanillaEye;
        float b = blend(partialTick);
        return b <= 0.0F ? vanillaEye : vanillaEye.add(eyeShift(entity).scale(b));
    }

    /**
     * The camera, after the game set it up: turn it to the frame's look and up, and move it out from the
     * wall. Changes {@code rotation} and the three axis vectors in place, and returns the new position,
     * or null when nothing has to change.
     */
    public static Vec3 adjustCamera(Entity entity, boolean reversed, float partialTick, Vec3 position,
                                    Quaternionf rotation, Vector3f forwards, Vector3f up, Vector3f left) {
        if (!appliesTo(entity)) return null;
        float b = blend(partialTick);
        if (b <= 0.0F) return null;

        Vec3 look = lookDirection(partialTick);
        Vec3 wantedUp = new Vec3(0, 1, 0).scale(1.0F - b).add(normalVec().scale(b));
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

        return position.add(eyeShift(entity).scale(b));
    }

    /**
     * The player model, in the entity's frame at the end of the game's own rotations: undo the game's
     * body yaw, then turn and move the model into the frame, blended with how it normally stands.
     */
    public static void transformModel(PoseStack poseStack, AbstractClientPlayer player, float bodyYaw, float partialTick) {
        if (!appliesTo(player)) return;
        float b = blend(partialTick);

        Quaternionf upright = new Quaternionf().rotationY((float) Math.toRadians(180.0 - bodyYaw));
        Quaternionf flipped = frameRotation().mul(
                new Quaternionf().rotationY((float) Math.toRadians(180.0 - yaw)), new Quaternionf());
        Quaternionf both = upright.slerp(flipped, b, new Quaternionf());

        // The feet go on the wall, at the height of the middle of the box
        Vec3 feet = new Vec3(0.0, CENTER, 0.0).subtract(normalVec().scale(HALF_WIDTH)).scale(b);

        poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180.0F));
        poseStack.translate(feet.x, feet.y, feet.z);
        poseStack.mulPose(both);
    }

    /** The head follows the frame's pitch, and the limbs swing while walking. */
    public static void poseModel(ModelPart head, ModelPart rightArm, ModelPart leftArm,
                                 ModelPart rightLeg, ModelPart leftLeg, float ageInTicks) {
        head.yRot = 0.0F;
        head.xRot = (float) Math.toRadians(pitch);
        if (moving) {
            float swing = ageInTicks * 0.8F;
            rightArm.xRot = Mth.cos(swing + (float) Math.PI) * 1.0F;
            leftArm.xRot = Mth.cos(swing) * 1.0F;
            rightLeg.xRot = Mth.cos(swing) * 1.4F;
            leftLeg.xRot = Mth.cos(swing + (float) Math.PI) * 1.4F;
        }
    }
}
