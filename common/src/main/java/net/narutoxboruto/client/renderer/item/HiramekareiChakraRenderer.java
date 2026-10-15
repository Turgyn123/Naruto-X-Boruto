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
import org.joml.Vector3f;

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
    private static final int SLICES = 64;
    /** The ring of the aura: rows along the blade and columns around it. */
    private static final int ROWS = 80;
    private static final int COLUMNS = 36;
    /** The aura closes over the tip of the blade, in a rounded cap over the last part of its length. */
    private static final float CAP_START = 0.93F;

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
        // Where the camera is, in the model's own space: the aura is brightest where it is seen edge on, like light
        // round the rim of glass, and clear where it is seen face on
        Vector3f camera = new Matrix4f(matrix).invert().transformPosition(new Vector3f());

        // From the outside in. The outer layers are thin mist, so the sword can be seen through them. The two next
        // to the blade are a light blue glow, brightest along the rim of the blade. Each layer has its own
        // size, speed and turn, so the mist crosses itself.
        // A buffer source ends the buffer before as soon as the next render type is asked for, so each one is fetched
        // just before it is drawn to and never held while another is asked for.
        VertexConsumer mist = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        sleeve(mist, matrix, outline, time, 1.05F, 0.026F, 0.16F, 0.90F, 1, 0.30F, camera, 0.45F, 1.00F);
        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(GLOW));
        sleeve(glow, matrix, outline, time, 1.02F, 0.016F, 0.24F, 1.50F, 1, 3.10F, camera, 0.15F, 2.20F);
        sleeve(glow, matrix, outline, time, 1.00F, 0.008F, 0.32F, 1.80F, 2, 4.20F, camera, 0.25F, 2.60F);

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

        // The points of every slice: the corners of the faces in it, and where the edges of the faces cross the
        // slice borders. A long face that narrows towards the tip then gives the true narrow cross section at the
        // tip, not its wide corners from the base.
        List<List<float[]>> points = new ArrayList<>();
        for (int s = 0; s < SLICES; s++) points.add(new ArrayList<>());
        float step = (yMax - yMin) / SLICES;
        for (BakedQuad quad : quads) {
            int[] data = quad.getVertices();
            float[] xs = new float[4];
            float[] ys = new float[4];
            float[] zs = new float[4];
            for (int v = 0; v < 4; v++) {
                xs[v] = Float.intBitsToFloat(data[v * 8]);
                ys[v] = Float.intBitsToFloat(data[v * 8 + 1]);
                zs[v] = Float.intBitsToFloat(data[v * 8 + 2]);
                int own = Mth.clamp((int) ((ys[v] - yMin) / step), 0, SLICES - 1);
                points.get(own).add(new float[] {xs[v], zs[v]});
            }
            for (int v = 0; v < 4; v++) {
                int w = (v + 1) % 4;
                if (Math.abs(ys[v] - ys[w]) < 1.0e-6F) continue;
                float lowY = Math.min(ys[v], ys[w]);
                float highY = Math.max(ys[v], ys[w]);
                int fromBorder = (int) Math.ceil((lowY - yMin) / step);
                int toBorder = (int) Math.floor((highY - yMin) / step);
                for (int k = Mth.clamp(fromBorder, 0, SLICES); k <= Mth.clamp(toBorder, 0, SLICES); k++) {
                    float t = (yMin + k * step - ys[v]) / (ys[w] - ys[v]);
                    if (t < 0.0F || t > 1.0F) continue;
                    float[] point = {Mth.lerp(t, xs[v], xs[w]), Mth.lerp(t, zs[v], zs[w])};
                    // A border belongs to the slice below it and the slice above it
                    if (k > 0) points.get(k - 1).add(point);
                    if (k < SLICES) points.get(k).add(point);
                }
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

        // Round off only the sharpest corners and steps. First every value is replaced by the biggest one right next
        // to it, then the result is blurred once, so the aura stays outside the blade and still follows its real
        // shape: the narrowing to the tip, the waist and the hooks. The blur is never wider than the first step, so it
        // cannot pull the aura inside the blade.
        float[][] grown = new float[SLICES][COLUMNS + 1];
        for (int s = 0; s < SLICES; s++) {
            for (int c = 0; c < COLUMNS; c++) {
                float biggest = 0.0F;
                for (int ds = -1; ds <= 1; ds++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        biggest = Math.max(biggest, reach[Mth.clamp(s + ds, 0, SLICES - 1)][Math.floorMod(c + dc, COLUMNS)]);
                    }
                }
                grown[s][c] = biggest;
            }
        }
        for (int pass = 0; pass < 1; pass++) {
            float[][] copy = new float[SLICES][];
            for (int s = 0; s < SLICES; s++) copy[s] = grown[s].clone();
            for (int s = 0; s < SLICES; s++) {
                for (int c = 0; c < COLUMNS; c++) {
                    float sum = 0.0F;
                    for (int ds = -1; ds <= 1; ds++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            float w = (ds == 0 ? 0.5F : 0.25F) * (dc == 0 ? 0.5F : 0.25F);
                            sum += w * copy[Mth.clamp(s + ds, 0, SLICES - 1)][Math.floorMod(c + dc, COLUMNS)];
                        }
                    }
                    grown[s][c] = sum;
                }
            }
        }
        for (int s = 0; s < SLICES; s++) {
            System.arraycopy(grown[s], 0, reach[s], 0, COLUMNS);
            reach[s][COLUMNS] = reach[s][0];
        }
        smooth(cx);
        smooth(cz);
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
                               float margin, float strength, float speed, int turns, float phase, Vector3f camera,
                               float rimBase, float rimGain) {
        // The aura breathes: a slow pulse in how bright it is, and a flicker in how far it reaches
        strength *= 0.88F + 0.12F * Mth.sin(time * 3.2F + phase);
        grow *= 1.0F + 0.015F * Mth.sin(time * 5.0F + phase * 2.0F);
        float length = o.yMax - o.yMin;
        float total = length;
        float[][][] ring = new float[ROWS + 1][COLUMNS + 1][6]; // x, y, z, u, v, rim
        float[] fade = new float[ROWS + 1];

        for (int r = 0; r <= ROWS; r++) {
            float y = o.yMin + total * r / ROWS;
            float t = (y - o.yMin) / length;
            // Over the last part of the blade the aura closes in a rounded cap, so it ends at the tip and does not
            // bulge or run on past it
            float cap = Mth.clamp((t - CAP_START) / (1.0F - CAP_START), 0.0F, 1.0F);
            float taper = Mth.sqrt(Math.max(0.0F, 1.0F - cap * cap));
            float cx = at(o.cx, t);
            float cz = at(o.cz, t);
            // Starts soft at the hilt
            fade[r] = Mth.clamp(r / (ROWS * 0.12F), 0.0F, 1.0F);

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
                p[4] = (o.yMax - y) * 0.55F + time * speed * 0.9F;
                // How much the surface is seen edge on from the camera: 0 face on, 1 along the edge
                float vx = camera.x - p[0];
                float vy = camera.y - p[1];
                float vz = camera.z - p[2];
                float len = Mth.sqrt(vx * vx + vy * vy + vz * vz);
                float facing = len < 1.0e-6F ? 1.0F
                        : Math.abs((Mth.cos(angle) * vx + Mth.sin(angle) * vz) / len);
                p[5] = (float) Math.pow(1.0F - Mth.clamp(facing, 0.0F, 1.0F), 1.5D);
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
                float k0 = strength * a0;
                float k1 = strength * a1;
                vertex(consumer, matrix, p0, k0, rimBase, rimGain);
                vertex(consumer, matrix, p1, k0, rimBase, rimGain);
                vertex(consumer, matrix, p2, k1, rimBase, rimGain);
                vertex(consumer, matrix, p3, k1, rimBase, rimGain);
                // The other side
                vertex(consumer, matrix, p3, k1, rimBase, rimGain);
                vertex(consumer, matrix, p2, k1, rimBase, rimGain);
                vertex(consumer, matrix, p1, k0, rimBase, rimGain);
                vertex(consumer, matrix, p0, k0, rimBase, rimGain);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float[] p, float strength, float rimBase,
                               float rimGain) {
        float rim = p[5];
        float alpha = strength * (rimBase + rimGain * rim);
        // Blue in the middle, a lighter blue at the rim, and never white
        int red = Mth.clamp((int) (105.0F + 85.0F * rim), 0, 255);
        int green = Mth.clamp((int) (180.0F + 55.0F * rim), 0, 255);
        consumer.addVertex(matrix, p[0], p[1], p[2])
                .setColor(red, green, 255, Mth.clamp((int) (alpha * 255.0F), 0, 255))
                .setUv(p[3], p[4])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
