package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

/**
 * Draws the {@link ChakraSphereEntity} in the style of the wind of the Giant Fan (see {@code WeaponEffectRenderer}): with
 * the same textures, the white streaks of wind (wind_streak) on ribbons that spiral and scroll, and the white crescent
 * blades (crescent), in the same near white colour.
 * <ul>
 *   <li>Rasengan: a white heart in a soft blue ball, held by three flat bright rings that spin on different axes
 *       (see {@link #rasenganRings}).</li>
 *   <li>Rasenshuriken: held up above the head of the caster while it is charged. The same ball, with four big
 *       crescent blades like the blades of the fan turning round it in a pinwheel. Thrown, ribbons of wind spiral
 *       behind it like the wind of the fan. Where it hits it grows into a huge ball of ribbons with pinwheels turning
 *       in three planes.</li>
 * </ul>
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation WIND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_streak.png");
    private static final ResourceLocation CRESCENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/crescent.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    /** The colour of the wind of the Giant Fan: white with a touch of blue. */
    private static final float[] WIND = {0.94F, 0.96F, 1.0F};

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

        Matrix4f matrix = poseStack.last().pose();
        if (!shuriken) {
            rasenganRings(bufferSource, matrix, radius, age, alpha);
            poseStack.popPose();
            return;
        }
        float ribbonAlpha = 0.94F * alpha;

        // The soft heart of the ball: a translucent ball with a white core in it
        RenderType whiteType = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);
        VertexConsumer white = bufferSource.getBuffer(whiteType);
        if (!blast) {
            roundBall(white, matrix, radius * 0.97F, 0.72F, 0.88F, 1.0F, 0.3F * alpha);
            roundBall(white, matrix, radius * 0.4F, 1.0F, 1.0F, 1.0F, 0.85F * alpha);
        } else {
            roundBall(white, matrix, radius, 0.72F, 0.88F, 1.0F, 0.1F * alpha);
        }

        // The ribbons of wind spiralling round the ball, as in the wind of the Giant Fan
        VertexConsumer wind = bufferSource.getBuffer(RenderType.entityTranslucent(WIND_TEXTURE));
        int ribbons = blast ? 14 : 8;
        windBall(wind, matrix, aim, across, up, radius, age, ribbons, 1.0F, ribbonAlpha);
        windBall(wind, matrix, aim, across, up, radius * 0.7F, age, ribbons - 2, -1.3F, ribbonAlpha * 0.9F);
        // Thrown, ribbons of wind spiral behind the Rasenshuriken like the wind of the fan
        if (shuriken && phase == ChakraSphereEntity.ACTIVE) {
            for (int r = 0; r < 4; r++) {
                trail(wind, matrix, aim, across, up, reach, age, r, ribbonAlpha);
            }
        }

        // The four big crescent blades of the Rasenshuriken, across the way it is aimed
        if (shuriken) {
            VertexConsumer crescent = bufferSource.getBuffer(RenderType.entityTranslucent(CRESCENT_TEXTURE));
            int sets = blast ? 3 : 1;
            for (int k = 0; k < sets; k++) {
                Vec3 axis = k == 0 ? aim : (k == 1 ? across : up);
                Vec3 sideA = k == 0 ? across : (k == 1 ? up : aim);
                Vec3 sideB = axis.cross(sideA).normalize();
                float spin = age * (0.35F + 0.1F * k) * (k == 1 ? -1.0F : 1.0F);
                for (int blade = 0; blade < 4; blade++) {
                    crescentBlade(crescent, matrix, axis, sideA, sideB, reach, spin + blade * (float) (Math.PI / 2.0D), alpha * (blast ? 0.7F : 0.95F));
                }
            }
        }
        poseStack.popPose();
    }

    /**
     * The Rasengan: a glowing white heart in a soft blue ball, held together by three flat bright rings that spin on
     * different axes, like the rings of a gyroscope or of an atom, so the ball seems to be held by turning bands of
     * energy. Everything is flat colour with hard edges.
     */
    private void rasenganRings(MultiBufferSource bufferSource, Matrix4f matrix, float radius, float age, float alpha) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WHITE_TEXTURE));
        roundBall(vc, matrix, radius * 0.38F, 1.0F, 1.0F, 1.0F, 0.95F * alpha);
        roundBall(vc, matrix, radius * 0.8F, 0.4F, 0.72F, 1.0F, 0.32F * alpha);
        float[][] colour = {{1.0F, 1.0F, 1.0F}, {0.62F, 0.9F, 1.0F}, {0.35F, 0.7F, 1.0F}};
        for (int k = 0; k < 3; k++) {
            double tilt = Math.toRadians(25.0D + 55.0D * k);
            double spin = age * (0.22D + 0.1D * k) * (k == 1 ? -1.0D : 1.0D) + k * 2.1D;
            // The flat plane of the ring: tipped over by tilt, then turned round the up axis by spin
            Vec3 u = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 v = new Vec3(0.0D, Math.sin(tilt), Math.cos(tilt));
            u = new Vec3(u.x * Math.cos(spin) - u.z * Math.sin(spin), u.y, u.x * Math.sin(spin) + u.z * Math.cos(spin));
            v = new Vec3(v.x * Math.cos(spin) - v.z * Math.sin(spin), v.y, v.x * Math.sin(spin) + v.z * Math.cos(spin));
            double outer = radius * (1.0D + 0.14D * k);
            double inner = outer * 0.86D;
            int segments = 20;
            for (int i = 0; i < segments; i++) {
                double a0 = Math.PI * 2.0D * i / segments;
                double a1 = Math.PI * 2.0D * (i + 1) / segments;
                Vec3 o0 = u.scale(Math.cos(a0) * outer).add(v.scale(Math.sin(a0) * outer));
                Vec3 o1 = u.scale(Math.cos(a1) * outer).add(v.scale(Math.sin(a1) * outer));
                Vec3 i0 = u.scale(Math.cos(a0) * inner).add(v.scale(Math.sin(a0) * inner));
                Vec3 i1 = u.scale(Math.cos(a1) * inner).add(v.scale(Math.sin(a1) * inner));
                Vec3[] corner = {o0, o1, i1, i0};
                // Every other piece is brighter, so the turning of the ring can be seen
                float shade = i % 2 == 0 ? 1.0F : 0.7F;
                for (int c = 0; c < 4; c++) flatVertex(vc, matrix, corner[c], colour[k][0] * shade, colour[k][1] * shade, colour[k][2] * shade, 0.9F * alpha);
                for (int c = 3; c >= 0; c--) flatVertex(vc, matrix, corner[c], colour[k][0] * shade, colour[k][1] * shade, colour[k][2] * shade, 0.9F * alpha);
            }
        }
    }

    /**
     * Ribbons of wind that spiral over the surface of a ball from one end of it (towards the way it is aimed) to the
     * other, and turn round it. The texture of the wind streaks runs along the ribbons and scrolls.
     */
    private static void windBall(VertexConsumer vc, Matrix4f matrix, Vec3 aim, Vec3 across, Vec3 up, float radius, float age,
                                 int ribbons, float turnSpeed, float alpha) {
        int segments = 14;
        for (int r = 0; r < ribbons; r++) {
            float offset = r * (float) (Math.PI * 2.0D / ribbons);
            Vec3 previous = null;
            Vec3 previousSide = null;
            float previousT = 0.0F;
            for (int s = 0; s <= segments; s++) {
                float t = s / (float) segments;
                double polar = Math.PI * t;
                double azimuth = offset + t * Math.PI * 3.0D + age * 0.18D * turnSpeed;
                Vec3 radial = aim.scale(Math.cos(polar))
                        .add(across.scale(Math.sin(polar) * Math.cos(azimuth)))
                        .add(up.scale(Math.sin(polar) * Math.sin(azimuth)));
                Vec3 point = radial.scale(radius);
                // Across the ribbon, along the surface of the ball
                double nextPolar = Math.PI * Math.min(1.0F, t + 0.02F);
                double nextAzimuth = offset + Math.min(1.0F, t + 0.02F) * Math.PI * 3.0D + age * 0.18D * turnSpeed;
                Vec3 nextRadial = aim.scale(Math.cos(nextPolar))
                        .add(across.scale(Math.sin(nextPolar) * Math.cos(nextAzimuth)))
                        .add(up.scale(Math.sin(nextPolar) * Math.sin(nextAzimuth)));
                Vec3 tangent = nextRadial.subtract(radial);
                Vec3 side = radial.cross(tangent);
                side = side.lengthSqr() < 1.0E-10D ? up : side.normalize();
                side = side.scale(radius * 0.17D * (0.4D + 0.6D * Math.sin(Math.PI * t)));
                if (previous != null) {
                    float a0 = alpha * Math.min(1.0F, Math.min(previousT * 5.0F, (1.0F - previousT) * 5.0F));
                    float a1 = alpha * Math.min(1.0F, Math.min(t * 5.0F, (1.0F - t) * 5.0F));
                    float u0 = previousT * 2.0F - age * 0.12F + r * 0.33F;
                    float u1 = t * 2.0F - age * 0.12F + r * 0.33F;
                    texturedVertex(vc, matrix, previous.add(previousSide), u0, 0.0F, a0);
                    texturedVertex(vc, matrix, previous.subtract(previousSide), u0, 1.0F, a0);
                    texturedVertex(vc, matrix, point.subtract(side), u1, 1.0F, a1);
                    texturedVertex(vc, matrix, point.add(side), u1, 0.0F, a1);
                }
                previous = point;
                previousSide = side;
                previousT = t;
            }
        }
    }

    /** A ribbon of wind that spirals around the way the Rasenshuriken flies, behind it, like the wind of the fan. */
    private static void trail(VertexConsumer vc, Matrix4f matrix, Vec3 aim, Vec3 across, Vec3 up, float reach, float age, int r, float alpha) {
        int segments = 12;
        float length = 4.0F;
        Vec3[] inner = new Vec3[segments + 1];
        Vec3[] outer = new Vec3[segments + 1];
        for (int s = 0; s <= segments; s++) {
            float t = s / (float) segments;
            double angle = r * Math.PI / 2.0D + t * 5.0D + age * 0.9D;
            double ring = reach * (0.25D + 0.4D * t);
            double width = reach * 0.18D * (0.5D + t);
            Vec3 centre = aim.scale(-t * length).add(across.scale(Math.cos(angle)).add(up.scale(Math.sin(angle))).scale(ring));
            Vec3 outward = across.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            inner[s] = centre.subtract(outward.scale(width * 0.5D));
            outer[s] = centre.add(outward.scale(width * 0.5D));
        }
        for (int s = 0; s < segments; s++) {
            float t0 = s / (float) segments;
            float t1 = (s + 1) / (float) segments;
            float a0 = alpha * Math.min(1.0F, Math.min(t0 * 5.0F, (1.0F - t0) * 5.0F)) * 0.8F;
            float a1 = alpha * Math.min(1.0F, Math.min(t1 * 5.0F, (1.0F - t1) * 5.0F)) * 0.8F;
            float u0 = t0 * 2.0F - age * 0.12F + r * 0.33F;
            float u1 = t1 * 2.0F - age * 0.12F + r * 0.33F;
            texturedVertex(vc, matrix, inner[s], u0, 0.0F, a0);
            texturedVertex(vc, matrix, outer[s], u0, 1.0F, a0);
            texturedVertex(vc, matrix, outer[s + 1], u1, 1.0F, a1);
            texturedVertex(vc, matrix, inner[s + 1], u1, 0.0F, a1);
        }
    }

    /**
     * One crescent blade like the blade of the fan: its curved side faces out and it is turned a little so the four of
     * them make a pinwheel. It lies in the plane of {@code sideA} and {@code sideB}, in three layers a little apart so
     * it has some thickness.
     */
    private static void crescentBlade(VertexConsumer vc, Matrix4f matrix, Vec3 axis, Vec3 sideA, Vec3 sideB, float reach, float angle, float alpha) {
        double tilt = angle + 0.6D;
        Vec3 radial = sideA.scale(Math.cos(tilt)).add(sideB.scale(Math.sin(tilt)));
        Vec3 tangent = sideA.scale(-Math.sin(tilt)).add(sideB.scale(Math.cos(tilt)));
        Vec3 centre = sideA.scale(Math.cos(angle)).add(sideB.scale(Math.sin(angle))).scale(reach * 0.6D);
        for (int layer = -1; layer <= 1; layer++) {
            Vec3 lift = axis.scale(layer * reach * 0.04D);
            double scale = layer == 0 ? 1.0D : 0.85D;
            double hw = reach * 0.5D * scale;
            double d = reach * 0.45D * scale;
            float layerAlpha = layer == 0 ? alpha : alpha * 0.55F;
            Vec3 c = centre.add(lift);
            texturedVertex(vc, matrix, c.subtract(tangent.scale(hw)).add(radial.scale(d)), 0.0F, 0.0F, layerAlpha);
            texturedVertex(vc, matrix, c.add(tangent.scale(hw)).add(radial.scale(d)), 1.0F, 0.0F, layerAlpha);
            texturedVertex(vc, matrix, c.add(tangent.scale(hw)).subtract(radial.scale(d)), 1.0F, 1.0F, layerAlpha);
            texturedVertex(vc, matrix, c.subtract(tangent.scale(hw)).subtract(radial.scale(d)), 0.0F, 1.0F, layerAlpha);
        }
    }

    /** A ball of only a few flat facets, seen from outside and from inside, of one colour. */
    private static void roundBall(VertexConsumer vc, Matrix4f matrix, float radius, float r, float g, float b, float alpha) {
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
                Vec3[] corner = {grid[j][i], grid[j][i + 1], grid[j + 1][i + 1], grid[j + 1][i]};
                for (int k = 0; k < 4; k++) flatVertex(vc, matrix, corner[k], r, g, b, alpha);
                for (int k = 3; k >= 0; k--) flatVertex(vc, matrix, corner[k], r, g, b, alpha);
            }
        }
    }

    private static void flatVertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float r, float g, float b, float alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, alpha)
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void texturedVertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v, float alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setColor(WIND[0], WIND[1], WIND[2], alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ChakraSphereEntity entity) {
        return WIND_TEXTURE;
    }
}
