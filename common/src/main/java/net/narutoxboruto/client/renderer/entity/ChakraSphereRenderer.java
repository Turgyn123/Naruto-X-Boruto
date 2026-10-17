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
 *   <li>Rasengan: an orb of energy drawn like the lightning, see {@link #plasmaBall}, in three looks to compare: the
 *       plasma orb, the tangle (V2) and the gyroscope (V3).</li>
 *   <li>Rasenshuriken: held up above the head of the caster while it is charged. The same ball, with four big
 *       crescent blades like the blades of the fan turning round it in a pinwheel. Thrown, ribbons of wind spiral
 *       behind it like the wind of the fan. Where it hits it grows into a huge ball of ribbons with pinwheels turning
 *       in three planes.</li>
 * </ul>
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation WIND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_streak.png");
    private static final ResourceLocation CRESCENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/crescent.png");
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
        float ribbonAlpha = 0.94F * alpha;

        // The ball, the same for both jutsus
        ball(bufferSource, poseStack, matrix, aim, across, up, radius, age, alpha, blast, shuriken ? 1 : entity.getVariant());

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

    /** The ball in the look of {@code variant}: 0 the plasma orb, 1 the tangle, 2 the gyroscope. */
    private void ball(MultiBufferSource bufferSource, PoseStack poseStack, Matrix4f matrix, Vec3 aim, Vec3 across, Vec3 up,
                      float radius, float age, float alpha, boolean blast, int variant) {
        plasmaBall(bufferSource, matrix, aim, across, up, radius, age, alpha, blast, variant);
    }

    /**
     * The Rasengan as air. The same tangle as before, drawn as wind and not as lightning: a pale, soft glowing heart (light
     * that is added to what is behind it, whiter than blue) and arms of the white wind streak texture of the wind of the
     * Giant Fan, wide, soft and with streaks that run along them, that spiral over the ball from pole to pole and turn,
     * some one way and some the other. No hard bolts and no sparks.
     * <ul>
     *   <li>0: six wide arms.</li>
     *   <li>1: nine arms, the tangle.</li>
     *   <li>2: the tangle with two thin rings of wind round the ball that spin on different axes.</li>
     * </ul>
     */
    private void plasmaBall(MultiBufferSource bufferSource, Matrix4f matrix, Vec3 aim, Vec3 across, Vec3 up,
                            float radius, float age, float alpha, boolean blast, int style) {
        if (alpha < 0.01F) return;
        float breath = 1.0F + 0.04F * Mth.sin(age * 0.8F);
        float r = radius * (blast ? 1.0F : breath);
        // The heart goes from the inside to the outside: this render type keeps what it has drawn in front, so the middle
        // is drawn first and the bigger balls then glow over it
        VertexConsumer glow = bufferSource.getBuffer(RenderType.lightning());
        float[][] shells = blast
                ? new float[][]{{1.0F, 0.8F, 0.92F, 1.0F, 0.06F}}
                : new float[][]{{0.2F, 1.0F, 1.0F, 1.0F, 0.5F}, {0.45F, 0.95F, 0.98F, 1.0F, 0.2F}, {0.75F, 0.86F, 0.95F, 1.0F, 0.12F},
                        {1.0F, 0.8F, 0.92F, 1.0F, 0.1F}, {1.1F, 0.85F, 0.95F, 1.0F, 0.05F}};
        for (float[] shell : shells) {
            smoothBall(glow, matrix, r * shell[0], shell[1], shell[2], shell[3], shell[4] * alpha);
        }

        VertexConsumer wind = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WIND_TEXTURE));
        // The arms spiral from pole to pole and turn, some one way and some the other
        int arms = style == 0 ? 6 : 9;
        for (int arm = 0; arm < arms; arm++) {
            int points = 16;
            Vec3[] path = new Vec3[points + 1];
            double direction = arm % 2 == 1 ? -1.0D : 1.0D;
            for (int j = 0; j <= points; j++) {
                double t = j / (double) points;
                double polar = Math.PI * t;
                double azimuth = arm * (Math.PI * 2.0D / arms) + direction * (t * Math.PI * 2.4D) + age * 0.32D * direction;
                double bulge = r * 1.03D;
                path[j] = aim.scale(Math.cos(polar) * bulge)
                        .add(across.scale(Math.sin(polar) * Math.cos(azimuth) * bulge))
                        .add(up.scale(Math.sin(polar) * Math.sin(azimuth) * bulge));
            }
            windRibbon(wind, matrix, path, r * (style == 0 ? 0.12F : 0.085F), age, arm, 0.95F * alpha, true);
        }

        // Two thin rings of wind round the ball that spin on different axes
        if (style == 2 && !blast) {
            for (int ring = 0; ring < 2; ring++) {
                double tilt = Math.toRadians(30.0D + 50.0D * ring);
                double spin = age * (0.12D + 0.07D * ring) * (ring == 1 ? -1.0D : 1.0D) + ring * 2.0D;
                Vec3 e1 = new Vec3(Math.cos(spin), 0.0D, Math.sin(spin));
                Vec3 e2 = new Vec3(-Math.sin(spin) * Math.cos(tilt), Math.sin(tilt), Math.cos(spin) * Math.cos(tilt));
                int points = 28;
                Vec3[] path = new Vec3[points + 1];
                for (int j = 0; j <= points; j++) {
                    double angle = Math.PI * 2.0D * j / points;
                    path[j] = e1.scale(Math.cos(angle) * r * 1.3D).add(e2.scale(Math.sin(angle) * r * 1.3D));
                }
                windRibbon(wind, matrix, path, r * 0.07F, age, 30 + ring, 0.9F * alpha, false);
            }
        }
    }

    /**
     * A ribbon of the white wind streak texture along {@code path} (points round the middle of the ball), with the streaks
     * running along it and scrolling. The ribbon lies along the surface: across it is at a right angle to both the way it
     * runs and the way out from the middle. An open ribbon is thin at its ends and fades in and out there.
     */
    private static void windRibbon(VertexConsumer vc, Matrix4f matrix, Vec3[] path, float halfWidth, float age, int id, float alpha, boolean open) {
        int n = path.length - 1;
        Vec3[] side = new Vec3[n + 1];
        for (int j = 0; j <= n; j++) {
            Vec3 tangent = path[Math.min(n, j + 1)].subtract(path[Math.max(0, j - 1)]);
            Vec3 across = path[j].normalize().cross(tangent);
            double taper = open ? 0.35D + 0.65D * Math.sin(Math.PI * j / n) : 1.0D;
            side[j] = across.lengthSqr() < 1.0E-10D ? Vec3.ZERO : across.normalize().scale(halfWidth * taper);
        }
        float scroll = age * (0.1F + 0.04F * (id % 3)) * (id % 2 == 0 ? 1.0F : -1.0F);
        for (int j = 0; j < n; j++) {
            float t0 = j / (float) n;
            float t1 = (j + 1) / (float) n;
            float a0 = open ? alpha * Math.min(1.0F, Math.min(t0 * 5.0F, (1.0F - t0) * 5.0F)) : alpha;
            float a1 = open ? alpha * Math.min(1.0F, Math.min(t1 * 5.0F, (1.0F - t1) * 5.0F)) : alpha;
            float u0 = t0 * 2.0F - scroll + id * 0.33F;
            float u1 = t1 * 2.0F - scroll + id * 0.33F;
            windVertex(vc, matrix, path[j].add(side[j]), u0, 0.0F, a0);
            windVertex(vc, matrix, path[j].subtract(side[j]), u0, 1.0F, a0);
            windVertex(vc, matrix, path[j + 1].subtract(side[j + 1]), u1, 1.0F, a1);
            windVertex(vc, matrix, path[j + 1].add(side[j + 1]), u1, 0.0F, a1);
        }
    }

    private static void windVertex(VertexConsumer vc, Matrix4f matrix, Vec3 p, float u, float v, float alpha) {
        vc.addVertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                .setColor(0.93F, 0.97F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    /** A smooth ball of one colour of glowing light, seen from outside and inside. */
    private static void smoothBall(VertexConsumer vc, Matrix4f matrix, float radius, float r, float g, float b, float alpha) {
        int segments = 20;
        int rings = 12;
        for (int j = 0; j < rings; j++) {
            for (int i = 0; i < segments; i++) {
                Vec3[] corner = new Vec3[4];
                for (int c = 0; c < 4; c++) {
                    int ci = i + (c == 1 || c == 2 ? 1 : 0);
                    int cj = j + (c >= 2 ? 1 : 0);
                    double lat = Math.PI * (cj / (double) rings - 0.5D);
                    double lon = Math.PI * 2.0D * ci / segments;
                    corner[c] = new Vec3(Math.cos(lat) * Math.cos(lon) * radius, Math.sin(lat) * radius, Math.cos(lat) * Math.sin(lon) * radius);
                }
                glowQuad(vc, matrix, corner[0], corner[1], corner[2], corner[3], r, g, b, alpha);
            }
        }
    }

    /** One quad of glowing colour, drawn from both sides (this render type does not draw the back of a face). */
    private static void glowQuad(VertexConsumer vc, Matrix4f matrix, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float r, float g, float b, float alpha) {
        Vec3[] corner = {p0, p1, p2, p3};
        for (int c = 0; c < 4; c++) vc.addVertex(matrix, (float) corner[c].x, (float) corner[c].y, (float) corner[c].z).setColor(r, g, b, alpha);
        for (int c = 3; c >= 0; c--) vc.addVertex(matrix, (float) corner[c].x, (float) corner[c].y, (float) corner[c].z).setColor(r, g, b, alpha);
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
