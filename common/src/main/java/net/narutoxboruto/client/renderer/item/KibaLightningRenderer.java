package net.narutoxboruto.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.items.swords.Kiba;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lightning on the Kiba sword while its ability is active, in the same style as the Lightning Chakra
 * Mode cloak: thin bolts crawling along the surface, tiny sparks, bolts that jump off and fork, and
 * the same three-layer look and electric blink, because it draws through {@link CloakLightningRenderer}.
 *
 * Bolts are placed on the sword's own model vertices, so they follow the blade whatever the model is.
 * They are re-rolled once per game tick. The loaders' item renderer mixins just call {@link #render}.
 */
public final class KibaLightningRenderer {

    private KibaLightningRenderer() {}

    private static final Random RANDOM = new Random();

    private static final int MAX_ARCS = 70;
    private static final int MAX_ARCS_BURST = 180;

    private static final List<CloakLightningRenderer.Arc> ARCS = new ArrayList<>();
    private static long lastTick = Long.MIN_VALUE;

    // The blade's vertices only change with the model or the view, so they are cached between ticks.
    private static BakedModel cachedModel;
    private static boolean cachedFirstPerson;
    private static List<Vector3f> cachedVertices = List.of();
    private static final Vector3f CACHED_CENTER = new Vector3f();
    /** The vertices grouped from one end of the blade to the other, so bolts can be spread along its length. */
    private static List<List<Vector3f>> cachedBins = List.of();
    private static final int LENGTH_BINS = 12;

