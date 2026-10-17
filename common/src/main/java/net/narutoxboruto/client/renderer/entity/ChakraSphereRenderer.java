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
 * Draws the {@link ChakraSphereEntity} in the pixel art style of Minecraft, with chunky pixel textures of the mod, the
 * same way as the Great Fire Annihilation: no models, no particles, no smooth glow.
 * <ul>
 *   <li>Rasengan: a ball made of many pixel swirls (that turn and change from frame to frame) packed into a sphere,
 *       with a bright white swirl in the middle.</li>
 *   <li>Rasenshuriken: held up above the head of the caster while it is charged, as a flat pixel disc of swirling wind
 *       that turns in steps. Thrown, it is a bigger ball of swirls with a pinwheel of four pixel blades turning in
 *       front of it, and where it hits it grows into a huge ball with pinwheels turning in three planes.</li>
 * </ul>
 * While it is charged it is drawn at the hand of the caster, so it does not lag behind them.
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation BLOB_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_blob.png");
    private static final ResourceLocation DISC_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_disc.png");
    private static final ResourceLocation BLADES_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/rasen_blades.png");
    private static final int FULL_BRIGHT = 0xF000F0;

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

        if (!shuriken) {
            float radius;
            float alpha = 1.0F;
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
            ball(bufferSource, poseStack, radius, age, alpha, 34);
        } else if (phase == ChakraSphereEntity.CHARGING) {
            // A flat disc of swirling wind held up above the head, that grows as it is charged
            float size = 0.4F + 2.8F * charge;
            if (charge >= 1.0F) size *= 1.0F + 0.05F * Mth.sin(age * 1.3F);
            disc(bufferSource, poseStack, size, age, 1.0F);
            ball(bufferSource, poseStack, 0.08F + 0.2F * charge, age, 1.0F, 20);
        } else {
            boolean blast = phase == ChakraSphereEntity.IMPACT;
            float radius = 1.3F;
            float alpha = 1.0F;
            if (blast) {
                float grown = Mth.clamp(phaseAge / ChakraSphereEntity.SHURIKEN_GROW_TICKS, 0.0F, 1.0F);
                grown = grown * grown * (3.0F - 2.0F * grown);
                radius = Mth.lerp(grown, 1.3F, (float) ChakraSphereEntity.SHURIKEN_RADIUS);
                alpha = Mth.clamp((ChakraSphereEntity.SHURIKEN_BLAST_TICKS - phaseAge) / 6.0F, 0.0F, 1.0F);
            }
            if (alpha >= 0.01F) {
                ball(bufferSource, poseStack, radius, age, alpha, blast ? 46 : 40);
                // The pinwheel of four blades, across the way it is aimed. The huge ball has them in three planes.
                double yawRad = Math.toRadians(yaw);
                double pitchRad = Math.toRadians(pitch);
                Vec3 aim = new Vec3(-Math.sin(yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(yawRad) * Math.cos(pitchRad));
                Vec3 across = aim.cross(new Vec3(0.0D, 1.0D, 0.0D));
                across = across.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : across.normalize();
                Vec3 up = aim.cross(across).normalize();
                float reach = blast ? radius : radius * 2.2F;
                pinwheel(bufferSource, poseStack, across, up, reach, age, alpha);
                if (blast) {
                    pinwheel(bufferSource, poseStack, up, aim, reach, age + 5.0F, alpha);
                    pinwheel(bufferSource, poseStack, aim, across, reach, age + 9.0F, alpha);
                }
            }
        }
        poseStack.popPose();
    }

    /**
     * A ball made of pixel swirls packed into a sphere and turning round it, the ones in the middle faster. Every
     * swirl is a picture of the sheet, changes from picture to picture as it turns, and they are drawn from the far
     * side to the near side. A bright white swirl sits in the middle.
     */
    private void ball(MultiBufferSource bufferSource, PoseStack poseStack, float radius, float age, float alpha, int count) {
        if (alpha < 0.01F) return;
        Quaternionf camera = this.entityRenderDispatcher.cameraOrientation();
        Vector3f forward = camera.transform(new Vector3f(0.0F, 0.0F, -1.0F));
        double[][] swirl = new double[count][6]; // x, y, z, size, depth, number
        for (int i = 0; i < count; i++) {
            double theta = hash(i * 5 + 1) * Math.PI * 2.0D;
            double z = hash(i * 5 + 2) * 2.0D - 1.0D;
            double ring = Math.sqrt(1.0D - z * z);
            double t = Math.cbrt(0.2D + 0.8D * hash(i * 5 + 3));
            double spin = age * (0.1D + 0.3D * (1.0D - t)) * (i % 2 == 0 ? 1.0D : -0.7D);
            double x0 = Math.cos(theta) * ring;
            double z0 = Math.sin(theta) * ring;
            swirl[i][0] = (x0 * Math.cos(spin) - z0 * Math.sin(spin)) * radius * t * 0.8D;
            swirl[i][1] = z * radius * t * 0.8D;
            swirl[i][2] = (x0 * Math.sin(spin) + z0 * Math.cos(spin)) * radius * t * 0.8D;
            swirl[i][3] = radius * (0.75D + 0.45D * hash(i * 5 + 4));
            swirl[i][4] = swirl[i][0] * forward.x + swirl[i][1] * forward.y + swirl[i][2] * forward.z;
            swirl[i][5] = i;
        }
        Integer[] order = new Integer[count];
        for (int i = 0; i < count; i++) order[i] = i;
        // The far ones first
        java.util.Arrays.sort(order, (p, q) -> Double.compare(swirl[q][4], swirl[p][4]));
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(BLOB_TEXTURE));
        for (int n = 0; n < count; n++) {
            double[] s = swirl[order[n]];
            int i = (int) s[5];
            // Only quarter turns, so the pixels stay square, and the picture changes about 6 times a second
            int frame = (int) (hash(i * 5 + 5) * 4.0F + age * 0.3F) & 3;
            poseStack.pushPose();
            poseStack.translate(s[0], s[1], s[2]);
            poseStack.mulPose(camera);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * ((int) (hash(i * 5 + 6) * 4.0F))));
            frameQuad(vc, poseStack.last().pose(), (float) s[3], 0.95F * alpha, frame);
            poseStack.popPose();
        }
        // The bright swirl in the middle
        poseStack.pushPose();
        poseStack.mulPose(camera);
        frameQuad(vc, poseStack.last().pose(), radius * 0.8F, alpha, ((int) (age * 0.3F)) & 3);
        poseStack.popPose();
    }

    /**
     * A flat disc lying flat in the air, with the pixel swirl on it, from both sides. It turns in steps, a second one
     * under it turns the other way, so the swirl is never the same twice.
     */
    private void disc(MultiBufferSource bufferSource, PoseStack poseStack, float size, float age, float alpha) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(DISC_TEXTURE));
        for (int k = 0; k < 2; k++) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.03D * k, 0.0D);
            int step = (int) (age * (k == 0 ? 0.8F : -0.6F));
            poseStack.mulPose(Axis.YP.rotationDegrees(step * 22.5F + k * 45.0F));
            float h = size * (k == 0 ? 0.5F : 0.42F);
            Matrix4f m = poseStack.last().pose();
            float a = alpha * (k == 0 ? 0.95F : 0.8F);
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

    /**
     * The pinwheel of four blades lying in the plane of {@code sideA} and {@code sideB}, from both sides, turning in
     * steps of 15 degrees. {@code reach} is how far the blades reach from the middle.
     */
    private void pinwheel(MultiBufferSource bufferSource, PoseStack poseStack, Vec3 sideA, Vec3 sideB, float reach, float age, float alpha) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(BLADES_TEXTURE));
        double turn = Math.toRadians(((int) (age * 0.8F)) * 15.0D);
        Vec3 a = sideA.scale(Math.cos(turn) * reach).add(sideB.scale(Math.sin(turn) * reach));
        Vec3 b = sideA.scale(-Math.sin(turn) * reach).add(sideB.scale(Math.cos(turn) * reach));
        Matrix4f m = poseStack.last().pose();
        float al = alpha * 0.95F;
        Vec3[] corner = {a.scale(-1.0D).subtract(b), a.subtract(b), a.add(b), b.subtract(a)};
        float[][] uv = {{0.0F, 0.0F}, {1.0F, 0.0F}, {1.0F, 1.0F}, {0.0F, 1.0F}};
        for (int i = 0; i < 4; i++) texturedVertex(vc, m, (float) corner[i].x, (float) corner[i].y, (float) corner[i].z, uv[i][0], uv[i][1], al);
        for (int i = 3; i >= 0; i--) texturedVertex(vc, m, (float) corner[i].x, (float) corner[i].y, (float) corner[i].z, uv[i][0], uv[i][1], al);
    }

    private static void texturedVertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z, float u, float v, float alpha) {
        vc.addVertex(matrix, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    /** A square facing the camera (the pose is already turned to it) with one picture of the 2 x 2 sheet on it. */
    private static void frameQuad(VertexConsumer vc, Matrix4f matrix, float size, float alpha, int frame) {
        float h = size * 0.5F;
        float u0 = (frame & 1) * 0.5F;
        float v0 = (frame >> 1) * 0.5F;
        texturedVertex(vc, matrix, -h, -h, 0.0F, u0, v0 + 0.5F, alpha);
        texturedVertex(vc, matrix, h, -h, 0.0F, u0 + 0.5F, v0 + 0.5F, alpha);
        texturedVertex(vc, matrix, h, h, 0.0F, u0 + 0.5F, v0, alpha);
        texturedVertex(vc, matrix, -h, h, 0.0F, u0, v0, alpha);
    }

    /** A pseudo random number from 0 to 1 for a whole number, always the same for the same number. */
    private static float hash(int n) {
        int h = n * 374761393 + 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(ChakraSphereEntity entity) {
        return BLOB_TEXTURE;
    }
}
