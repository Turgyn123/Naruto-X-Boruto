package net.narutoxboruto.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.items.swords.Hiramekarei;
import net.narutoxboruto.main.Main;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The chakra coat of the Hiramekarei, drawn the way the anime does it: not a tint on the blade but a big flame-like
 * aura of chakra around it, soft pale-blue mist with white highlights, flowing along the blade and licking off past
 * its tip.
 *
 * The aura is built around the blade: the model is measured once (how wide it is along its length) and three
 * sleeves of flames are wrapped around that outline, one close to the blade and two wider. Each sleeve scrolls a flame
 * texture along the blade and wavers, so the flames stream towards the tip. It is drawn fully bright and see-through.
 * The loaders' item renderer mixins just call {@link #render} after the item itself has been drawn.
 */
public final class HiramekareiChakraRenderer {

    private HiramekareiChakraRenderer() {}

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/effect/hiramekarei_chakra.png");

    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/effect/hiramekarei_glow.png");
    private static final ResourceLocation WISP = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/effect/hiramekarei_wisp.png");

    /** The wisps that lift off the edge of the blade: where, which way, how fast and how far. */
    private static final int WISPS = 14;
    private static final int WISP_SEGMENTS = 8;
    private static final float[] WISP_HEIGHT = new float[WISPS];
    private static final float[] WISP_ANGLE = new float[WISPS];
    private static final float[] WISP_PHASE = new float[WISPS];
    private static final float[] WISP_SPEED = new float[WISPS];
    private static final float[] WISP_REACH = new float[WISPS];

    static {
        RandomSource random = RandomSource.create(7L);
        for (int i = 0; i < WISPS; i++) {
            WISP_HEIGHT[i] = 0.08F + 0.87F * random.nextFloat();
            WISP_ANGLE[i] = random.nextFloat() * Mth.TWO_PI;
            WISP_PHASE[i] = random.nextFloat();
            WISP_SPEED[i] = 0.55F + 0.5F * random.nextFloat();
            WISP_REACH[i] = 0.8F + 0.6F * random.nextFloat();
        }
    }

    private static final RandomSource RANDOM = RandomSource.create(42L);

    /** How wide the blade is along its length: the middle and the half sizes of its outline in slices. */
    private static final int SLICES = 40;
    /** The ring of the aura: rows along the blade and columns around it. */
    private static final int ROWS = 36;
    private static final int COLUMNS = 24;
    /** The flames lick on past the tip of the blade, this much of its length. */
    private static final float TIP_EXTENSION = 0.06F;

    private record Outline(float yMin, float yMax, float[] cx, float[] cz, float[] hx, float[] hz) {}

    private static final Map<BakedModel, Outline> OUTLINES = new WeakHashMap<>();

    /** Call after the item itself has been drawn. Does nothing unless this is a Hiramekarei with its coat on. */
    public static void render(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack,
                              MultiBufferSource buffer, BakedModel model) {
        if (!(stack.getItem() instanceof Hiramekarei blade) || !blade.isCoated(stack)) return;
        // Not in the inventory, where the aura would spill over the icon
        if (context == ItemDisplayContext.GUI) return;

        Outline outline = OUTLINES.computeIfAbsent(model, HiramekareiChakraRenderer::measure);
        if (outline == null) return;

        float time = (Util.getMillis() % 1_000_000L) / 1000.0F;

        poseStack.pushPose();
        // Back to where the item model itself was drawn: its display transform, then the model's own origin
        model.getTransforms().getTransform(context).apply(leftHand, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // The aura does not write depth, so the blade must be drawn first or it paints over the aura
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        Matrix4f matrix = poseStack.last().pose();

        // From the outside in. The outer layers are thin mist, so the sword can be seen through them. The two next
        // to the blade are a near white glow: bright at the blade and fading blue outwards. Each layer has its own
        // size, speed and turn, so the mist crosses itself.
        // A buffer source ends the buffer before as soon as the next render type is asked for, so each one is fetched
        // just before it is drawn to and never held while another is asked for.
        VertexConsumer mist = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        sleeve(mist, matrix, outline, time, 1.22F, 0.050F, 0.10F, 0.45F, 1, 0.30F);
        sleeve(mist, matrix, outline, time, 1.14F, 0.035F, 0.14F, 0.60F, 2, 1.70F);
        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(GLOW));
        sleeve(glow, matrix, outline, time, 1.07F, 0.020F, 0.22F, 0.75F, 1, 3.10F);
        sleeve(glow, matrix, outline, time, 1.02F, 0.008F, 0.32F, 0.90F, 2, 4.20F);
        // Thin wisps of chakra lift off the edge and drift away along the blade
        VertexConsumer wispBuffer = buffer.getBuffer(RenderType.entityTranslucentEmissive(WISP));
        wisps(wispBuffer, matrix, outline, time);

        poseStack.popPose();
    }

