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
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.main.Main;
import org.joml.Matrix4f;

/**
 * Draws the weapon effects as real shapes with textures, so that they look like wind, energy and thread and not
 * like particles:
 * <ul>
 *   <li>wind: three ribbons of air that spiral around the way it flies, widening and scrolling forward;</li>
 *   <li>crescent: a flat crescent blade that grows a little as it flies and fades out;</li>
 *   <li>thread: a thin, slightly swaying thread from the hand of the wielder to the target.</li>
 * </ul>
 */
public class WeaponEffectRenderer extends EntityRenderer<WeaponEffectEntity> {

    private static final ResourceLocation WIND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_streak.png");
    private static final ResourceLocation CRESCENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/crescent.png");
    private static final ResourceLocation THREAD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/thread.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public WeaponEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WeaponEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        float age = entity.tickCount + partialTick;
        int kind = entity.getKind();

        poseStack.pushPose();
        if (kind == WeaponEffectEntity.THREAD) {
            renderThread(entity, age, partialTick, poseStack, bufferSource);
        } else {
            // Turn so that the local z axis points the way the effect flies
            poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
            float fade = Mth.clamp((entity.getMaxLife() - age) / 4.0F, 0.0F, 1.0F) * Mth.clamp(age / 2.0F, 0.0F, 1.0F);
            if (kind == WeaponEffectEntity.WIND) renderWind(entity, age, fade, poseStack, bufferSource);
            else renderCrescent(entity, age, fade, poseStack, bufferSource);
        }
        poseStack.popPose();
    }

    // ---------------------------------------------------------------- wind

    private void renderWind(WeaponEffectEntity entity, float age, float fade, PoseStack poseStack, MultiBufferSource bufferSource) {
        float size = entity.getSize();
        float length = 2.8F * size;
        float radius = 0.4F * size;
        int color = entity.getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float baseAlpha = ((color >> 24) & 0xFF) / 255.0F * fade;

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(WIND_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int segments = 12;

        for (int ribbon = 0; ribbon < 3; ribbon++) {
            float phase = ribbon * (float) (Math.PI * 2.0 / 3.0) + age * 0.9F;
            for (int s = 0; s < segments; s++) {
                float t0 = s / (float) segments;
                float t1 = (s + 1) / (float) segments;
                float[] a = ribbonPoint(t0, length, radius, phase, size);
                float[] b = ribbonPoint(t1, length, radius, phase, size);
                float alpha0 = baseAlpha * Math.min(1.0F, Math.min(t0 * 5.0F, (1.0F - t0) * 5.0F));
                float alpha1 = baseAlpha * Math.min(1.0F, Math.min(t1 * 5.0F, (1.0F - t1) * 5.0F));
                float u0 = t0 * 2.0F - age * 0.12F + ribbon * 0.33F;
                float u1 = t1 * 2.0F - age * 0.12F + ribbon * 0.33F;
                // a: inner x, y, z, outer x, y, z
                vertex(vc, matrix, a[0], a[1], a[2], u0, 0.0F, red, green, blue, alpha0);
                vertex(vc, matrix, a[3], a[4], a[5], u0, 1.0F, red, green, blue, alpha0);
                vertex(vc, matrix, b[3], b[4], b[5], u1, 1.0F, red, green, blue, alpha1);
                vertex(vc, matrix, b[0], b[1], b[2], u1, 0.0F, red, green, blue, alpha1);
            }
        }
    }

    /** A point on a ribbon: its inner and its outer edge, for a position {@code t} (0 to 1) along the swirl. */
    private static float[] ribbonPoint(float t, float length, float radius, float phase, float size) {
        float z = (t - 0.5F) * length;
        float r = radius * (0.3F + 0.7F * t);
        float angle = phase + z * 2.2F;
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);
        float width = 0.2F * size * (0.5F + t);
        float inner = Math.max(0.0F, r - width * 0.5F);
        float outer = r + width * 0.5F;
        return new float[]{sin * inner, cos * inner, z, sin * outer, cos * outer, z};
    }

    // ---------------------------------------------------------------- crescent

    private void renderCrescent(WeaponEffectEntity entity, float age, float fade, PoseStack poseStack, MultiBufferSource bufferSource) {
        float grow = 1.0F + 0.04F * age;
        float halfWidth = 1.3F * entity.getSize() * grow;
        float depth = 1.0F * entity.getSize() * grow;
        int color = entity.getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = ((color >> 24) & 0xFF) / 255.0F * fade;

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(CRESCENT_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        // Three layers a little above each other, so the blade has some thickness
        for (int layer = -1; layer <= 1; layer++) {
            float y = layer * 0.12F * entity.getSize();
            float layerAlpha = layer == 0 ? alpha : alpha * 0.55F;
            float scale = layer == 0 ? 1.0F : 0.85F;
            float hw = halfWidth * scale;
            float d = depth * scale;
            vertex(vc, matrix, -hw, y, d * 0.5F, 0.0F, 0.0F, red, green, blue, layerAlpha);
            vertex(vc, matrix, hw, y, d * 0.5F, 1.0F, 0.0F, red, green, blue, layerAlpha);
            vertex(vc, matrix, hw, y, -d * 0.5F, 1.0F, 1.0F, red, green, blue, layerAlpha);
            vertex(vc, matrix, -hw, y, -d * 0.5F, 0.0F, 1.0F, red, green, blue, layerAlpha);
        }
    }

    // ---------------------------------------------------------------- thread

    private void renderThread(WeaponEffectEntity entity, float age, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        Entity target = entity.level().getEntity(entity.getTargetId());
        if (target == null) return;

        Vec3 from = entity.getPosition(partialTick);
        Vec3 to = target.getPosition(partialTick).add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vec3 along = to.subtract(from);
        double distance = along.length();
        if (distance < 0.05D) return;
        Vec3 direction = along.scale(1.0D / distance);

        // Two directions across the thread, so that it can be seen from any side
        Vec3 reference = Math.abs(direction.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 sideA = direction.cross(reference).normalize().scale(0.03D);
        Vec3 sideB = direction.cross(sideA).normalize().scale(0.03D);

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(THREAD_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int segments = Math.max(6, (int) (distance * 3.0D));

        for (int s = 0; s < segments; s++) {
            float t0 = s / (float) segments;
            float t1 = (s + 1) / (float) segments;
            Vec3 p0 = threadPoint(along, direction, sideA, sideB, t0, age, distance);
            Vec3 p1 = threadPoint(along, direction, sideA, sideB, t1, age, distance);
            float u0 = (float) (t0 * distance * 2.0D);
            float u1 = (float) (t1 * distance * 2.0D);
            for (int side = 0; side < 2; side++) {
                Vec3 across = side == 0 ? sideA : sideB;
                vertex(vc, matrix, (float) (p0.x + across.x), (float) (p0.y + across.y), (float) (p0.z + across.z), u0, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F);
                vertex(vc, matrix, (float) (p0.x - across.x), (float) (p0.y - across.y), (float) (p0.z - across.z), u0, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
                vertex(vc, matrix, (float) (p1.x - across.x), (float) (p1.y - across.y), (float) (p1.z - across.z), u1, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
                vertex(vc, matrix, (float) (p1.x + across.x), (float) (p1.y + across.y), (float) (p1.z + across.z), u1, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    /** A point on the thread: on the line from the hand to the target, sagging a little and swaying. */
    private static Vec3 threadPoint(Vec3 along, Vec3 direction, Vec3 sideA, Vec3 sideB, float t, float age, double distance) {
        double envelope = Math.sin(Math.PI * t);
        double sway = Math.sin(t * 9.0D + age * 0.35D) * 0.05D * envelope;
        double sag = -0.12D * envelope * Math.min(1.0D, distance / 6.0D);
        Vec3 swayDirection = sideA.normalize().scale(sway * 3.0D);
        return along.scale(t).add(0.0D, sag, 0.0D).add(swayDirection);
    }

    // ---------------------------------------------------------------- helpers

    private static void vertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z, float u, float v,
                               float red, float green, float blue, float alpha) {
        vc.addVertex(matrix, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(WeaponEffectEntity entity) {
        return WIND_TEXTURE;
    }
}
