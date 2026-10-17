package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.main.Main;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws the {@link ChakraSphereEntity} with textures of the mod and glowing ribbons, there are no models and no
 * particles.
 * <ul>
 *   <li>Rasengan: a soft round ball of pale blue and white, made of many puffs of wind that turn round each other.
 *       It is drawn in the hand of the caster while it is charged.</li>
 *   <li>Rasenshuriken: held up above the head of the caster while it is charged, as a flat white disc of swirling wind
 *       that spins and grows. When it is thrown it is a bright white ball with shards of wind cutting round its edge,
 *       and where it hits it grows into a huge white ball with shards flying round it.</li>
 * </ul>
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final ResourceLocation PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_puff.png");
    private static final ResourceLocation DISC_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_disc.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float[] HOT = {1.0F, 1.0F, 1.0F};
    /** The edge of the streaks and shards of the Rasenshuriken: white with a touch of green and blue. */
    private static final float[] WHITE_EDGE = {0.8F, 1.0F, 0.95F};

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
        float charge = Mth.clamp(phaseAge / (shuriken ? ChakraSphereEntity.RASENSHURIKEN_CHARGE_TICKS : ChakraSphereEntity.RASENGAN_CHARGE_TICKS), 0.0F, 1.0F);

        // In the hand of the caster while charging (and for the Rasengan while it is driven forward), and otherwise
        // where it is
        poseStack.pushPose();
        if (phase == ChakraSphereEntity.CHARGING || (!shuriken && phase == ChakraSphereEntity.ACTIVE)) {
            Entity owner = entity.level().getEntity(entity.getOwnerId());
            if (owner != null) {
                Vec3 hand = ChakraSphereEntity.handPosition(owner, entity.getKind(), partialTick);
                Vec3 here = entity.getPosition(partialTick);
                poseStack.translate(hand.x - here.x, hand.y - here.y, hand.z - here.z);
            }
        }

        if (shuriken) renderRasenshuriken(phase, phaseAge, charge, age, poseStack, bufferSource);
        else renderRasengan(phase, phaseAge, charge, age, poseStack, bufferSource);
        poseStack.popPose();
    }

    private void renderRasengan(int phase, float phaseAge, float charge, float age, PoseStack poseStack, MultiBufferSource bufferSource) {
        float radius;
        float alpha = 1.0F;
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
        if (alpha < 0.01F) return;
        puffBall(bufferSource, poseStack, radius, age, alpha, 1.0F);
    }

    private void renderRasenshuriken(int phase, float phaseAge, float charge, float age, PoseStack poseStack, MultiBufferSource bufferSource) {
        if (phase == ChakraSphereEntity.CHARGING) {
            // A flat disc of swirling white wind, held up above the head, that grows and spins faster as it is charged
            float size = 0.4F + 2.8F * charge;
            if (charge >= 1.0F) size *= 1.0F + 0.05F * Mth.sin(age * 1.3F);
            disc(bufferSource, poseStack, size, age * (0.25F + 0.35F * charge), 1.0F);
            puffBall(bufferSource, poseStack, 0.1F + 0.2F * charge, age, 0.9F, 0.2F);
            return;
        }
        boolean blast = phase == ChakraSphereEntity.IMPACT;
        float radius = 1.3F;
        float alpha = 1.0F;
        if (blast) {
            float grown = Mth.clamp(phaseAge / ChakraSphereEntity.SHURIKEN_GROW_TICKS, 0.0F, 1.0F);
            grown = grown * grown * (3.0F - 2.0F * grown);
            radius = Mth.lerp(grown, 1.3F, (float) ChakraSphereEntity.SHURIKEN_RADIUS);
            alpha = Mth.clamp((ChakraSphereEntity.SHURIKEN_BLAST_TICKS - phaseAge) / 6.0F, 0.0F, 1.0F);
        }
        if (alpha < 0.01F) return;

        // A bright white ball, and shards of wind cutting round its edge. The ball first: asking for another kind of
        // buffer finishes the one before it, so every buffer is asked for right before it is used.
        puffBall(bufferSource, poseStack, radius, age, alpha, 0.15F);
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        float scale = radius / 0.35F;
        int shards = blast ? 44 : 30;
        for (int i = 0; i < shards; i++) {
            streak(vc, matrix, i, age, radius * (0.95F + 0.3F * hash(i * 11 + 9)), 1.0F, 8,
                    0.25D + 0.35D * hash(i * 7 + 4), 0.5D + 0.5D * hash(i * 7 + 6), 0.012F * scale, alpha);
        }
        // A few long, thin white arcs sweeping round the whole thing
        for (int i = 0; i < 3; i++) {
            streak(vc, matrix, 200 + i, age, radius * (1.5F + 0.5F * hash(i * 13 + 3)), 0.5F, 30,
                    0.08D + 0.06D * hash(i * 13 + 5), 2.6D + 0.8D * hash(i * 13 + 7), 0.03F * scale, 0.9F * alpha);
        }
    }

    /**
     * A flat disc lying flat in the air, with the texture of swirling wind on it, from both sides. A second, smaller
     * one turns the other way a little above it, so it never looks the same.
     */
    private void disc(MultiBufferSource bufferSource, PoseStack poseStack, float size, float spin, float alpha) {
        RenderType type = RenderType.entityTranslucentEmissive(DISC_TEXTURE);
        VertexConsumer vc = bufferSource.getBuffer(type);
        for (int k = 0; k < 2; k++) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.02D * k, 0.0D);
            poseStack.mulPose(Axis.YP.rotation(k == 0 ? spin : -spin * 1.3F + 1.0F));
            float h = size * (k == 0 ? 0.5F : 0.4F);
            float a = alpha * (k == 0 ? 0.95F : 0.7F);
            Matrix4f m = poseStack.last().pose();
            for (int side = 0; side < 2; side++) {
                float s = side == 0 ? 1.0F : -1.0F;
                texturedVertex(vc, m, -h, 0.0F, -h * s, 0.0F, 0.0F, a);
                texturedVertex(vc, m, h, 0.0F, -h * s, 1.0F, 0.0F, a);
                texturedVertex(vc, m, h, 0.0F, h * s, 1.0F, 1.0F, a);
                texturedVertex(vc, m, -h, 0.0F, h * s, 0.0F, 1.0F, a);
            }
            poseStack.popPose();
        }
    }

    private static void texturedVertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z, float u, float v, float alpha) {
        vc.addVertex(matrix, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    /**
     * A ball of about 44 soft puffs of wind, spread through a sphere and turning round it, the ones in the middle
     * faster. Together they make a soft, round, glowing ball that is white in the middle and, by {@code blue}, pale
     * blue at its edge. The puffs are drawn from the far side to the near side, so that they blend into each other.
     */
    private void puffBall(MultiBufferSource bufferSource, PoseStack poseStack, float radius, float age, float alpha, float blue) {
        int count = 44;
        Quaternionf camera = this.entityRenderDispatcher.cameraOrientation();
        Vector3f forward = camera.transform(new Vector3f(0.0F, 0.0F, -1.0F));
        double[][] puff = new double[count][8]; // x, y, z, size, depth, t, roll
        for (int i = 0; i < count; i++) {
            double theta = hash(i * 5 + 1) * Math.PI * 2.0D;
            double z = hash(i * 5 + 2) * 2.0D - 1.0D;
            double ring = Math.sqrt(1.0D - z * z);
            double t = Math.cbrt(0.1D + 0.9D * hash(i * 5 + 3));
            double spin = age * (0.12D + 0.38D * (1.0D - t)) * (i % 2 == 0 ? 1.0D : -0.7D);
            double x0 = Math.cos(theta) * ring;
            double y0 = z;
            double z0 = Math.sin(theta) * ring;
            double x1 = x0 * Math.cos(spin) - z0 * Math.sin(spin);
            double z1 = x0 * Math.sin(spin) + z0 * Math.cos(spin);
            puff[i][0] = x1 * radius * t * 0.85D;
            puff[i][1] = y0 * radius * t * 0.85D;
            puff[i][2] = z1 * radius * t * 0.85D;
            puff[i][3] = radius * (0.5D + 0.4D * hash(i * 5 + 4)) * (0.8D + 0.4D * t);
            puff[i][4] = puff[i][0] * forward.x + puff[i][1] * forward.y + puff[i][2] * forward.z;
            puff[i][5] = t;
            puff[i][6] = hash(i * 5 + 5) * 4.0D;
        }
        Integer[] order = new Integer[count];
        for (int i = 0; i < count; i++) order[i] = i;
        // The far ones first
        java.util.Arrays.sort(order, (p, q) -> Double.compare(puff[q][4], puff[p][4]));
        RenderType type = RenderType.entityTranslucentEmissive(PUFF_TEXTURE);
        VertexConsumer vc = bufferSource.getBuffer(type);
        for (int n = 0; n < count; n++) {
            double[] p = puff[order[n]];
            float t = (float) p[5];
            poseStack.pushPose();
            poseStack.translate(p[0], p[1], p[2]);
            poseStack.mulPose(camera);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * (int) p[6]));
            puffQuad(vc, poseStack.last().pose(), (float) p[3], 0.5F * alpha * (1.0F - 0.25F * t),
                    1.0F - 0.25F * t * blue, 1.0F - 0.1F * t * blue, 1.0F);
            poseStack.popPose();
        }
        // A bright glow in the middle
        poseStack.pushPose();
        poseStack.mulPose(camera);
        puffQuad(vc, poseStack.last().pose(), radius * 1.15F, 0.45F * alpha, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    /** A square facing the camera (the pose is already turned to it) with the whole puff on it. */
    private static void puffQuad(VertexConsumer vc, Matrix4f matrix, float size, float alpha, float red, float green, float blue) {
        float h = size * 0.5F;
        puffVertex(vc, matrix, -h, -h, 0.0F, 1.0F, red, green, blue, alpha);
        puffVertex(vc, matrix, h, -h, 1.0F, 1.0F, red, green, blue, alpha);
        puffVertex(vc, matrix, h, h, 1.0F, 0.0F, red, green, blue, alpha);
        puffVertex(vc, matrix, -h, h, 0.0F, 0.0F, red, green, blue, alpha);
    }

    private static void puffVertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float u, float v,
                                   float red, float green, float blue, float alpha) {
        vc.addVertex(matrix, x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
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
     * A ribbon along {@code path} that is bright in the middle and fades to nothing at its edges, thin and clear at
     * its tail and bright at its head. It is drawn twice, across each other, so it can be seen from every side.
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
                quad(vc, matrix, l0, WHITE_EDGE, 0.0F, path[j], HOT, a0, path[j + 1], HOT, a1, l1, WHITE_EDGE, 0.0F);
                quad(vc, matrix, path[j], HOT, a0, r0, WHITE_EDGE, 0.0F, r1, WHITE_EDGE, 0.0F, path[j + 1], HOT, a1);
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
