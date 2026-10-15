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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The chakra coat of the Hiramekarei, drawn the way the anime does it: not a tint on the blade but a big flame-like
 * aura of chakra around it, soft pale-blue mist with white highlights, flowing along the blade and licking off past
 * its tip.
 *
 * The aura is built around the blade: the model is measured once (how wide it is along its length) and four
 * sleeves are wrapped around that outline, two of thin mist and two of near white glow next to the blade. Each sleeve
 * scrolls its texture along the blade and wavers, so the chakra streams towards the tip. It is drawn fully bright and
 * see-through.
 * The loaders' item renderer mixins just call {@link #render} after the item itself has been drawn.
 */
public final class HiramekareiChakraRenderer {

    private HiramekareiChakraRenderer() {}

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/effect/hiramekarei_chakra.png");

    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/effect/hiramekarei_glow.png");
    private static final RandomSource RANDOM = RandomSource.create(42L);

    /** How wide the blade is along its length: the middle and the half sizes of its outline in slices. */
    private static final int SLICES = 40;
    /** The ring of the aura: rows along the blade and columns around it. */
    private static final int ROWS = 36;
    private static final int COLUMNS = 24;
    /** The flames lick on past the tip of the blade, this much of its length. */
    private static final float TIP_EXTENSION = 0.04F;

