package net.narutoxboruto.entities.jutsus;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The hitboxes of the Water Dragon: one for the head, one for the neck, one for each body segment and
 * one for each tail segment, instead of a single box for a dragon about ten blocks long.
 *
 * Each part sits at a spot in front of (forward) and above (up) the entity's position, measured from
 * the model in two poses: upright, rising out of the puddle, and flat, in flight. While the Attack
 * animation plays the spot slides from one to the other. The boxes are cubes around those spots, so a
 * tilted dragon is still covered. The renderer draws them with F3+B and the entity uses the same ones
 * to hit things, so what you see is what hits.
 *
 * If the model changes, measure the spots again. The numbers are blocks, the model's units times 3/16.
 */
public final class WaterDragonParts {

    private WaterDragonParts() {}

    /** Forward and up in the upright pose and in the flat pose, and half the width of the box. */
    private record Part(String name, double upForward, double upUp, double flatForward, double flatUp, double radius) {}

    private static final Part[] PARTS = {
            new Part("head",  4.44,  1.63,  6.05, 0.11, 1.15),
            new Part("neck",  2.74,  2.17,  4.37, 0.24, 0.65),
            new Part("body1", 2.18,  2.36,  3.57, 0.26, 0.6),
            new Part("body2", 1.76,  2.24,  2.89, 0.28, 0.6),
            new Part("body3", 1.30,  1.87,  2.20, 0.29, 0.6),
            new Part("body4", 0.99,  1.35,  1.58, 0.30, 0.6),
            new Part("body5", 0.81,  0.74,  0.94, 0.30, 0.6),
            new Part("body6", 0.62,  0.12,  0.31, 0.28, 0.6),
            new Part("tail1", 0.57, -0.46, -0.30, 0.28, 0.55),
            new Part("tail2", 0.70, -1.07, -0.92, 0.31, 0.55),
            new Part("tail3", 0.81, -1.67, -1.54, 0.36, 0.5),
            new Part("tail4", 0.90, -2.29, -2.15, 0.43, 0.45),
            new Part("tail5", 0.95, -2.81, -2.67, 0.52, 0.4),
            new Part("tail6", 0.97, -3.24, -3.09, 0.61, 0.3),
            new Part("tail7", 0.97, -3.58, -3.42, 0.70, 0.25),
    };

    public static final int COUNT = PARTS.length;
    public static final int HEAD = 0;

    public static String name(int part) {
        return PARTS[part].name();
    }

    /**
     * The boxes of all parts, head first.
     *
     * @param origin the entity's position
     * @param yaw    the entity's yaw, in degrees
     * @param pitch  the entity's pitch, in degrees (positive looks down)
     * @param flat   0 while the dragon is upright and 1 once it is flat; in between while it swings down
     */
    public static AABB[] boxes(Vec3 origin, float yaw, float pitch, double flat) {
        double t = Mth.clamp(flat, 0.0, 1.0);
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw);
        Vec3 up = Vec3.directionFromRotation(pitch - 90.0F, yaw);

        AABB[] boxes = new AABB[PARTS.length];
        for (int i = 0; i < PARTS.length; i++) {
            Part part = PARTS[i];
            double f = Mth.lerp(t, part.upForward(), part.flatForward());
            double u = Mth.lerp(t, part.upUp(), part.flatUp());
            Vec3 center = origin.add(forward.scale(f)).add(up.scale(u));
            boxes[i] = new AABB(center, center).inflate(part.radius());
        }
        return boxes;
    }
}
