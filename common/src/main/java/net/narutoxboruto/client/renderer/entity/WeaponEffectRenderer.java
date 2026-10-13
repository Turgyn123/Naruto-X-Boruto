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
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
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
        float length = 3.4F * size;
        float radius = 0.55F * size;
        int color = entity.getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float baseAlpha = ((color >> 24) & 0xFF) / 255.0F * fade;

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(WIND_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int segments = 12;

        int ribbons = 5;
        for (int ribbon = 0; ribbon < ribbons; ribbon++) {
            float phase = ribbon * (float) (Math.PI * 2.0 / ribbons) + age * 0.9F;
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
        float width = 0.38F * size * (0.5F + t);
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

        // The thread: thin, white and straight, seen from any side with two crossed strips
        Vec3 reference = Math.abs(direction.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 sideA = direction.cross(reference).normalize().scale(0.012D);
        Vec3 sideB = direction.cross(sideA).normalize().scale(0.012D);

        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(THREAD_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float u1 = (float) (distance * 2.0D);
        for (int side = 0; side < 2; side++) {
            Vec3 across = side == 0 ? sideA : sideB;
            vertex(vc, matrix, (float) across.x, (float) across.y, (float) across.z, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            vertex(vc, matrix, (float) -across.x, (float) -across.y, (float) -across.z, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            vertex(vc, matrix, (float) (along.x - across.x), (float) (along.y - across.y), (float) (along.z - across.z), u1, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            vertex(vc, matrix, (float) (along.x + across.x), (float) (along.y + across.y), (float) (along.z + across.z), u1, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        // The target is tinted red while the thread holds it: a thin red shell around its body
        net.minecraft.world.phys.AABB box = target.getBoundingBox()
                .move(target.getPosition(partialTick).subtract(target.position()))
                .move(-entity.getX(), -entity.getY(), -entity.getZ())
                .inflate(0.04D);
        float alpha = 0.32F + 0.08F * Mth.sin(age * 0.4F);
        VertexConsumer shell = bufferSource.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
        shellBox(shell, matrix, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, alpha);
    }

    /** The six faces of a box in red. */
    private static void shellBox(VertexConsumer vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float alpha) {
        float r = 0.9F;
        float g = 0.05F;
        float b = 0.05F;
        // bottom, top
        vertex(vc, m, x0, y0, z0, 0, 0, r, g, b, alpha); vertex(vc, m, x1, y0, z0, 1, 0, r, g, b, alpha);
        vertex(vc, m, x1, y0, z1, 1, 1, r, g, b, alpha); vertex(vc, m, x0, y0, z1, 0, 1, r, g, b, alpha);
        vertex(vc, m, x0, y1, z0, 0, 0, r, g, b, alpha); vertex(vc, m, x0, y1, z1, 0, 1, r, g, b, alpha);
        vertex(vc, m, x1, y1, z1, 1, 1, r, g, b, alpha); vertex(vc, m, x1, y1, z0, 1, 0, r, g, b, alpha);
        // north, south
        vertex(vc, m, x0, y0, z0, 0, 0, r, g, b, alpha); vertex(vc, m, x0, y1, z0, 0, 1, r, g, b, alpha);
        vertex(vc, m, x1, y1, z0, 1, 1, r, g, b, alpha); vertex(vc, m, x1, y0, z0, 1, 0, r, g, b, alpha);
        vertex(vc, m, x0, y0, z1, 0, 0, r, g, b, alpha); vertex(vc, m, x1, y0, z1, 1, 0, r, g, b, alpha);
        vertex(vc, m, x1, y1, z1, 1, 1, r, g, b, alpha); vertex(vc, m, x0, y1, z1, 0, 1, r, g, b, alpha);
        // west, east
        vertex(vc, m, x0, y0, z0, 0, 0, r, g, b, alpha); vertex(vc, m, x0, y0, z1, 1, 0, r, g, b, alpha);
        vertex(vc, m, x0, y1, z1, 1, 1, r, g, b, alpha); vertex(vc, m, x0, y1, z0, 0, 1, r, g, b, alpha);
        vertex(vc, m, x1, y0, z0, 0, 0, r, g, b, alpha); vertex(vc, m, x1, y1, z0, 0, 1, r, g, b, alpha);
        vertex(vc, m, x1, y1, z1, 1, 1, r, g, b, alpha); vertex(vc, m, x1, y0, z1, 1, 0, r, g, b, alpha);
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