    /** Call after the sword itself has been drawn. Does nothing unless this is an active Kiba held in a hand. */
    public static void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                              MultiBufferSource buffer, BakedModel model) {
        if (!(stack.getItem() instanceof Kiba)) return;
        if (!PlayerData.isKibaActive()) return;
        if (context == ItemDisplayContext.GUI || context == ItemDisplayContext.GROUND
                || context == ItemDisplayContext.FIXED) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        poseStack.pushPose();
        // Lines the effect up with the blade of the model.
        poseStack.translate(-0.5f, -0.15f, -0.4f);
        if (firstPerson) {
            poseStack.mulPose(Axis.XP.rotationDegrees(10.0f));
        }

        List<Vector3f> vertices = bladeVertices(model, firstPerson);
        long now = level.getGameTime();
        if (now != lastTick) { // once per game tick, however many times the sword is drawn
            lastTick = now;
            tick(vertices, now, PlayerData.isKibaBurstActive());
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        for (CloakLightningRenderer.Arc arc : ARCS) {
            CloakLightningRenderer.drawAnimated(consumer, matrix, arc, now);
        }

        poseStack.popPose();
    }

    // ------------------------------------------------------------------ generation

    private static void tick(List<Vector3f> vertices, long now, boolean burst) {
        ARCS.removeIf(a -> now >= a.expire || a.expire > now + 40);
        if (vertices.size() < 2 || cachedBins.isEmpty()) return;

        int mult = burst ? 3 : 1;

        // 1. Crawlers: short thin bolts running along the blade surface (the core of the look).
        for (int i = 0; i < 4 * mult; i++) {
            crawler(vertices, now);
        }

        // 2. Tiny sparks popping off the surface.
        for (int i = 0; i < 4 * mult; i++) {
            spark(vertices, now);
        }

        // 3. Bolts that jump off the blade and fork.
        int jumps = burst ? 3 : (RANDOM.nextFloat() < 0.45f ? 1 : 0);
        for (int i = 0; i < jumps; i++) {
            jump(vertices, now);
        }

        // 4. A long strand running most of the blade's length.
        int strands = burst ? 2 : (RANDOM.nextFloat() < 0.3f ? 1 : 0);
        for (int i = 0; i < strands; i++) {
            strand(vertices, now);
        }

        int cap = burst ? MAX_ARCS_BURST : MAX_ARCS;
        while (ARCS.size() > cap) {
            ARCS.remove(0);
        }
    }

    private static void crawler(List<Vector3f> vertices, long now) {
        Vector3f a = pick(vertices);
        Vector3f b = partner(vertices, a, 0.06f, 0.35f);
        if (b == null) return;
        addBolt(a, b, 3 + RANDOM.nextInt(2), 0.012f, now, 2 + RANDOM.nextInt(2), 0.0055f, 1.0f);
    }

    private static void spark(List<Vector3f> vertices, long now) {
        Vector3f a = pick(vertices);
        Vector3f b = new Vector3f(a).add(randomDirection().mul(0.03f + RANDOM.nextFloat() * 0.04f));
        addBolt(a, b, 2, 0.004f, now, 1 + RANDOM.nextInt(2), 0.005f, 1.0f);
    }

    private static void jump(List<Vector3f> vertices, long now) {
        Vector3f a = pick(vertices);
        // Away from the middle of the blade, with some randomness so it does not look radial.
        Vector3f dir = new Vector3f(a).sub(CACHED_CENTER).add(randomDirection().mul(0.6f));
        if (dir.lengthSquared() < 1.0e-6f) dir = randomDirection();
        dir.normalize();

        float length = 0.12f + RANDOM.nextFloat() * 0.18f;
        Vector3f tip = new Vector3f(a).add(new Vector3f(dir).mul(length));
        int life = 2 + RANDOM.nextInt(2);
        addBolt(a, tip, 4, 0.02f, now, life, 0.007f, 1.0f);

        // A fork off the middle of the bolt.
        Vector3f mid = new Vector3f(a).add(new Vector3f(dir).mul(length * 0.5f));
        Vector3f forkTip = new Vector3f(mid).add(randomDirection().mul(length * 0.5f));
        addBolt(mid, forkTip, 3, 0.015f, now, life, 0.005f, 0.8f);
    }

    private static void strand(List<Vector3f> vertices, long now) {
        // One end near the start of the blade, the other near the far end, so it spans most of it.
        Vector3f a = pickBetween(0.0f, 0.35f);
        Vector3f b = pickBetween(0.65f, 1.0f);
        addBolt(a, b, 7, 0.02f, now, 2, 0.0045f, 0.9f);
    }

    /**
     * A random point on the blade, evenly spread along its length. Picking a random vertex instead
     * bunches the bolts where the model has the most detail, which is near the hilt.
     */
    private static Vector3f pick(List<Vector3f> vertices) {
        return pickBetween(0.0f, 1.0f);
    }

    /** A random point from the part of the blade between two fractions of its length (0 to 1). */
    private static Vector3f pickBetween(float from, float to) {
        int n = cachedBins.size();
        int first = Math.min(n - 1, (int) Math.floor(from * n));
        int last = Math.max(first, Math.min(n - 1, (int) Math.ceil(to * n) - 1));
        List<Vector3f> bin = cachedBins.get(first + RANDOM.nextInt(last - first + 1));
        return bin.get(RANDOM.nextInt(bin.size()));
    }

    /** A random other vertex between min and max distance from {@code a}, or null if a few tries find none. */
    private static Vector3f partner(List<Vector3f> vertices, Vector3f a, float min, float max) {
        for (int tries = 0; tries < 10; tries++) {
            Vector3f c = pick(vertices);
            float d = a.distance(c);
            if (d >= min && d <= max) return c;
        }
        return null;
    }

    private static Vector3f randomDirection() {
        Vector3f v = new Vector3f(RANDOM.nextFloat() - 0.5f, RANDOM.nextFloat() - 0.5f, RANDOM.nextFloat() - 0.5f);
        if (v.lengthSquared() < 1.0e-6f) return new Vector3f(0f, 1f, 0f);
        return v.normalize();
    }

    /** A jagged bolt from a to b: the end points are exact, the points in between are knocked off the line. */
    private static void addBolt(Vector3f a, Vector3f b, int segments, float jitter, long now, int life,
                                float width, float alpha) {
        int count = segments + 1;
        float[] pts = new float[count * 3];
        for (int i = 0; i < count; i++) {
            float t = (float) i / segments;
            float x = a.x + (b.x - a.x) * t;
            float y = a.y + (b.y - a.y) * t;
            float z = a.z + (b.z - a.z) * t;
            if (i > 0 && i < segments) {
                x += (RANDOM.nextFloat() - 0.5f) * 2f * jitter;
                y += (RANDOM.nextFloat() - 0.5f) * 2f * jitter;
                z += (RANDOM.nextFloat() - 0.5f) * 2f * jitter;
            }
            pts[i * 3] = x;
            pts[i * 3 + 1] = y;
            pts[i * 3 + 2] = z;
        }
        ARCS.add(new CloakLightningRenderer.Arc(pts, count, now, now + life, width, alpha, 0f, RANDOM.nextInt(1 << 16)));
    }

    // ------------------------------------------------------------------ model vertices

    private static List<Vector3f> bladeVertices(BakedModel model, boolean firstPerson) {
        if (model == cachedModel && firstPerson == cachedFirstPerson && !cachedVertices.isEmpty()) {
            return cachedVertices;
        }

        // First person stays above the hilt (y > 0.25); third person reaches the whole blade, shifted up
        // and tilted by a degree to line up with the tip.
        float clampY = firstPerson ? 0.25f : 0.0f;
        List<Vector3f> vertices = new ArrayList<>();
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads(null, direction, random)) {
                collect(quad, vertices, clampY, firstPerson);
            }
        }
        for (BakedQuad quad : model.getQuads(null, null, random)) {
            collect(quad, vertices, clampY, firstPerson);
        }

        CACHED_CENTER.set(0f, 0f, 0f);
        for (Vector3f v : vertices) {
            CACHED_CENTER.add(v);
        }
        if (!vertices.isEmpty()) {
            CACHED_CENTER.div(vertices.size());
        }

        cachedBins = binAlongBlade(vertices);
        cachedModel = model;
        cachedFirstPerson = firstPerson;
        cachedVertices = vertices;
        return vertices;
    }

    /**
     * Splits the vertices into equal slices along the blade's long axis, dropping empty slices. The axis
     * is found from two far-apart vertices, so it works whatever way the model is turned.
     */
    private static List<List<Vector3f>> binAlongBlade(List<Vector3f> vertices) {
        if (vertices.size() < 2) return List.of();

        Vector3f start = vertices.get(0);
        for (Vector3f v : vertices) {
            if (v.distanceSquared(vertices.get(0)) > start.distanceSquared(vertices.get(0))) start = v;
        }
        Vector3f end = start;
        for (Vector3f v : vertices) {
            if (v.distanceSquared(start) > end.distanceSquared(start)) end = v;
        }

        Vector3f axis = new Vector3f(end).sub(start);
        float lengthSq = axis.lengthSquared();
        if (lengthSq < 1.0e-6f) return List.of(new ArrayList<>(vertices));

        List<List<Vector3f>> bins = new ArrayList<>();
        for (int i = 0; i < LENGTH_BINS; i++) {
            bins.add(new ArrayList<>());
        }
        for (Vector3f v : vertices) {
            float t = new Vector3f(v).sub(start).dot(axis) / lengthSq;
            int index = Math.max(0, Math.min(LENGTH_BINS - 1, (int) (t * LENGTH_BINS)));
            bins.get(index).add(v);
        }
        bins.removeIf(List::isEmpty);
        return bins;
    }

    private static void collect(BakedQuad quad, List<Vector3f> out, float clampY, boolean firstPerson) {
        int[] data = quad.getVertices();
        int vertexCount = data.length / 8;
        for (int i = 0; i < vertexCount; i++) {
            int offset = i * 8;
            float x = Float.intBitsToFloat(data[offset]);
            float y = Float.intBitsToFloat(data[offset + 1]);
            float z = Float.intBitsToFloat(data[offset + 2]);
            if (y <= clampY) continue;
            if (firstPerson) {
                out.add(new Vector3f(x, y, z));
            } else {
                out.add(new Vector3f(x + y * 0.0175f, y + 0.25f, z));
            }
        }
    }
}
