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
 *   <li>Nuibari: a needle that flies to the target and lands at its feet, with thin threads and a red flash.</li>
 * </ul>
 */
public class WeaponEffectRenderer extends EntityRenderer<WeaponEffectEntity> {

    private static final ResourceLocation WIND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_streak.png");
    private static final ResourceLocation CRESCENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/crescent.png");
    private static final ResourceLocation NEEDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/needle.png");
    private static final float NEEDLE_FLIGHT_TICKS = 5.0F;
    private static final double NEEDLE_LENGTH = 2.0D;
    private static final float RED_FLASH_TICKS = 14.0F;
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

    /**
     * Nuibari, as in the video: a long needle flies from the hand to the target and sticks in the ground in front of it,
     * the target flashes red, a thin thread runs from the eye of the needle to the target, another from the hand
     * to the target, and a few loose bands of thread are stitched round its body.
     */
    private void renderThread(WeaponEffectEntity entity, float age, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        Entity target = entity.level().getEntity(entity.getTargetId());
        if (target == null) return;

        // Everything is worked out in the world and then moved to the origin of the entity (the hand)
        Vec3 hand = entity.getPosition(partialTick);
        Vec3 feet = target.getPosition(partialTick);
        Vec3 chest = feet.add(0.0D, target.getBbHeight() * 0.55D, 0.0D);

        Vec3 flat = new Vec3(feet.x - hand.x, 0.0D, feet.z - hand.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
        // The needle ends up stuck in the ground in front of the target, leaning over it, and stays there
        Vec3 landTip = feet.subtract(flat.scale(0.5D)).add(0.0D, -0.1D, 0.0D);
        Vec3 rest = new Vec3(flat.x * 0.5D, -0.87D, flat.z * 0.5D).normalize();

        // The flight of the needle: straight at the target, and flat on the ground when it has landed
        float flight = Mth.clamp(age / NEEDLE_FLIGHT_TICKS, 0.0F, 1.0F);
        Vec3 toTarget = landTip.subtract(hand);
        Vec3 flightDirection = toTarget.lengthSqr() < 1.0E-4D ? flat : toTarget.normalize();
        float settle = flight * flight * flight * flight;
        Vec3 direction = flightDirection.scale(1.0F - settle).add(rest.scale(settle));
        direction = direction.lengthSqr() < 1.0E-4D ? flat : direction.normalize();
        Vec3 tip = hand.add(landTip.subtract(hand).scale(flight));
        Vec3 tail = tip.subtract(direction.scale(NEEDLE_LENGTH));

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer needle = bufferSource.getBuffer(RenderType.entityTranslucent(NEEDLE_TEXTURE));
        strip(needle, matrix, tail.subtract(hand), tip.subtract(hand), 0.035D, (float) NEEDLE_LENGTH, false);

        if (flight >= 1.0F) {
            VertexConsumer thread = bufferSource.getBuffer(RenderType.entityTranslucent(THREAD_TEXTURE));
            float grow = Mth.clamp((age - NEEDLE_FLIGHT_TICKS) / 6.0F, 0.0F, 1.0F);
            // A thin thread from the hand to the target, so it can be seen what holds it
            strip(thread, matrix, Vec3.ZERO, chest.subtract(hand).scale(grow), 0.006D, 1.0F, true);
            // From the eye of the needle, which sticks out of the ground, to the target
            strip(thread, matrix, tail.subtract(hand), chest.subtract(hand), 0.006D, 1.0F, true);
            // A few loose stitches round the body. They pull tight and then stay where they are.
            stitches(thread, matrix, target, feet.subtract(hand), grow);
        }

        // The target flashes red when the needle lands
        float flash = age - NEEDLE_FLIGHT_TICKS;
        if (flash >= 0.0F && flash < RED_FLASH_TICKS) {
            net.minecraft.world.phys.AABB box = target.getBoundingBox()
                    .move(feet.subtract(target.position()))
                    .move(-hand.x, -hand.y, -hand.z)
                    .inflate(0.04D);
            float alpha = 0.6F * (1.0F - flash / RED_FLASH_TICKS);
            VertexConsumer shell = bufferSource.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));
            shellBox(shell, matrix, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, alpha);
        }
    }

    /** Bands of thread across the body: two over the chest and shoulders, two round the legs. They do not move. */
    private static void stitches(VertexConsumer vc, Matrix4f matrix, Entity target, Vec3 feetFromHand, float grow) {
        double height = target.getBbHeight();
        double radius = target.getBbWidth() * 0.58D + 0.06D;
        // Height as a part of the body, how much the band slants, and which way round it starts
        double[][] bands = {{0.80D, 0.10D, 0.0D}, {0.60D, -0.09D, 1.5D}, {0.36D, 0.05D, 0.7D}, {0.14D, -0.05D, 2.4D}};
        int segments = 18;
        // The bands start wide and pull tight over the first moments
        double r = radius * (1.0D + (1.0D - grow));
        for (double[] band : bands) {
            Vec3 previous = null;
            for (int i = 0; i <= segments; i++) {
                double angle = (double) i / segments * Math.PI * 2.0D;
                double y = (band[0] + band[1] * Math.sin(angle + band[2])) * height;
                Vec3 point = feetFromHand.add(Math.cos(angle) * r, y, Math.sin(angle) * r);
                if (previous != null) strip(vc, matrix, previous, point, 0.006D, 1.0F, true);
                previous = point;
            }
        }
    }

    /** A thin strip from one point to another, as two crossed ribbons so it is seen from any side. */
    private static void strip(VertexConsumer vc, Matrix4f matrix, Vec3 from, Vec3 to, double halfWidth, float length, boolean tile) {
        Vec3 along = to.subtract(from);
        double distance = along.length();
        if (distance < 1.0E-3D) return;
        Vec3 direction = along.scale(1.0D / distance);
        Vec3 reference = Math.abs(direction.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 sideA = direction.cross(reference).normalize().scale(halfWidth);
        Vec3 sideB = direction.cross(sideA).normalize().scale(halfWidth);
        float u1 = tile ? (float) (distance * 2.0D) : 1.0F;
        for (int side = 0; side < 2; side++) {
            Vec3 across = side == 0 ? sideA : sideB;
            vertex(vc, matrix, (float) (from.x + across.x), (float) (from.y + across.y), (float) (from.z + across.z), 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            vertex(vc, matrix, (float) (from.x - across.x), (float) (from.y - across.y), (float) (from.z - across.z), 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            vertex(vc, matrix, (float) (to.x - across.x), (float) (to.y - across.y), (float) (to.z - across.z), u1, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            vertex(vc, matrix, (float) (to.x + across.x), (float) (to.y + across.y), (float) (to.z + across.z), u1, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        }
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
