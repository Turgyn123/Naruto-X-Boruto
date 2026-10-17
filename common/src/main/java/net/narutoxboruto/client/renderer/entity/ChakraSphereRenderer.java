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
import net.narutoxboruto.client.model.FireBallModel;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.main.Main;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws the {@link ChakraSphereEntity} in the style of the wind of the Giant Fan (see {@code WeaponEffectRenderer}): with
 * the same textures, the white streaks of wind (wind_streak) on ribbons that spiral and scroll, and the white crescent
 * blades (crescent), in the same near white colour.
 * <ul>
 *   <li>Rasengan: three looks to compare, the Rasengan (see {@link #glowBall}), Rasengan V2 (see {@link #puffBall}) and
 *       Rasengan V3 (see {@link #cubeBall}).</li>
 *   <li>Rasenshuriken: held up above the head of the caster while it is charged. The same ball, with four big
 *       crescent blades like the blades of the fan turning round it in a pinwheel. Thrown, ribbons of wind spiral
 *       behind it like the wind of the fan. Where it hits it grows into a huge ball of ribbons with pinwheels turning
 *       in three planes.</li>
 * </ul>
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation WIND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_streak.png");
    private static final ResourceLocation CRESCENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/crescent.png");
    // The textures of the three looks of the Rasengan
    private static final ResourceLocation SWIRL_BALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasen_swirl_ball.png");
    private static final ResourceLocation FLARE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasen_flare.png");
    private static final ResourceLocation PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasen_puff.png");
    private static final ResourceLocation CUBE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasengan_cube.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    /** The colour of the wind of the Giant Fan: white with a touch of blue. */
    private static final float[] WIND = {0.94F, 0.96F, 1.0F};

    private final FireBallModel cubes;

    public ChakraSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.cubes = new FireBallModel(context.bakeLayer(FireBallModel.LAYER_LOCATION));
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
        float ribbonAlpha = 0.94F * alpha;

        // The ball, the same for both jutsus
        ball(bufferSource, poseStack, matrix, radius, age, alpha, blast, shuriken ? 0 : entity.getVariant());

        // Thrown, ribbons of wind spiral behind the Rasenshuriken like the wind of the fan
        if (shuriken && phase == ChakraSphereEntity.ACTIVE) {
            VertexConsumer wind = bufferSource.getBuffer(RenderType.entityTranslucent(WIND_TEXTURE));
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

    /** The ball in the look of {@code variant}: 0 the glowing swirl ball, 1 the fluffy ball of puffs, 2 the cube model. */
    private void ball(MultiBufferSource bufferSource, PoseStack poseStack, Matrix4f matrix, float radius, float age, float alpha, boolean blast, int variant) {
        if (variant == 1) puffBall(bufferSource, poseStack, radius, age, alpha, blast);
        else if (variant == 2) cubeBall(bufferSource, poseStack, radius, age, alpha, blast);
        else glowBall(bufferSource, poseStack, matrix, radius, age, alpha, blast);
    }

    /**
     * Look 1, the glowing ball: a pale, see-through blue ball that is whiter in the middle, with a soft glowing edge made of
     * two bigger, very faint balls. Two balls with a texture of soft swirling bands turn in it on different axes in
     * different directions, so the swirl slides over itself, and a bright white heart shines in the middle with a flare
     * of rays that always faces you.
     */
    private void glowBall(MultiBufferSource bufferSource, PoseStack poseStack, Matrix4f matrix, float radius, float age, float alpha, boolean blast) {
        VertexConsumer white = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WHITE_TEXTURE));
        if (!blast) {
            roundBall(white, matrix, radius * 1.3F, 0.7F, 0.9F, 1.0F, 0.05F * alpha);
            roundBall(white, matrix, radius * 1.15F, 0.7F, 0.9F, 1.0F, 0.1F * alpha);
            roundBall(white, matrix, radius * 0.97F, 0.62F, 0.86F, 1.0F, 0.45F * alpha);
            roundBall(white, matrix, radius * 0.6F, 0.85F, 0.96F, 1.0F, 0.45F * alpha);
            roundBall(white, matrix, radius * 0.3F, 1.0F, 1.0F, 1.0F, 0.9F * alpha);
        } else {
            roundBall(white, matrix, radius, 0.3F, 0.65F, 1.0F, 0.1F * alpha);
        }
        VertexConsumer swirl = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(SWIRL_BALL_TEXTURE));
        texturedSphere(swirl, matrix, radius * 1.01F, age * 0.12F, 0.0F, 0.0F, alpha * (blast ? 0.5F : 0.85F));
        texturedSphere(swirl, matrix, radius * 0.88F, -age * 0.17F, 1.0F, 0.5F, alpha * (blast ? 0.4F : 0.6F));
        VertexConsumer flare = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FLARE_TEXTURE));
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotation(age * 0.03F));
        billboard(flare, poseStack.last().pose(), radius * (blast ? 1.2F : 3.4F), (blast ? 0.5F : 0.95F) * alpha);
        poseStack.popPose();
    }

    /**
     * Look 2, the fluffy ball: about 56 soft puffs of wind spread through a sphere and turning round it, the ones in the
     * middle faster, white in the middle and pale blue at the edge, drawn from the far side to the near side so they
     * blend into each other, with a small flare in the middle.
     */
    private void puffBall(MultiBufferSource bufferSource, PoseStack poseStack, float radius, float age, float alpha, boolean blast) {
        int count = blast ? 70 : 56;
        Quaternionf camera = this.entityRenderDispatcher.cameraOrientation();
        Vector3f forward = camera.transform(new Vector3f(0.0F, 0.0F, -1.0F));
        double[][] puff = new double[count][7]; // x, y, z, size, depth, t, roll
        for (int i = 0; i < count; i++) {
            double theta = hash(i * 5 + 1) * Math.PI * 2.0D;
            double z = hash(i * 5 + 2) * 2.0D - 1.0D;
            double ring = Math.sqrt(1.0D - z * z);
            double t = Math.cbrt(0.15D + 0.85D * hash(i * 5 + 3));
            double spin = age * (0.1D + 0.35D * (1.0D - t)) * (i % 2 == 0 ? 1.0D : -0.7D);
            double x0 = Math.cos(theta) * ring;
            double z0 = Math.sin(theta) * ring;
            puff[i][0] = (x0 * Math.cos(spin) - z0 * Math.sin(spin)) * radius * t * 0.85D;
            puff[i][1] = z * radius * t * 0.85D;
            puff[i][2] = (x0 * Math.sin(spin) + z0 * Math.cos(spin)) * radius * t * 0.85D;
            puff[i][3] = radius * (0.55D + 0.4D * hash(i * 5 + 4)) * (0.8D + 0.4D * t);
            puff[i][4] = puff[i][0] * forward.x + puff[i][1] * forward.y + puff[i][2] * forward.z;
            puff[i][5] = t;
            puff[i][6] = hash(i * 5 + 5) * 4.0D;
        }
        Integer[] order = new Integer[count];
        for (int i = 0; i < count; i++) order[i] = i;
        java.util.Arrays.sort(order, (p, q) -> Double.compare(puff[q][4], puff[p][4]));
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(PUFF_TEXTURE));
        for (int n = 0; n < count; n++) {
            double[] p = puff[order[n]];
            float t = (float) p[5];
            poseStack.pushPose();
            poseStack.translate(p[0], p[1], p[2]);
            poseStack.mulPose(camera);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * (int) p[6]));
            billboard(vc, poseStack.last().pose(), (float) p[3], (blast ? 0.3F : 0.5F) * alpha * (1.0F - 0.25F * t), 1.0F - 0.25F * t, 1.0F - 0.08F * t, 1.0F);
            poseStack.popPose();
        }
        VertexConsumer flare = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FLARE_TEXTURE));
        poseStack.pushPose();
        poseStack.mulPose(camera);
        billboard(flare, poseStack.last().pose(), radius * (blast ? 1.0F : 2.0F), 0.7F * alpha);
        poseStack.popPose();
    }

    /**
     * Look 3, the cube model: the same model as the Great Fireball, nested cubes that spin on all axes, with the texture
     * of the fireball in blue and white, drawn the same way, glowing and see-through.
     */
    private void cubeBall(MultiBufferSource bufferSource, PoseStack poseStack, float radius, float age, float alpha, boolean blast) {
        if (alpha < 0.01F) return;
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(CUBE_TEXTURE));
        // The middle cube of the model is a block wide
        float scale = radius * 2.0F;
        float rotation = age * 15.0F;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.7F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 0.3F));
        int color = (Math.max(0, Math.min(255, (int) ((blast ? 0.4F : 1.0F) * alpha * 255.0F))) << 24) | 0xFFFFFF;
        cubes.renderToBuffer(poseStack, vc, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
        poseStack.popPose();
    }

    private static void billboard(VertexConsumer vc, Matrix4f matrix, float size, float alpha) {
        billboard(vc, matrix, size, alpha, 1.0F, 1.0F, 1.0F);
    }

    /** A square facing the camera (the pose is already turned to it) with the whole texture on it. */
    private static void billboard(VertexConsumer vc, Matrix4f matrix, float size, float alpha, float r, float g, float b) {
        float h = size * 0.5F;
        float[][] corner = {{-h, -h, 0.0F, 1.0F}, {h, -h, 1.0F, 1.0F}, {h, h, 1.0F, 0.0F}, {-h, h, 0.0F, 0.0F}};
        for (float[] c : corner) {
            vc.addVertex(matrix, c[0], c[1], 0.0F)
                    .setColor(r, g, b, alpha)
                    .setUv(c[2], c[3])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(0.0F, 1.0F, 0.0F);
        }
    }

    /**
     * A smooth ball with a texture on it once round, turned on its axis by {@code spin} and leaned over by
     * {@code tilt}, and the texture moved round by {@code shift}.
     */
    private static void texturedSphere(VertexConsumer vc, Matrix4f matrix, float radius, float spin, float tilt, float shift, float alpha) {
        int segments = 28;
        int rings = 16;
        double cs = Math.cos(tilt);
        double sn = Math.sin(tilt);
        double cp = Math.cos(spin);
        double sp = Math.sin(spin);
        for (int j = 0; j < rings; j++) {
            for (int i = 0; i < segments; i++) {
                for (int corner = 0; corner < 4; corner++) {
                    int ci = i + (corner == 1 || corner == 2 ? 1 : 0);
                    int cj = j + (corner >= 2 ? 1 : 0);
                    double lat = Math.PI * (0.5D - cj / (double) rings);
                    double lon = Math.PI * 2.0D * ci / segments;
                    double x = Math.cos(lat) * Math.cos(lon);
                    double y = Math.sin(lat);
                    double z = Math.cos(lat) * Math.sin(lon);
                    // Lean it over, then turn it round the up axis
                    double y2 = y * cs - z * sn;
                    double z2 = y * sn + z * cs;
                    double x3 = x * cp - z2 * sp;
                    double z3 = x * sp + z2 * cp;
                    vc.addVertex(matrix, (float) (x3 * radius), (float) (y2 * radius), (float) (z3 * radius))
                            .setColor(1.0F, 1.0F, 1.0F, alpha)
                            .setUv(ci / (float) segments + shift, cj / (float) rings)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(FULL_BRIGHT)
                            .setNormal((float) x3, (float) y2, (float) z3);
                }
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
        int segments = 16;
        int rings = 10;
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
        texturedVertex(vc, matrix, p, u, v, alpha, WIND[0], WIND[1], WIND[2]);
    }

    private static void texturedVertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v, float alpha, float r, float g, float b) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, alpha)
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