    /** The blade in slices: the middle of each, and how far the blade reaches from it in every direction round it. */
    private record Outline(float yMin, float yMax, float[] cx, float[] cz, float[][] reach) {}

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
        sleeve(mist, matrix, outline, time, 1.06F, 0.020F, 0.10F, 0.90F, 1, 0.30F);
        sleeve(mist, matrix, outline, time, 1.035F, 0.012F, 0.14F, 1.20F, 2, 1.70F);
        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(GLOW));
        sleeve(glow, matrix, outline, time, 1.015F, 0.007F, 0.22F, 1.50F, 1, 3.10F);
        sleeve(glow, matrix, outline, time, 1.000F, 0.003F, 0.32F, 1.80F, 2, 4.20F);

        poseStack.popPose();
    }

    /**
     * Measures the blade from one end to the other, in slices. For every slice it finds the middle and how far the
     * blade reaches from there in every direction round it, so the aura goes round the real outline, corners and
     * all, and not round an oval that cuts them off.
     */
    private static Outline measure(BakedModel model) {
        List<BakedQuad> quads = new ArrayList<>();
        float yMin = Float.MAX_VALUE;
        float yMax = -Float.MAX_VALUE;
        for (int i = 0; i <= Direction.values().length; i++) {
            Direction side = i < Direction.values().length ? Direction.values()[i] : null;
            for (BakedQuad quad : model.getQuads(null, side, RANDOM)) {
                quads.add(quad);
                int[] data = quad.getVertices();
                for (int v = 0; v < 4; v++) {
                    float y = Float.intBitsToFloat(data[v * 8 + 1]);
                    yMin = Math.min(yMin, y);
                    yMax = Math.max(yMax, y);
                }
            }
        }
        if (yMax - yMin < 1.0e-4F) return null;

        // The points of every slice. A long face counts in every slice it runs through, not only the ones where it
        // has a corner.
        List<List<float[]>> points = new ArrayList<>();
        for (int s = 0; s < SLICES; s++) points.add(new ArrayList<>());
        for (BakedQuad quad : quads) {
            int[] data = quad.getVertices();
            float[] xs = new float[4];
            float[] zs = new float[4];
            float lo = Float.MAX_VALUE;
            float hi = -Float.MAX_VALUE;
            for (int v = 0; v < 4; v++) {
                xs[v] = Float.intBitsToFloat(data[v * 8]);
                float y = Float.intBitsToFloat(data[v * 8 + 1]);
                zs[v] = Float.intBitsToFloat(data[v * 8 + 2]);
                lo = Math.min(lo, y);
                hi = Math.max(hi, y);
            }
            int first = Mth.clamp((int) ((lo - yMin) / (yMax - yMin) * SLICES), 0, SLICES - 1);
            int last = Mth.clamp((int) ((hi - yMin) / (yMax - yMin) * SLICES), 0, SLICES - 1);
            for (int s = first; s <= last; s++) {
                for (int v = 0; v < 4; v++) points.get(s).add(new float[] {xs[v], zs[v]});
            }
        }

        // Empty slices take their neighbour's points
        for (int s = 1; s < SLICES; s++) if (points.get(s).isEmpty()) points.set(s, points.get(s - 1));
        for (int s = SLICES - 2; s >= 0; s--) if (points.get(s).isEmpty()) points.set(s, points.get(s + 1));

        float[] cx = new float[SLICES];
        float[] cz = new float[SLICES];
        float[][] reach = new float[SLICES][COLUMNS + 1];
        for (int s = 0; s < SLICES; s++) {
            // The outline of the slice is the convex hull of its points, and its middle is the average of the
            // corners of that hull, which is always inside it
            List<float[]> hull = hull(points.get(s));
            float mx = 0.0F;
            float mz = 0.0F;
            for (float[] p : hull) {
                mx += p[0];
                mz += p[1];
            }
            cx[s] = mx / hull.size();
            cz[s] = mz / hull.size();
            for (int c = 0; c <= COLUMNS; c++) {
                float angle = (float) (2.0D * Math.PI * c / COLUMNS);
                float dx = Mth.cos(angle);
                float dz = Mth.sin(angle);
                // Where a line out of the middle leaves the hull. A very thin slice has no real hull, then the
                // furthest point in that direction does.
                float far = hull.size() >= 3 ? rayReach(hull, cx[s], cz[s], dx, dz) : -1.0F;
                if (far < 0.0F) {
                    far = 0.0F;
                    for (float[] p : points.get(s)) {
                        far = Math.max(far, (p[0] - cx[s]) * dx + (p[1] - cz[s]) * dz);
                    }
                }
                reach[s][c] = Math.max(far, 0.015F);
            }
        }

        // Smooth from slice to slice so there are no steps, but never smaller than the blade
        float[][] original = new float[SLICES][];
        for (int s = 0; s < SLICES; s++) original[s] = reach[s].clone();
        for (int pass = 0; pass < 2; pass++) {
            float[][] copy = new float[SLICES][];
            for (int s = 0; s < SLICES; s++) copy[s] = reach[s].clone();
            for (int s = 0; s < SLICES; s++) {
                for (int c = 0; c <= COLUMNS; c++) {
                    float before = copy[Math.max(0, s - 1)][c];
                    float after = copy[Math.min(SLICES - 1, s + 1)][c];
                    reach[s][c] = Math.max(original[s][c], (before + copy[s][c] * 2.0F + after) * 0.25F);
                }
            }
        }
        smooth(cx);
        smooth(cz);
        return new Outline(yMin, yMax, cx, cz, reach);
    }

    /** The convex hull of some points, counter clockwise (Andrew's monotone chain). */
    private static List<float[]> hull(List<float[]> points) {
        List<float[]> sorted = new ArrayList<>(points);
        sorted.sort((p, q) -> p[0] != q[0] ? Float.compare(p[0], q[0]) : Float.compare(p[1], q[1]));
        int n = sorted.size();
        if (n < 3) return sorted;
        float[][] h = new float[2 * n][];
        int k = 0;
        for (int i = 0; i < n; i++) {
            while (k >= 2 && cross(h[k - 2], h[k - 1], sorted.get(i)) <= 0.0F) k--;
            h[k++] = sorted.get(i);
        }
        for (int i = n - 2, t = k + 1; i >= 0; i--) {
            while (k >= t && cross(h[k - 2], h[k - 1], sorted.get(i)) <= 0.0F) k--;
            h[k++] = sorted.get(i);
        }
        List<float[]> result = new ArrayList<>();
        for (int i = 0; i < k - 1; i++) result.add(h[i]);
        return result;
    }

    private static float cross(float[] o, float[] a, float[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }

    /** How far a line from a point inside the hull, in a direction, goes before it leaves the hull. -1 if none. */
    private static float rayReach(List<float[]> hull, float ox, float oz, float dx, float dz) {
        float best = -1.0F;
        for (int i = 0; i < hull.size(); i++) {
            float[] a = hull.get(i);
            float[] b = hull.get((i + 1) % hull.size());
            float ex = b[0] - a[0];
            float ez = b[1] - a[1];
            float denom = dx * ez - dz * ex;
            if (Math.abs(denom) < 1.0e-9F) continue;
            float wx = a[0] - ox;
            float wz = a[1] - oz;
            float t = (wx * ez - wz * ex) / denom;
            float u = (wx * dz - wz * dx) / denom;
            if (t > 0.0F && u >= -1.0e-4F && u <= 1.0001F) best = Math.max(best, t);
        }
        return best;
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

    /** A value of the outline at a height, between the slices. */
    private static float at(float[] values, float t) {
        float f = Mth.clamp(t, 0.0F, 1.0F) * SLICES - 0.5F;
        int i = Mth.clamp((int) Math.floor(f), 0, SLICES - 1);
        int j = Math.min(SLICES - 1, i + 1);
        return Mth.lerp(Mth.clamp(f - i, 0.0F, 1.0F), values[i], values[j]);
    }

    /** How far the blade reaches at a height and in one of the directions round it. */
    private static float reachAt(float[][] reach, float t, int column) {
        float f = Mth.clamp(t, 0.0F, 1.0F) * SLICES - 0.5F;
        int i = Mth.clamp((int) Math.floor(f), 0, SLICES - 1);
        int j = Math.min(SLICES - 1, i + 1);
        return Mth.lerp(Mth.clamp(f - i, 0.0F, 1.0F), reach[i][column], reach[j][column]);
    }

    /**
     * One sleeve around the blade: a tube that follows the real outline of the blade, grown by a factor and a margin,
     * tapering to nothing past the tip. It is drawn from both sides.
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
            float taper = t <= 1.0F ? 1.0F : Math.max(0.0F, 1.0F - (t - 1.0F) / TIP_EXTENSION);
            float cx = at(o.cx, t);
            float cz = at(o.cz, t);
            // Starts soft at the hilt, and thins out towards the tip
            fade[r] = Mth.clamp(r / (ROWS * 0.12F), 0.0F, 1.0F) * (t <= 1.0F ? 1.0F : taper);

            for (int c = 0; c <= COLUMNS; c++) {
                float angle = (float) (2.0D * Math.PI * c / COLUMNS);
                // The flames waver in size as they flow
                float wobble = 1.0F + 0.08F * Mth.sin(time * 1.8F * speed + angle * 2.0F + y * 4.0F + phase)
                        + 0.04F * Mth.sin(time * 2.4F * speed - angle * 3.0F + y * 3.0F);
                float radius = (reachAt(o.reach, t, c) * grow + margin) * wobble * taper;
                float[] p = ring[r][c];
                p[0] = cx + radius * Mth.cos(angle);
                p[1] = y;
                p[2] = cz + radius * Mth.sin(angle);
                // The flame texture: round the blade, and along it so that the flames point at the tip and flow
                p[3] = (float) c / COLUMNS * turns + phase * 0.1F + time * speed * 0.07F;
                p[4] = (o.yMax + length * TIP_EXTENSION - y) * 0.55F + time * speed * 0.9F;
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

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float[] p, float alpha) {
        consumer.addVertex(matrix, p[0], p[1], p[2])
                .setColor(255, 255, 255, Mth.clamp((int) (alpha * 255.0F), 0, 255))
                .setUv(p[3], p[4])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