    /** Measures the blade: its centre and half widths in slices from one end to the other. */
    private static Outline measure(BakedModel model) {
        float yMin = Float.MAX_VALUE;
        float yMax = -Float.MAX_VALUE;
        for (int i = 0; i <= Direction.values().length; i++) {
            Direction side = i < Direction.values().length ? Direction.values()[i] : null;
            for (BakedQuad quad : model.getQuads(null, side, RANDOM)) {
                int[] data = quad.getVertices();
                for (int v = 0; v < 4; v++) {
                    float y = Float.intBitsToFloat(data[v * 8 + 1]);
                    yMin = Math.min(yMin, y);
                    yMax = Math.max(yMax, y);
                }
            }
        }
        if (yMax - yMin < 1.0e-4F) return null;

        float[] minX = new float[SLICES];
        float[] maxX = new float[SLICES];
        float[] minZ = new float[SLICES];
        float[] maxZ = new float[SLICES];
        java.util.Arrays.fill(minX, Float.MAX_VALUE);
        java.util.Arrays.fill(minZ, Float.MAX_VALUE);
        java.util.Arrays.fill(maxX, -Float.MAX_VALUE);
        java.util.Arrays.fill(maxZ, -Float.MAX_VALUE);

        for (int i = 0; i <= Direction.values().length; i++) {
            Direction side = i < Direction.values().length ? Direction.values()[i] : null;
            for (BakedQuad quad : model.getQuads(null, side, RANDOM)) {
                int[] data = quad.getVertices();
                for (int v = 0; v < 4; v++) {
                    float x = Float.intBitsToFloat(data[v * 8]);
                    float y = Float.intBitsToFloat(data[v * 8 + 1]);
                    float z = Float.intBitsToFloat(data[v * 8 + 2]);
                    int s = Mth.clamp((int) ((y - yMin) / (yMax - yMin) * SLICES), 0, SLICES - 1);
                    minX[s] = Math.min(minX[s], x);
                    maxX[s] = Math.max(maxX[s], x);
                    minZ[s] = Math.min(minZ[s], z);
                    maxZ[s] = Math.max(maxZ[s], z);
                }
            }
        }

        // Empty slices take their neighbour's outline, then everything is smoothed so the aura has no steps
        for (int s = 1; s < SLICES; s++) copyIfEmpty(minX, maxX, minZ, maxZ, s, s - 1);
        for (int s = SLICES - 2; s >= 0; s--) copyIfEmpty(minX, maxX, minZ, maxZ, s, s + 1);
        float[] cx = new float[SLICES];
        float[] cz = new float[SLICES];
        float[] hx = new float[SLICES];
        float[] hz = new float[SLICES];
        for (int s = 0; s < SLICES; s++) {
            cx[s] = (minX[s] + maxX[s]) * 0.5F;
            cz[s] = (minZ[s] + maxZ[s]) * 0.5F;
            hx[s] = Math.max(0.02F, (maxX[s] - minX[s]) * 0.5F);
            hz[s] = Math.max(0.02F, (maxZ[s] - minZ[s]) * 0.5F);
        }
        smooth(cx);
        smooth(cz);
        smooth(hx);
        smooth(hz);
        return new Outline(yMin, yMax, cx, cz, hx, hz);
    }

