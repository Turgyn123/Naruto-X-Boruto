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
 * Draws the {@link ChakraSphereEntity} the way the lightning of the Lightning Chakra Mode is drawn, but for wind: with
 * no model and no texture, only glowing ribbons that are added to the light of the world, as in the anime.
 * <ul>
 *   <li>Rasengan: a white hot heart in a blue ball, with bands of fine streaks that spin round it fast, as seen in the
 *       anime when the ball faces you, and thin wisps of wind flying off it.</li>
 *   <li>Rasenshuriken: the same ball with four big curved blades of wind spinning round it, held up above the head of
 *       the caster and facing the way they look. Thrown, it flies with the blades spinning.</li>
 *   <li>Where the Rasenshuriken hits it grows into a huge ball of streaks with three sets of blades spinning inside.</li>
 * </ul>
 * While it is charged it is drawn at the hand of the caster, so it does not lag behind them. Every ribbon is bright in
 * the middle and fades to nothing at its edges, so no layers are needed on top of each other.
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final float[] HOT = {1.0F, 1.0F, 1.0F};
    private static final float[] COOL = {0.35F, 0.72F, 1.0F};
    private static final float[] BODY = {0.55F, 0.85F, 1.0F};

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
                radius = 0.08F + 0.27F * charge;
                if (charge >= 1.0F) radius *= 1.0F + 0.07F * Mth.sin(age * 1.3F);
            } else if (phase == ChakraSphereEntity.ACTIVE) {
                radius = 0.36F;
            } else {
                float t = Mth.clamp(phaseAge / ChakraSphereEntity.RASENGAN_BURST_TICKS, 0.0F, 1.0F);
                radius = 0.36F + 1.7F * t;
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

        // The ball: a white hot heart, a softer middle and a blue edge. It is drawn first, so the streaks behind it
        // are hidden by it and the ones in front of it show.
        if (!blast) {
            sphere(vc, matrix, radius * 0.38F, 1.0F, 1.0F, 1.0F, 0.95F * alpha);
            sphere(vc, matrix, radius * 0.68F, 0.65F, 0.92F, 1.0F, 0.55F * alpha);
            sphere(vc, matrix, radius * 0.94F, 0.3F, 0.65F, 1.0F, 0.4F * alpha);
            // Bands of fine streaks spinning round the aim, fastest round the middle of the ball, so that from behind it
            // is a vortex and from the side it is a ball with bands, like the Rasengan of the anime
            for (int ring = 0; ring < 11; ring++) {
                double lat = Math.toRadians(-75.0D + 15.0D * ring);
                for (int k = 0; k < 3; k++) {
                    band(vc, matrix, aim, across, up, radius * 1.02F, lat, age, ring * 3 + k, k * (Math.PI * 2.0D / 3.0D), 0.014F * scale, alpha);
                }
            }
            // Thin wisps of wind fly off it
            int wisps = shuriken ? 10 : 6;
            for (int i = 0; i < wisps; i++) {
                streak(vc, matrix, 100 + i, age, radius * (1.5F + 1.9F * hash(i * 13 + 3)), 0.6F, 30,
                        0.05D + 0.07D * hash(i * 13 + 5), 2.4D + 1.6D * hash(i * 13 + 7), 0.02F * scale, 0.7F * alpha);
            }
        } else {
            sphere(vc, matrix, radius, 0.35F, 0.7F, 1.0F, 0.07F * alpha);
            for (int i = 0; i < 36; i++) {
                streak(vc, matrix, i, age, radius * (0.55F + 0.45F * hash(i * 11 + 9)), 1.0F, 14,
                        0.2D + 0.3D * hash(i * 7 + 4), 1.6D + 1.4D * hash(i * 7 + 6), 0.03F * scale, alpha);
            }
        }

        // The four big blades of the Rasenshuriken, in the plane across the way it is aimed
        if (shuriken) {
            int sets = blast ? 3 : 1;
            for (int k = 0; k < sets; k++) {
                Vec3 axis = k == 0 ? aim : (k == 1 ? across : up);
                Vec3 sideA = k == 0 ? across : (k == 1 ? up : aim);
                Vec3 sideB = axis.cross(sideA).normalize();
                float spin = age * (0.5F + 0.12F * k) * (k == 1 ? -1.0F : 1.0F);
                for (int blade = 0; blade < 4; blade++) {
                    blade(vc, matrix, axis, sideA, sideB, reach, spin + blade * (float) (Math.PI / 2.0D), alpha * (blast ? 0.7F : 1.0F));
                }
            }
        }
        poseStack.popPose();
    }

    /**
     * A band of streaks: one streak round a circle that lies across the aim, at the height {@code lat} of the ball (so
     * the circle is smaller towards the ends of the ball). The streaks of the same band spin the same way and, with a
     * touch of a spiral in them, make the vortex.
     */
    private static void band(VertexConsumer vc, Matrix4f matrix, Vec3 aim, Vec3 across, Vec3 up, float radius, double lat, float age,
                             int id, double phase, float halfWidth, float alpha) {
        double along = Math.sin(lat) * radius;
        double ring = Math.cos(lat) * radius;
        double speed = 0.12D + 0.42D * Math.cos(lat) + 0.05D * hash(id * 3 + 1);
        double span = 1.1D + 0.7D * hash(id * 3 + 2);
        double head = phase + age * speed + hash(id * 3 + 3) * Math.PI * 2.0D;
        int points = 14;
        Vec3[] path = new Vec3[points + 1];
        for (int j = 0; j <= points; j++) {
            double angle = head - span * (1.0D - j / (double) points);
            double spiral = 1.0D + 0.05D * Math.sin(angle * 2.0D + age * 0.2D);
            path[j] = aim.scale(along).add(across.scale(Math.cos(angle) * ring * spiral)).add(up.scale(Math.sin(angle) * ring * spiral));
        }
        ribbon(vc, matrix, path, halfWidth, alpha);
    }

    /**
     * One streak of wind that winds round the middle of the ball in a circle (or an oval, when {@code flatten} is less
     * than 1) of {@code size}, in a plane of its own. It has a bright head and a tail that fades away.
     */
    private static void streak(VertexConsumer vc, Matrix4f matrix, int i, float age, float size, float flatten, int points,
                               double speed, double span, float halfWidth, float alpha) {
        double theta = hash(i * 7 + 1) * Math.PI * 2.0D;
        double z = hash(i * 7 + 2) * 2.0D - 1.0D;
        double ring = Math.sqrt(1.0D - z * z);
        Vec3 normal = new Vec3(Math.cos(theta) * ring, z, Math.sin(theta) * ring);
        Vec3 a = normal.cross(new Vec3(0.0D, 1.0D, 0.0D));
        a = a.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : a.normalize();
        Vec3 b = normal.cross(a).normalize();
        double sign = hash(i * 7 + 3) < 0.5F ? -1.0D : 1.0D;
        double head = hash(i * 7 + 5) * Math.PI * 2.0D + age * speed * sign;
        Vec3[] path = new Vec3[points + 1];
        for (int j = 0; j <= points; j++) {
            double angle = head - sign * span * (1.0D - j / (double) points);
            double wobble = 1.0D + 0.06D * Math.sin(angle * 3.0D + age * 0.3D + i);
            path[j] = a.scale(Math.cos(angle) * size * wobble).add(b.scale(Math.sin(angle) * size * wobble * flatten));
        }
        ribbon(vc, matrix, path, halfWidth, alpha);
    }

    /**
     * A flat curved blade that sweeps out from the middle: wide near the middle and thin at the tip, bright along its
     * middle and fading at its edges. It lies in the plane of {@code sideA} and {@code sideB}.
     */
    private static void blade(VertexConsumer vc, Matrix4f matrix, Vec3 axis, Vec3 sideA, Vec3 sideB, float reach, float start, float alpha) {
        int steps = 14;
        Vec3[] center = new Vec3[steps + 1];
        double[] half = new double[steps + 1];
        for (int j = 0; j <= steps; j++) {
            double t = j / (double) steps;
            double rho = reach * (0.3D + 0.75D * t);
            double angle = start + 1.1D * t;
            center[j] = sideA.scale(Math.cos(angle) * rho).add(sideB.scale(Math.sin(angle) * rho));
            half[j] = reach * 0.3D * Math.pow(1.0D - t, 0.65D) + reach * 0.015D;
        }
        for (int j = 0; j < steps; j++) {
            Vec3 tangent = center[j + 1].subtract(center[j]);
            if (tangent.lengthSqr() < 1.0E-8D) continue;
            Vec3 normal = axis.cross(tangent).normalize();
            Vec3 normal2 = j + 2 <= steps ? axis.cross(center[j + 2].subtract(center[j + 1])).normalize() : normal;
            float head = 1.0F - 0.55F * (j / (float) steps);
            float head2 = 1.0F - 0.55F * ((j + 1) / (float) steps);
            Vec3 l0 = center[j].add(normal.scale(half[j]));
            Vec3 r0 = center[j].subtract(normal.scale(half[j]));
            Vec3 l1 = center[j + 1].add(normal2.scale(half[j + 1]));
            Vec3 r1 = center[j + 1].subtract(normal2.scale(half[j + 1]));
            // A solid pale blue blade with a white edge
            quad(vc, matrix, l0, HOT, 0.95F * head * alpha, center[j], BODY, 0.55F * head * alpha,
                    center[j + 1], BODY, 0.55F * head2 * alpha, l1, HOT, 0.95F * head2 * alpha);
            quad(vc, matrix, center[j], BODY, 0.55F * head * alpha, r0, HOT, 0.95F * head * alpha,
                    r1, HOT, 0.95F * head2 * alpha, center[j + 1], BODY, 0.55F * head2 * alpha);
        }
    }

    /**
     * A streak: a ribbon along {@code path} that is bright in the middle and fades to nothing at its edges, thin and
     * clear at its tail and bright at its head. It is drawn twice, across each other, so it can be seen from every
     * side.
     */
    private static void ribbon(VertexConsumer vc, Matrix4f matrix, Vec3[] path, float halfWidth, float alpha) {
        int n = path.length - 1;
        for (int j = 0; j < n; j++) {
            Vec3 direction = path[j + 1].subtract(path[j]);
            if (direction.lengthSqr() < 1.0E-10D) continue;
            direction = direction.normalize();
            Vec3 ref = Math.abs(direction.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 u = direction.cross(ref).normalize();
            Vec3 w = direction.cross(u).normalize();
            float t0 = j / (float) n;
            float t1 = (j + 1) / (float) n;
            float a0 = alpha * t0 * t0;
            float a1 = alpha * t1 * t1;
            double w0 = halfWidth * (0.25D + 0.75D * t0);
            double w1 = halfWidth * (0.25D + 0.75D * t1);
            for (Vec3 side : new Vec3[]{u, w}) {
                Vec3 l0 = path[j].add(side.scale(w0));
                Vec3 r0 = path[j].subtract(side.scale(w0));
                Vec3 l1 = path[j + 1].add(side.scale(w1));
                Vec3 r1 = path[j + 1].subtract(side.scale(w1));
                quad(vc, matrix, l0, COOL, 0.0F, path[j], HOT, a0, path[j + 1], HOT, a1, l1, COOL, 0.0F);
                quad(vc, matrix, path[j], HOT, a0, r0, COOL, 0.0F, r1, COOL, 0.0F, path[j + 1], HOT, a1);
            }
        }
    }

    /** A round ball of one colour, seen from the outside and from the inside. */
    private static void sphere(VertexConsumer vc, Matrix4f matrix, float radius, float r, float g, float b, float alpha) {
        int segments = 14;
        int rings = 9;
        float[] color = {r, g, b};
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
                quad(vc, matrix, grid[j][i], color, alpha, grid[j][i + 1], color, alpha,
                        grid[j + 1][i + 1], color, alpha, grid[j + 1][i], color, alpha);
            }
        }
    }

    /** One quad of glowing colour, drawn from both sides (this render type does not draw the back of a face). */
    private static void quad(VertexConsumer vc, Matrix4f matrix, Vec3 p0, float[] c0, float a0, Vec3 p1, float[] c1, float a1,
                             Vec3 p2, float[] c2, float a2, Vec3 p3, float[] c3, float a3) {
        vertex(vc, matrix, p0, c0, a0);
        vertex(vc, matrix, p1, c1, a1);
        vertex(vc, matrix, p2, c2, a2);
        vertex(vc, matrix, p3, c3, a3);
        vertex(vc, matrix, p3, c3, a3);
        vertex(vc, matrix, p2, c2, a2);
        vertex(vc, matrix, p1, c1, a1);
        vertex(vc, matrix, p0, c0, a0);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float[] c, float alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z).setColor(c[0], c[1], c[2], alpha);
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
