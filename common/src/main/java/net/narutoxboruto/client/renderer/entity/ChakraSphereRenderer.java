package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import org.joml.Matrix4f;

/**
 * Draws the {@link ChakraSphereEntity} in the same style as the lightning of the Lightning Chakra Mode: no models and
 * no textures, only flat, hard coloured shapes with angular corners that are added to the light of the world. Like the
 * lightning, a streak is made of three flat layers (a white core, a light blue middle and a wide soft blue glow) and
 * is made of a few straight pieces, so it looks angular and a bit blocky, like Minecraft, and not smooth and real.
 * <ul>
 *   <li>Rasengan: a ball of flat facets (a white heart in a blue ball) covered with a tangle of fine thin lines, with a
 *       few short rays shooting out of it, as in the anime.</li>
 *   <li>Rasenshuriken: held up above the head of the caster while it is charged. The same ball with four big curved
 *       blades of flat colour with a white edge turning round it. Where it hits it grows into a huge ball of streaks
 *       with blades turning in three planes.</li>
 * </ul>
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    // The three layers of a streak, as in the lightning: white core, light blue middle, wide blue glow
    private static final float[][] LAYER_RGB = {{1.0F, 1.0F, 1.0F}, {0.45F, 0.82F, 1.0F}, {0.18F, 0.55F, 1.0F}};
    private static final float[] LAYER_WIDTH = {0.42F, 1.0F, 3.0F};
    private static final float[] LAYER_ALPHA = {1.0F, 0.85F, 0.22F};
    // Each layer is turned by a different angle, so no two layers lie in the same plane and fight over the same pixels
    private static final float[] LAYER_COS = {1.0F, (float) Math.cos(Math.PI / 6), (float) Math.cos(Math.PI / 3)};
    private static final float[] LAYER_SIN = {0.0F, (float) Math.sin(Math.PI / 6), (float) Math.sin(Math.PI / 3)};

    public ChakraSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ChakraSphereEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        boolean shuriken = entity.getKind() == ChakraSphereEntity.RASENSHURIKEN;
        int phase = entity.getPhase();
        float phaseAge = entity.getPhaseTicks() + partialTick;
        float age = entity.tickCount + partialTick;

        // At the hand of the caster while charging (and for the Rasengan while it is driven forward), and otherwise
        // where it is. The way it faces is the way the caster looks, or the way it was thrown.
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.pushPose();
        if (phase == ChakraSphereEntity.CHARGING || (!shuriken && phase == ChakraSphereEntity.ACTIVE)) {
            Entity owner = entity.level().getEntity(entity.getOwnerId());
            if (owner != null) {
                Vec3 hand = ChakraSphereEntity.handPosition(owner, entity.getKind(), partialTick);
                Vec3 here = entity.getPosition(partialTick);
                poseStack.translate(hand.x - here.x, hand.y - here.y, hand.z - here.z);
                yaw = owner.getViewYRot(partialTick);
                pitch = owner.getViewXRot(partialTick);
            }
        }

        // How big the ball is, how far the blades reach and how bright everything is
        float charge = Mth.clamp(phaseAge / (shuriken ? ChakraSphereEntity.RASENSHURIKEN_CHARGE_TICKS : ChakraSphereEntity.RASENGAN_CHARGE_TICKS), 0.0F, 1.0F);
        float radius;
        float reach = 0.0F;
        float alpha = 1.0F;
        boolean blast = false;
        if (!shuriken) {
            if (phase == ChakraSphereEntity.CHARGING) {
                radius = 0.07F + 0.21F * charge;
                if (charge >= 1.0F) radius *= 1.0F + 0.07F * Mth.sin(age * 1.3F);
            } else if (phase == ChakraSphereEntity.ACTIVE) {
                radius = 0.3F;
            } else {
                float t = Mth.clamp(phaseAge / ChakraSphereEntity.RASENGAN_BURST_TICKS, 0.0F, 1.0F);
                radius = 0.3F + 1.7F * t;
                alpha = 1.0F - t;
            }
        } else if (phase == ChakraSphereEntity.CHARGING) {
            radius = 0.08F + 0.2F * charge;
            reach = 0.25F + 1.0F * charge;
            if (charge >= 1.0F) {
                float pulse = 1.0F + 0.07F * Mth.sin(age * 1.3F);
                radius *= pulse;
                reach *= pulse;
            }
        } else if (phase == ChakraSphereEntity.ACTIVE) {
            radius = 0.3F;
            reach = 1.3F;
        } else {
            blast = true;
            float grown = Mth.clamp(phaseAge / ChakraSphereEntity.SHURIKEN_GROW_TICKS, 0.0F, 1.0F);
            grown = grown * grown * (3.0F - 2.0F * grown);
            radius = Mth.lerp(grown, 0.3F, (float) ChakraSphereEntity.SHURIKEN_RADIUS);
            reach = radius;
            alpha = Mth.clamp((ChakraSphereEntity.SHURIKEN_BLAST_TICKS - phaseAge) / 6.0F, 0.0F, 1.0F);
        }
        if (alpha < 0.01F) {
            poseStack.popPose();
            return;
        }

        // The way it is aimed, and two directions across it
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        Vec3 aim = new Vec3(-Math.sin(yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(yawRad) * Math.cos(pitchRad));
        Vec3 across = aim.cross(new Vec3(0.0D, 1.0D, 0.0D));
        across = across.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : across.normalize();
        Vec3 up = aim.cross(across).normalize();

        VertexConsumer vc = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        float scale = Math.max(radius, reach * 0.35F) / 0.35F;
        // The streaks jump from shape to shape 10 times a second, like the bolts of the lightning
        int jump = (int) (age * 0.5F);

        // The ball, drawn first so that it hides the streaks behind it: a white heart in a blue ball, so it is white in
        // the middle and blue at the edge, each of a few flat facets of its own shade, so it is round but blocky
        if (!blast) {
            facetSphere(vc, matrix, radius * 0.28F, 1.0F, 1.0F, 1.0F, 0.9F * alpha, 7);
            facetSphere(vc, matrix, radius * 0.55F, 0.75F, 0.93F, 1.0F, 0.4F * alpha, 17);
            facetSphere(vc, matrix, radius * 0.97F, 0.35F, 0.68F, 1.0F, 0.5F * alpha, 31);
            // A tangle of fine thin lines over the whole ball, as in the anime, turning slowly
            yarn(vc, matrix, radius * 1.01F, age, jump, shuriken ? 38 : 46, 0.009F * scale, alpha);
            if (!shuriken) {
                // A few short straight rays shoot out of it
                for (int i = 0; i < 14; i++) {
                    Vec3 dir = randomDirection(i * 5 + jump * 11);
                    double from = radius * (1.1D + 0.1D * hash(i * 3 + jump));
                    double to = radius * (1.5D + 0.9D * hash(i * 7 + jump * 3));
                    bolt(vc, matrix, new Vec3[]{dir.scale(from), dir.scale(to)}, 0.012F * scale, 0.7F * alpha);
                }
            }
        } else {
            facetSphere(vc, matrix, radius, 0.35F, 0.7F, 1.0F, 0.08F * alpha, 41);
            yarn(vc, matrix, radius * 0.98F, age, jump, 70, 0.02F * scale, alpha);
        }

        // The four big blades of the Rasenshuriken, across the way it is aimed
        if (shuriken) {
            int sets = blast ? 3 : 1;
            for (int k = 0; k < sets; k++) {
                Vec3 axis = k == 0 ? aim : (k == 1 ? across : up);
                Vec3 sideA = k == 0 ? across : (k == 1 ? up : aim);
                Vec3 sideB = axis.cross(sideA).normalize();
                float spin = (float) (Math.floor(age * 0.5F) * 0.3D * (0.5F + 0.12F * k)) * (k == 1 ? -1.0F : 1.0F) * 3.3F;
                for (int blade = 0; blade < 4; blade++) {
                    blade(vc, matrix, axis, sideA, sideB, reach, spin + blade * (float) (Math.PI / 2.0D), alpha * (blast ? 0.7F : 1.0F));
                }
            }
        }
        poseStack.popPose();
    }

    /** A direction that points anywhere, always the same for the same number. */
    private static Vec3 randomDirection(int n) {
        double theta = hash(n * 7 + 1) * Math.PI * 2.0D;
        double z = hash(n * 7 + 2) * 2.0D - 1.0D;
        double ring = Math.sqrt(1.0D - z * z);
        return new Vec3(Math.cos(theta) * ring, z, Math.sin(theta) * ring);
    }

    /**
     * A tangle of fine thin lines over the surface of a ball, like thread wound round it, as the Rasengan is drawn in
     * the anime. Every line is a part of a circle round the middle of the ball, made of a few straight pieces, and the
     * whole tangle turns slowly round the ball.
     */
    private static void yarn(VertexConsumer vc, Matrix4f matrix, float radius, float age, int jump, int lines, float halfWidth, float alpha) {
        double turn = age * 0.06D;
        double cos = Math.cos(turn);
        double sin = Math.sin(turn);
        for (int i = 0; i < lines; i++) {
            Vec3 normal = randomDirection(i * 3 + 50);
            Vec3 a = normal.cross(new Vec3(0.0D, 1.0D, 0.0D));
            a = a.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : a.normalize();
            Vec3 b = normal.cross(a).normalize();
            double start = hash(i * 3 + 51) * Math.PI * 2.0D;
            double span = 0.7D + 1.1D * hash(i * 3 + 52);
            int points = 6;
            Vec3[] path = new Vec3[points + 1];
            for (int j = 0; j <= points; j++) {
                double angle = start + span * j / points;
                // A little knocked out of line, and a little different from time to time
                double kink = 1.0D + 0.04D * (hash(i * 23 + j * 5 + jump) - 0.5D);
                Vec3 point = a.scale(Math.cos(angle)).add(b.scale(Math.sin(angle))).scale(radius * kink);
                // The tangle turns round the up axis
                path[j] = new Vec3(point.x * cos - point.z * sin, point.y, point.x * sin + point.z * cos);
            }
            bolt(vc, matrix, path, halfWidth, alpha);
        }
    }

    /**
     * A streak along {@code path}: thin at its tail and wide at its head, in the three flat layers of the lightning.
     * Every layer is two ribbons across each other, turned by the angle of its layer.
     */
    private static void bolt(VertexConsumer vc, Matrix4f matrix, Vec3[] path, float halfWidth, float alpha) {
        int n = path.length - 1;
        for (int layer = 2; layer >= 0; layer--) {
            float[] rgb = LAYER_RGB[layer];
            float a = LAYER_ALPHA[layer] * alpha;
            for (int j = 0; j < n; j++) {
                Vec3 direction = path[j + 1].subtract(path[j]);
                if (direction.lengthSqr() < 1.0E-10D) continue;
                direction = direction.normalize();
                Vec3 ref = Math.abs(direction.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
                Vec3 u = direction.cross(ref).normalize();
                Vec3 w = direction.cross(u).normalize();
                double w0 = halfWidth * LAYER_WIDTH[layer] * (0.2D + 0.8D * j / n);
                double w1 = halfWidth * LAYER_WIDTH[layer] * (0.2D + 0.8D * (j + 1) / n);
                // The layer's own angle
                Vec3 sideA = u.scale(LAYER_COS[layer]).add(w.scale(LAYER_SIN[layer]));
                Vec3 sideB = w.scale(LAYER_COS[layer]).subtract(u.scale(LAYER_SIN[layer]));
                for (Vec3 side : new Vec3[]{sideA, sideB}) {
                    quad(vc, matrix, path[j].add(side.scale(w0)), path[j].subtract(side.scale(w0)),
                            path[j + 1].subtract(side.scale(w1)), path[j + 1].add(side.scale(w1)), rgb[0], rgb[1], rgb[2], a);
                }
            }
        }
    }

    /**
     * A big curved blade that sweeps out from the middle, made of a few straight pieces: wide near the middle and
     * pointed at the tip, flat light blue, with a wide blue glow under it and a white edge on its leading side. It lies
     * in the plane of {@code sideA} and {@code sideB}.
     */
    private static void blade(VertexConsumer vc, Matrix4f matrix, Vec3 axis, Vec3 sideA, Vec3 sideB, float reach, float start, float alpha) {
        int steps = 6;
        Vec3[] center = new Vec3[steps + 1];
        double[] half = new double[steps + 1];
        for (int j = 0; j <= steps; j++) {
            double t = j / (double) steps;
            double rho = reach * (0.3D + 0.75D * t);
            double angle = start + 1.1D * t;
            center[j] = sideA.scale(Math.cos(angle) * rho).add(sideB.scale(Math.sin(angle) * rho));
            half[j] = reach * 0.3D * Math.pow(1.0D - t, 0.65D) + reach * 0.015D;
        }
        Vec3[] left = new Vec3[steps + 1];
        Vec3[] right = new Vec3[steps + 1];
        for (int j = 0; j <= steps; j++) {
            Vec3 tangent = j < steps ? center[j + 1].subtract(center[j]) : center[j].subtract(center[j - 1]);
            Vec3 normal = axis.cross(tangent).normalize();
            left[j] = center[j].add(normal.scale(half[j]));
            right[j] = center[j].subtract(normal.scale(half[j]));
        }
        // Three flat layers a little apart along the axis: the wide glow at the back, the body, the white edge in front
        double apart = reach * 0.012D;
        for (int layer = 0; layer < 3; layer++) {
            Vec3 lift = axis.scale((layer - 1) * apart);
            for (int j = 0; j < steps; j++) {
                if (layer == 0) {
                    // The glow: wider than the blade
                    Vec3 gl0 = center[j].add(left[j].subtract(center[j]).scale(1.5D)).add(lift);
                    Vec3 gr0 = center[j].add(right[j].subtract(center[j]).scale(1.5D)).add(lift);
                    Vec3 gl1 = center[j + 1].add(left[j + 1].subtract(center[j + 1]).scale(1.5D)).add(lift);
                    Vec3 gr1 = center[j + 1].add(right[j + 1].subtract(center[j + 1]).scale(1.5D)).add(lift);
                    quad(vc, matrix, gl0, gr0, gr1, gl1, 0.18F, 0.55F, 1.0F, 0.22F * alpha);
                } else if (layer == 1) {
                    quad(vc, matrix, left[j].add(lift), right[j].add(lift), right[j + 1].add(lift), left[j + 1].add(lift), 0.45F, 0.82F, 1.0F, 0.6F * alpha);
                } else {
                    // The white edge, along the leading side of the blade
                    Vec3 edge = center[j].add(left[j].subtract(center[j]).scale(0.78D)).add(lift);
                    Vec3 edge1 = center[j + 1].add(left[j + 1].subtract(center[j + 1]).scale(0.78D)).add(lift);
                    quad(vc, matrix, left[j].add(lift), edge, edge1, left[j + 1].add(lift), 1.0F, 1.0F, 1.0F, 0.95F * alpha);
                }
            }
        }
    }

    /**
     * A round ball of only a few flat facets, seen from outside and from inside. Every facet has a shade of its own, so
     * the ball looks cut from blocks. {@code seed} decides the shades.
     */
    private static void facetSphere(VertexConsumer vc, Matrix4f matrix, float radius, float r, float g, float b, float alpha, int seed) {
        int segments = 10;
        int rings = 7;
        Vec3[][] grid = new Vec3[rings + 1][segments + 1];
        for (int j = 0; j <= rings; j++) {
            double lat = Math.PI * (j / (double) rings - 0.5D);
            for (int i = 0; i <= segments; i++) {
                double lon = Math.PI * 2.0D * i / segments;
                grid[j][i] = new Vec3(Math.cos(lat) * Math.cos(lon) * radius, Math.sin(lat) * radius, Math.cos(lat) * Math.sin(lon) * radius);
            }
        }
        for (int j = 0; j < rings; j++) {
            for (int i = 0; i < segments; i++) {
                float shade = 0.75F + 0.25F * hash(seed * 101 + j * 13 + i);
                quad(vc, matrix, grid[j][i], grid[j][i + 1], grid[j + 1][i + 1], grid[j + 1][i], r * shade, g * shade, b * shade, alpha);
            }
        }
    }

    /** One flat coloured quad, drawn from both sides (this render type does not draw the back of a face). */
    private static void quad(VertexConsumer vc, Matrix4f matrix, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float r, float g, float b, float alpha) {
        vertex(vc, matrix, p0, r, g, b, alpha);
        vertex(vc, matrix, p1, r, g, b, alpha);
        vertex(vc, matrix, p2, r, g, b, alpha);
        vertex(vc, matrix, p3, r, g, b, alpha);
        vertex(vc, matrix, p3, r, g, b, alpha);
        vertex(vc, matrix, p2, r, g, b, alpha);
        vertex(vc, matrix, p1, r, g, b, alpha);
        vertex(vc, matrix, p0, r, g, b, alpha);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float r, float g, float b, float alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z).setColor(r, g, b, alpha);
    }

    /** A pseudo random number from 0 to 1 for a whole number, always the same for the same number. */
    private static float hash(int n) {
        int h = n * 374761393 + 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(ChakraSphereEntity entity) {
        return TEXTURE;
    }
}