    private static void copyIfEmpty(float[] minX, float[] maxX, float[] minZ, float[] maxZ, int to, int from) {
        if (minX[to] == Float.MAX_VALUE) {
            minX[to] = minX[from];
            maxX[to] = maxX[from];
            minZ[to] = minZ[from];
            maxZ[to] = maxZ[from];
        }
    }

    private static void smooth(float[] values) {
        for (int pass = 0; pass < 2; pass++) {
            float[] copy = values.clone();
            for (int i = 0; i < values.length; i++) {
                float before = copy[Math.max(0, i - 1)];
                float after = copy[Math.min(values.length - 1, i + 1)];
                values[i] = (before + copy[i] * 2.0F + after) * 0.25F;
            }
        }
    }

    /** The outline at a height, between the slices. */
    private static float at(float[] values, float t) {
        float f = Mth.clamp(t, 0.0F, 1.0F) * SLICES - 0.5F;
        int i = Mth.clamp((int) Math.floor(f), 0, SLICES - 1);
        int j = Math.min(SLICES - 1, i + 1);
        return Mth.lerp(Mth.clamp(f - i, 0.0F, 1.0F), values[i], values[j]);
    }

    /**
     * One sleeve of flames around the blade: a tube that follows the outline of the blade, grown by a factor and a
     * margin, tapering to nothing past the tip. It is drawn from both sides.
     */
    private static void sleeve(VertexConsumer consumer, Matrix4f matrix, Outline o, float time, float grow,
                               float margin, float strength, float speed, int turns, float phase) {
        float length = o.yMax - o.yMin;
        float total = length * (1.0F + TIP_EXTENSION);
        float[][][] ring = new float[ROWS + 1][COLUMNS + 1][5]; // x, y, z, u, v
        float[] fade = new float[ROWS + 1];

        for (int r = 0; r <= ROWS; r++) {
            float y = o.yMin + total * r / ROWS;
            float t = (y - o.yMin) / length;
            // Past the tip the flames narrow to a point
            float taper = t <= 1.0F ? 1.0F : (float) Math.pow(Math.max(0.0F, 1.0F - (t - 1.0F) / TIP_EXTENSION), 0.5);
            float cx = at(o.cx, t);
            float cz = at(o.cz, t);
            float hx = at(o.hx, t);
            float hz = at(o.hz, t);
            // Starts soft at the hilt, and thins out towards the tip
            fade[r] = Mth.clamp(r / (ROWS * 0.12F), 0.0F, 1.0F) * (t <= 1.0F ? 1.0F : taper);

            for (int c = 0; c <= COLUMNS; c++) {
                float angle = (float) (2.0D * Math.PI * c / COLUMNS);
                // The flames waver in size as they flow
                float wobble = 1.0F + 0.08F * Mth.sin(time * 1.8F * speed + angle * 2.0F + y * 4.0F + phase)
                        + 0.04F * Mth.sin(time * 2.4F * speed - angle * 3.0F + y * 3.0F);
                float a = (hx * grow + margin) * wobble * taper;
                float b = (hz * grow + margin) * wobble * taper;
                float[] p = ring[r][c];
                p[0] = cx + a * Mth.cos(angle);
                p[1] = y;
                p[2] = cz + b * Mth.sin(angle);
                // The flame texture: round the blade, and along it so that the flames point at the tip and flow
                p[3] = (float) c / COLUMNS * turns + phase * 0.1F + time * speed * 0.07F;
                p[4] = (o.yMax + length * TIP_EXTENSION - y) * 0.55F + time * speed * 0.45F;
            }
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                float[] p0 = ring[r][c];
                float[] p1 = ring[r][c + 1];
                float[] p2 = ring[r + 1][c + 1];
                float[] p3 = ring[r + 1][c];
                float a0 = fade[r];
                float a1 = fade[r + 1];
                vertex(consumer, matrix, p0, strength * a0);
                vertex(consumer, matrix, p1, strength * a0);
                vertex(consumer, matrix, p2, strength * a1);
                vertex(consumer, matrix, p3, strength * a1);
                // The other side
                vertex(consumer, matrix, p3, strength * a1);
                vertex(consumer, matrix, p2, strength * a1);
                vertex(consumer, matrix, p1, strength * a0);
                vertex(consumer, matrix, p0, strength * a0);
            }
        }
    }

    /**
     * Thin ribbons of chakra that lift off the edge of the blade, curl a little and drift away towards the tip.
     * Each one is a streak that travels along its path and fades in and out. They are two crossed ribbons, so they
     * can be seen from every side, and soft at the edges because the vertex alpha is zero there.
     */
    private static void wisps(VertexConsumer consumer, Matrix4f matrix, Outline o, float time) {
        float length = o.yMax - o.yMin;
        for (int i = 0; i < WISPS; i++) {
            float t = WISP_HEIGHT[i];
            float angle = WISP_ANGLE[i];
            float rx = Mth.cos(angle);
            float rz = Mth.sin(angle);
            float tx = -rz;
            float tz = rx;
            float y0 = o.yMin + length * t;
            float sx = at(o.cx, t) + at(o.hx, t) * rx;
            float sz = at(o.cz, t) + at(o.hz, t) * rz;
            float life = (time * WISP_SPEED[i] + WISP_PHASE[i]) % 1.0F;

            float[][] centre = new float[WISP_SEGMENTS + 1][3];
            float[] alpha = new float[WISP_SEGMENTS + 1];
            float[] width = new float[WISP_SEGMENTS + 1];
            for (int j = 0; j <= WISP_SEGMENTS; j++) {
                float s = (float) j / WISP_SEGMENTS;
                float out = 0.012F + 0.30F * WISP_REACH[i] * (float) Math.pow(s, 0.8D);
                float curl = 0.045F * Mth.sin(s * 5.0F + time * 2.5F * WISP_SPEED[i] + i);
                centre[j][0] = sx + rx * out + tx * curl;
                centre[j][1] = y0 + length * 0.14F * s;
                centre[j][2] = sz + rz * out + tz * curl;
                float d = (s - life) / 0.2F;
                float streak = (float) Math.exp(-d * d);
                float edges = Mth.clamp(s / 0.12F, 0.0F, 1.0F) * Mth.clamp((1.0F - s) / 0.15F, 0.0F, 1.0F);
                alpha[j] = 0.85F * streak * edges;
                width[j] = 0.024F * (1.0F - 0.55F * s);
            }

            ribbon(consumer, matrix, centre, alpha, width, tx, 0.0F, tz);
            ribbon(consumer, matrix, centre, alpha, width, rx, 0.0F, rz);
        }
    }

    /** One ribbon along a path: three rows of vertices, clear at both edges and opaque in the middle. */
    private static void ribbon(VertexConsumer consumer, Matrix4f matrix, float[][] c, float[] alpha, float[] width,
                               float wx, float wy, float wz) {
        for (int j = 0; j < c.length - 1; j++) {
            for (int side = -1; side <= 1; side += 2) {
                float[] a = c[j];
                float[] b = c[j + 1];
                float wa = width[j] * side;
                float wb = width[j + 1] * side;
                float[][] quad = {
                        {a[0], a[1], a[2], alpha[j]},
                        {a[0] + wx * wa, a[1] + wy * wa, a[2] + wz * wa, 0.0F},
                        {b[0] + wx * wb, b[1] + wy * wb, b[2] + wz * wb, 0.0F},
                        {b[0], b[1], b[2], alpha[j + 1]}};
                for (int v = 0; v < 4; v++) wispVertex(consumer, matrix, quad[v]);
                for (int v = 3; v >= 0; v--) wispVertex(consumer, matrix, quad[v]);
            }
        }
    }

    private static void wispVertex(VertexConsumer consumer, Matrix4f matrix, float[] p) {
        consumer.addVertex(matrix, p[0], p[1], p[2])
                .setColor(255, 255, 255, Mth.clamp((int) (p[3] * 255.0F), 0, 255))
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float[] p, float alpha) {
        consumer.addVertex(matrix, p[0], p[1], p[2])
                .setColor(255, 255, 255, Mth.clamp((int) (alpha * 255.0F), 0, 255))
                .setUv(p[3], p[4])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
