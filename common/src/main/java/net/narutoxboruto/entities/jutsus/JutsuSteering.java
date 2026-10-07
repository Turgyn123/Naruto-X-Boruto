package net.narutoxboruto.entities.jutsus;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Steering for guided jutsu projectiles (Shark Bomb, Water Dragon, Fire Ball).
 *
 * The old homing blended the velocity a fixed share toward the target every tick, which snaps the
 * projectile sideways, can send it through the ground, and gives no way for the caster to guide it.
 * These helpers turn a direction by a limited angle per tick instead, so the path is a smooth arc,
 * and pick where to steer: the caster's crosshair, or a locked target.
 */
public final class JutsuSteering {

    private JutsuSteering() {}

    /**
     * Turns {@code current} toward {@code desired} by at most {@code maxTurnRadians} and returns the
     * new unit direction. Both directions may be any length; a zero vector keeps the current direction.
     */
    public static Vec3 turnToward(Vec3 current, Vec3 desired, double maxTurnRadians) {
        if (current.lengthSqr() < 1.0e-8) return desired.lengthSqr() < 1.0e-8 ? Vec3.ZERO : desired.normalize();
        if (desired.lengthSqr() < 1.0e-8) return current.normalize();

        Vec3 from = current.normalize();
        Vec3 to = desired.normalize();
        double dot = Math.max(-1.0, Math.min(1.0, from.dot(to)));
        double angle = Math.acos(dot);
        if (angle <= maxTurnRadians || angle < 1.0e-6) return to;

        // Rotate "from" toward "to" by exactly maxTurnRadians, in the plane that contains both.
        Vec3 side = to.subtract(from.scale(dot));
        if (side.lengthSqr() < 1.0e-8) {
            // Exactly opposite: any perpendicular direction will do.
            side = Math.abs(from.y) < 0.9 ? from.cross(new Vec3(0, 1, 0)) : from.cross(new Vec3(1, 0, 0));
        }
        side = side.normalize();
        return from.scale(Math.cos(maxTurnRadians)).add(side.scale(Math.sin(maxTurnRadians))).normalize();
    }

    /**
     * The point the caster is looking at: where their look ray first hits a block, or the point
     * {@code maxDistance} blocks along it.
     */
    public static Vec3 aimPoint(LivingEntity caster, double maxDistance) {
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getLookAngle().scale(maxDistance));
        BlockHitResult hit = caster.level().clip(
                new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }

    /**
     * If a block is within {@code lookAhead} blocks in the flight direction, tips the direction upward
     * so the projectile climbs over it instead of ploughing into the ground or a low wall. Otherwise
     * returns the direction unchanged.
     */
    public static Vec3 avoidTerrain(Entity projectile, Vec3 direction, double lookAhead) {
        // From the middle of the body: a projectile whose position is at ground level would otherwise
        // see the ground right under it as an obstacle and climb away.
        return avoidTerrain(projectile, projectile.getBoundingBox().getCenter(), direction, lookAhead);
    }

    /** Same, but looks from {@code start}: for a long projectile this is its front, not its middle. */
    public static Vec3 avoidTerrain(Entity projectile, Vec3 start, Vec3 direction, double lookAhead) {
        if (direction.lengthSqr() < 1.0e-8) return direction;

        Vec3 dir = direction.normalize();
        BlockHitResult hit = projectile.level().clip(new ClipContext(
                start, start.add(dir.scale(lookAhead)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, projectile));
        if (hit.getType() == HitResult.Type.MISS) return direction;

        // The closer the wall, the harder it pulls up.
        double closeness = 1.0 - start.distanceTo(hit.getLocation()) / lookAhead;
        return new Vec3(dir.x, dir.y + 0.35 + 0.65 * closeness, dir.z).normalize();
    }
}
