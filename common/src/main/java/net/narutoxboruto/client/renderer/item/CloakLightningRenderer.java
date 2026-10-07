package net.narutoxboruto.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.client.PlayerData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Shared cloak lightning rendering logic for Lightning Chakra Mode.
 * Used by Forge, Fabric, and NeoForge loader-specific event handlers.
 */
public class CloakLightningRenderer {

    /** Mid-layer colour of the bolts. Kept public for any outside users. */
    public static final int CLOAK_LIGHTNING_COLOR = 0x60C0FF;

    // ------------------------------------------------------------------ tuning

    /**
     * Which coordinate frame the PoseStack handed to us is in.
     * false (default): origin at the player's feet, axes aligned to the WORLD (what you normally get from a
     *                  RenderLivingEvent / world-render hook). We rotate the body model by the body yaw.
     * true:            the stack is already rotated to the body (e.g. you render inside a model layer).
     * If arcs on the arms/torso look turned 90 degrees away from the body, flip this.
     */
    private static final boolean STACK_IS_BODY_ALIGNED = false;

    /** First person only draws arcs below this height so the view is not blocked. */
    private static final float FIRST_PERSON_MAX_Y = 1.0f;

    private static final int MAX_ARCS = 90;
    private static final int MAX_ARCS_BURST = 220;
    private static final float BURST_WIDTH_MULT = 1.3f;

    /** Horizontal speed (blocks/tick) at which the "fast mover" effects are at full strength. */
    private static final float FULL_SPEED = 0.30f;

    // Bolt layers, drawn in this order: white-hot core, saturated mid, soft outer glow.
    private static final float[] CORE_RGB = {1.0f, 1.0f, 1.0f};
    private static final float[] MID_RGB = rgb(CLOAK_LIGHTNING_COLOR);
    private static final float[] GLOW_RGB = rgb(0x2F8CFF);
    private static final float[][] LAYER_RGB = {CORE_RGB, MID_RGB, GLOW_RGB};
    private static final float[] LAYER_WIDTH = {0.42f, 1.0f, 3.0f};
    private static final float[] LAYER_ALPHA = {1.0f, 0.85f, 0.22f};
    // Each layer's ribbon pair is turned by a different angle so no two layers are coplanar (no z-fighting).
    private static final float[] LAYER_COS = {1.0f, (float) Math.cos(Math.PI / 6), (float) Math.cos(Math.PI / 3)};
    private static final float[] LAYER_SIN = {0.0f, (float) Math.sin(Math.PI / 6), (float) Math.sin(Math.PI / 3)};

    private static final float TWO_PI = (float) (Math.PI * 2.0);
    private static final Random RANDOM = new Random();

    // ------------------------------------------------------------------ body model (player proportions)

    /** Axis-aligned box in body-local space (+Z = facing direction). Sizes are half-extents. */
    private record Part(float cx, float cy, float cz, float hx, float hy, float hz, float weight) {}

    // The player model is drawn at 15/16 scale: legs 0..0.703, torso 0.703..1.406, head 1.406..1.875.
    private static final Part[] PARTS = {
            new Part(0.000f, 1.055f, 0f, 0.234f, 0.352f, 0.117f, 3.0f), // torso
            new Part(-0.352f, 1.055f, 0f, 0.117f, 0.352f, 0.117f, 1.4f), // arm
            new Part(0.352f, 1.055f, 0f, 0.117f, 0.352f, 0.117f, 1.4f),  // arm
            new Part(-0.117f, 0.352f, 0f, 0.117f, 0.352f, 0.117f, 1.6f), // leg
            new Part(0.117f, 0.352f, 0f, 0.117f, 0.352f, 0.117f, 1.6f),  // leg
            new Part(0.000f, 1.641f, 0f, 0.234f, 0.234f, 0.234f, 1.2f)   // head
    };
    private static final float TOTAL_WEIGHT = totalWeight();
    private static final float HEAD_TOP_Y = 1.875f;

    // ------------------------------------------------------------------ per-player state

    private static final class Arc {
        final float[] pts;   // x,y,z triples, already in the PoseStack frame
        final int count;
        final long birth;
        final long expire;   // exclusive
        final float width;   // half-thickness of the mid layer
        final float alpha;
        final float maxY;
        final int seed;

        Arc(float[] pts, int count, long birth, long expire, float width, float alpha, float maxY, int seed) {
            this.pts = pts;
            this.count = count;
            this.birth = birth;
            this.expire = expire;
            this.width = width;
            this.alpha = alpha;
            this.maxY = maxY;
            this.seed = seed;
        }
    }

    private static final class CloakState {
        final List<Arc> arcs = new ArrayList<>();
        long lastTick = Long.MIN_VALUE;
    }

    private static final Map<UUID, CloakState> STATES = new HashMap<>();
    private static long lastPrune = 0;

    /** First-person cloak rendering - only lower-body arcs so the view is not blocked. */
    public static void renderCloakLightningFirstPerson(PoseStack poseStack, MultiBufferSource buffer, Player player) {
        render(poseStack, buffer, player, true);
    }

    /** Third-person cloak rendering - full body. */
    public static void renderCloakLightning(PoseStack poseStack, MultiBufferSource buffer, Player player) {
        render(poseStack, buffer, player, false);
    }

    private static void render(PoseStack poseStack, MultiBufferSource buffer, Player player, boolean firstPerson) {
        long now = player.level().getGameTime();

        CloakState state = STATES.computeIfAbsent(player.getUUID(), id -> new CloakState());
        if (state.lastTick != now) { // arcs are re-rolled once per game tick, however many frames/passes render
            state.lastTick = now;
            tick(state, player, now);
        }
        prune(now);

        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (Arc arc : state.arcs) {
            if (firstPerson && arc.maxY > FIRST_PERSON_MAX_Y) continue;

            long life = arc.expire - arc.birth;
            long remaining = arc.expire - now;
            // Electric blink: longer-lived arcs drop out on some ticks (never on their first tick).
            if (life >= 3 && now != arc.birth && (arc.seed + now) % 3 == 0) continue;
            // Hard cut, with a dimmer last frame instead of a smooth fade.
            float fade = (life > 1 && remaining == 1) ? 0.55f : 1.0f;

            drawArc(consumer, matrix, arc, fade);
        }
    }

    private static void drawArc(VertexConsumer vc, Matrix4f m, Arc a, float fade) {
        float[] p = a.pts;
        int segs = a.count - 1;
        for (int i = 0; i < segs; i++) {
            int o1 = i * 3;
            int o2 = o1 + 3;
            float x1 = p[o1], y1 = p[o1 + 1], z1 = p[o1 + 2];
            float x2 = p[o2], y2 = p[o2 + 1], z2 = p[o2 + 2];

            float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1.0e-4f) continue;
            dx /= len;
            dy /= len;
            dz /= len;

            // Orthonormal pair (u, v) perpendicular to the segment.
            float rx = 0f, ry = 1f, rz = 0f;
            if (Math.abs(dy) > 0.9f) {
                rx = 1f;
                ry = 0f;
            }
            float ux = dy * rz - dz * ry, uy = dz * rx - dx * rz, uz = dx * ry - dy * rx;
            float ul = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
            ux /= ul;
            uy /= ul;
            uz /= ul;
            float vx = dy * uz - dz * uy, vy = dz * ux - dx * uz, vz = dx * uy - dy * ux;

            // Bolts are slightly thinner at their tips.
            float t1 = (float) i / segs;
            float t2 = (float) (i + 1) / segs;
            float w1 = a.width * (0.45f + 0.55f * (float) Math.sin(Math.PI * t1));
            float w2 = a.width * (0.45f + 0.55f * (float) Math.sin(Math.PI * t2));

            for (int layer = 0; layer < 3; layer++) {
                float[] c = LAYER_RGB[layer];
                float alpha = LAYER_ALPHA[layer] * a.alpha * fade;
                float lw1 = w1 * LAYER_WIDTH[layer];
                float lw2 = w2 * LAYER_WIDTH[layer];
                float ca = LAYER_COS[layer], sa = LAYER_SIN[layer];

                // Two crossed ribbons so the bolt reads from every angle.
                for (int k = 0; k < 2; k++) {
                    float cc = k == 0 ? ca : -sa;
                    float ss = k == 0 ? sa : ca;
                    float px = ux * cc + vx * ss, py = uy * cc + vy * ss, pz = uz * cc + vz * ss;
                    quad(vc, m, x1, y1, z1, x2, y2, z2, px, py, pz, lw1, lw2, c[0], c[1], c[2], alpha);
                }
            }
        }
    }

    /**
     * RenderType.lightning() has face culling on, so each ribbon is emitted with both windings; this way a bolt
     * can never vanish because the camera happens to see its back face.
     */
    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float px, float py, float pz, float w1, float w2,
                             float r, float g, float b, float a) {
        float ax = x1 + px * w1, ay = y1 + py * w1, az = z1 + pz * w1;
        float bx = x1 - px * w1, by = y1 - py * w1, bz = z1 - pz * w1;
        float cx = x2 - px * w2, cy = y2 - py * w2, cz = z2 - pz * w2;
        float dx = x2 + px * w2, dy = y2 + py * w2, dz = z2 + pz * w2;

        vc.addVertex(m, ax, ay, az).setColor(r, g, b, a);
        vc.addVertex(m, bx, by, bz).setColor(r, g, b, a);
        vc.addVertex(m, cx, cy, cz).setColor(r, g, b, a);
        vc.addVertex(m, dx, dy, dz).setColor(r, g, b, a);

        vc.addVertex(m, dx, dy, dz).setColor(r, g, b, a);
        vc.addVertex(m, cx, cy, cz).setColor(r, g, b, a);
        vc.addVertex(m, bx, by, bz).setColor(r, g, b, a);
        vc.addVertex(m, ax, ay, az).setColor(r, g, b, a);
    }

    private static void prune(long now) {
        if (now >= lastPrune && now - lastPrune < 200) return;
        lastPrune = now;
        STATES.values().removeIf(s -> s.lastTick > now || now - s.lastTick > 100);
    }

    // ------------------------------------------------------------------ per-tick generation

    private static void tick(CloakState state, Player player, long now) {
        state.arcs.removeIf(a -> now >= a.expire || a.expire > now + 40);

        // The burst flag in PlayerData belongs to the local player only, so only the local player's cloak uses it.
        boolean burst = player == Minecraft.getInstance().player && PlayerData.isCloakBurstActive();

        Vec3 vel = player.getDeltaMovement();
        float vx = (float) vel.x;
        float vz = (float) vel.z;
        float speed01 = Math.min(1f, (float) Math.sqrt(vx * vx + vz * vz) / FULL_SPEED);

        double yaw = Math.toRadians(player.yBodyRot);
        Gen g = new Gen(state, now, burst, (float) Math.cos(yaw), (float) Math.sin(yaw));

        float k = 1f + 0.8f * speed01; // more speed = more crackle
        int mult = burst ? 3 : 1;

        // 1. Skin crawlers: thin bolts running along the limbs and torso (the core of the look).
        int crawlers = Math.round(8 * k) * mult;
        for (int i = 0; i < crawlers; i++) {
            Part p = pickPart();
            boolean longStrand = p.hy() > 0.3f && RANDOM.nextFloat() < 0.2f;
            g.crawl(p, 0.035f, longStrand ? 0.5f : 0.12f, longStrand ? 0.95f : 0.5f,
                    0.035f, 2 + RANDOM.nextInt(2), 0.010f, 1.0f);
        }

        // 2. Tiny sparks that pop on the surface.
        int sparks = Math.round(8 * k) * mult;
        for (int i = 0; i < sparks; i++) {
            g.spark(pickPart());
        }

        // 3. Outer layer: fainter strands a hand's width off the body.
        int halos = 3 * mult;
        for (int i = 0; i < halos; i++) {
            g.crawl(pickPart(), 0.10f + RANDOM.nextFloat() * 0.12f, 0.2f, 0.55f,
                    0.05f, 2, 0.007f, 0.55f);
        }

        // 4. Hair-raising strands from the top of the head.
        int hair = (RANDOM.nextFloat() < 0.75f ? 1 : 0) + (RANDOM.nextFloat() < 0.4f ? 1 : 0);
        for (int i = 0; i < hair * mult; i++) {
            g.hair();
        }

        // 5. Bolts that jump off the body and fork.
        float jumpChance = burst ? 1.0f : 0.30f * k;
        int jumps = burst ? 4 : (RANDOM.nextFloat() < jumpChance ? 1 : 0);
        for (int i = 0; i < jumps; i++) {
            g.jump(burst ? 1.2f : 0.6f);
        }

        // 6. Lightning crackling on the floor around the feet.
        if (burst || player.onGround()) {
            int ground = burst ? 6 : (RANDOM.nextFloat() < 0.40f * k ? 1 : 0);
            for (int i = 0; i < ground; i++) {
                g.ground(burst ? 1.1f : 0.55f);
            }
        }

        // 7. Streaks trailing behind a fast-moving user.
        if (speed01 > 0.3f) {
            int trails = 1 + (int) (speed01 * 2f);
            for (int i = 0; i < trails; i++) {
                g.trail(vx, vz, speed01);
            }
        }

        // 8. Activation flare: long radial bolts blasting outwards.
        if (burst) {
            for (int i = 0; i < 4; i++) {
                g.radial();
            }
        }
    }

    private static final class Surf {
        final float x, y, z;
        final int axis;   // 0 = x, 1 = y, 2 = z: which face we are on
        final float sign; // which side of that axis

        Surf(float x, float y, float z, int axis, float sign) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.axis = axis;
            this.sign = sign;
        }
    }

    /** Random point on the visible surface of a body part, pushed {@code skin} blocks outward. */
    private static Surf surface(Part p, float skin) {
        float ax = p.hy() * p.hz(), ay = p.hx() * p.hz(), az = p.hx() * p.hy();
        float r = RANDOM.nextFloat() * (ax + ay + az);
        int axis = r < ax ? 0 : (r < ax + ay ? 1 : 2);
        float sign = RANDOM.nextBoolean() ? 1f : -1f;

        if (axis == 1) {
            if (p.hy() >= p.hx() * 1.2f) {
                // Tall parts (limbs, torso): top/bottom faces are hidden in the body or at the floor.
                axis = RANDOM.nextBoolean() ? 0 : 2;
            } else {
                sign = 1f; // head: top only
            }
        }

        float lx = (RANDOM.nextFloat() * 2f - 1f) * p.hx();
        float ly = (RANDOM.nextFloat() * 2f - 1f) * p.hy();
        float lz = (RANDOM.nextFloat() * 2f - 1f) * p.hz();
        switch (axis) {
            case 0 -> lx = sign * (p.hx() + skin);
            case 1 -> ly = sign * (p.hy() + skin);
            default -> lz = sign * (p.hz() + skin);
        }
        return new Surf(p.cx() + lx, p.cy() + ly, p.cz() + lz, axis, sign);
    }

    private static Part pickPart() {
        float r = RANDOM.nextFloat() * TOTAL_WEIGHT;
        for (Part p : PARTS) {
            r -= p.weight();
            if (r <= 0f) return p;
        }
        return PARTS[0];
    }

    private static float clampAlong(Part p, int axis, float v) {
        float c = axis == 0 ? p.cx() : axis == 1 ? p.cy() : p.cz();
        float h = axis == 0 ? p.hx() : axis == 1 ? p.hy() : p.hz();
        return Math.max(c - h - 0.05f, Math.min(c + h + 0.05f, v));
    }

    private static float totalWeight() {
        float t = 0f;
        for (Part p : PARTS) t += p.weight();
        return t;
    }

    private static float[] rgb(int c) {
        return new float[]{((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f};
    }

    /** Builds arcs for one player for one tick. Everything is authored in body-local space. */
    private static final class Gen {
        final CloakState state;
        final long now;
        final boolean burst;
        final float yawCos, yawSin;
        final int cap;
        final float widthScale;

        Gen(CloakState state, long now, boolean burst, float yawCos, float yawSin) {
            this.state = state;
            this.now = now;
            this.burst = burst;
            this.yawCos = yawCos;
            this.yawSin = yawSin;
            this.cap = burst ? MAX_ARCS_BURST : MAX_ARCS;
            this.widthScale = burst ? BURST_WIDTH_MULT : 1f;
        }

        /** A jagged bolt from a to b (body-local). Kinks alternate left/right for the classic zig-zag. */
        void bolt(float[] a, float[] b, float jitter, int life, float width, float alpha) {
            float dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.02f) return;

            int segs = Math.max(2, Math.min(8, Math.round(len / 0.085f)));
            float nx = dx / len, ny = dy / len, nz = dz / len;

            float rx = 0f, ry = 1f, rz = 0f;
            if (Math.abs(ny) > 0.9f) {
                rx = 1f;
                ry = 0f;
            }
            float ux = ny * rz - nz * ry, uy = nz * rx - nx * rz, uz = nx * ry - ny * rx;
            float ul = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
            ux /= ul;
            uy /= ul;
            uz /= ul;
            float vx = ny * uz - nz * uy, vy = nz * ux - nx * uz, vz = nx * uy - ny * ux;

            float[] pts = new float[(segs + 1) * 3];
            float phase = RANDOM.nextFloat() * TWO_PI;
            float sign = RANDOM.nextBoolean() ? 1f : -1f;
            float maxY = -Float.MAX_VALUE;

            for (int i = 0; i <= segs; i++) {
                float t = (float) i / segs;
                float x = a[0] + dx * t, y = a[1] + dy * t, z = a[2] + dz * t;

                if (i > 0 && i < segs) {
                    float env = (float) Math.sin(Math.PI * t); // biggest kinks mid-bolt, tips stay anchored
                    float mag = jitter * env * (0.45f + 0.55f * RANDOM.nextFloat()) * sign;
                    float ang = phase + (RANDOM.nextFloat() - 0.5f) * 1.2f;
                    float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
                    x += (ux * ca + vx * sa) * mag;
                    y += (uy * ca + vy * sa) * mag;
                    z += (uz * ca + vz * sa) * mag;
                    sign = -sign;
                }

                // body-local -> PoseStack frame
                float ox = x, oz = z;
                if (!STACK_IS_BODY_ALIGNED) {
                    ox = x * yawCos - z * yawSin;
                    oz = x * yawSin + z * yawCos;
                }
                pts[i * 3] = ox;
                pts[i * 3 + 1] = y;
                pts[i * 3 + 2] = oz;
                maxY = Math.max(maxY, y);
            }

            int ticks = life + (burst ? 1 : 0);
            while (state.arcs.size() >= cap) state.arcs.remove(0);
            state.arcs.add(new Arc(pts, segs + 1, now, now + ticks, width * widthScale, alpha, maxY,
                    RANDOM.nextInt(1024)));
        }

        /** A bolt that runs along a body part's surface, mostly vertically along limbs. */
        void crawl(Part p, float skin, float minLen, float maxLen, float jitter, int life, float width, float alpha) {
            Surf f = surface(p, skin);

            int tAxis;
            if (f.axis == 1) tAxis = RANDOM.nextBoolean() ? 0 : 2;
            else if (RANDOM.nextFloat() < 0.75f) tAxis = 1;
            else tAxis = (f.axis == 0) ? 2 : 0;
            int oAxis = 3 - f.axis - tAxis;

            float[] a = {f.x, f.y, f.z};
            float[] b = {f.x, f.y, f.z};
            float len = minLen + RANDOM.nextFloat() * (maxLen - minLen);
            float dir = RANDOM.nextBoolean() ? 1f : -1f;
            b[tAxis] = clampAlong(p, tAxis, a[tAxis] + dir * len);
            b[oAxis] = clampAlong(p, oAxis, a[oAxis] + (RANDOM.nextFloat() - 0.5f) * 0.10f);
            bolt(a, b, jitter, life, width, alpha);
        }

        /** A tiny flickering spark on the skin. */
        void spark(Part p) {
            Surf f = surface(p, 0.03f);
            float[] d = {RANDOM.nextFloat() - 0.5f, RANDOM.nextFloat() - 0.5f, RANDOM.nextFloat() - 0.5f};
            d[f.axis] = 0f;
            float l = (float) Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);
            if (l < 1.0e-3f) return;
            float len = 0.04f + RANDOM.nextFloat() * 0.08f;
            float[] a = {f.x, f.y, f.z};
            float[] b = {f.x + d[0] / l * len, f.y + d[1] / l * len, f.z + d[2] / l * len};
            bolt(a, b, 0.02f, 1 + RANDOM.nextInt(2), 0.008f, 1.0f);
        }

        /** A strand of "standing hair" shooting up from the crown. */
        void hair() {
            float hx = (RANDOM.nextFloat() - 0.5f) * 0.32f;
            float hz = (RANDOM.nextFloat() - 0.5f) * 0.32f;
            float y0 = HEAD_TOP_Y - 0.02f;
            float[] a = {hx, y0, hz};
            float[] b = {hx * 1.5f + (RANDOM.nextFloat() - 0.5f) * 0.14f,
                    y0 + 0.18f + RANDOM.nextFloat() * 0.24f,
                    hz * 1.5f + (RANDOM.nextFloat() - 0.5f) * 0.14f};
            bolt(a, b, 0.03f, 2, 0.008f, 0.9f);
        }

        /** A bolt that leaves the body and forks once. */
        void jump(float length) {
            Surf f = surface(pickPart(), 0.03f);
            float nx = f.axis == 0 ? f.sign : 0f;
            float ny = f.axis == 1 ? f.sign : 0f;
            float nz = f.axis == 2 ? f.sign : 0f;

            float dx = nx * 0.9f + (RANDOM.nextFloat() - 0.5f) * 0.8f;
            float dy = ny * 0.9f + (RANDOM.nextFloat() - 0.5f) * 0.8f + 0.15f;
            float dz = nz * 0.9f + (RANDOM.nextFloat() - 0.5f) * 0.8f;
            float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= dl;
            dy /= dl;
            dz /= dl;

            float len = length * (0.7f + 0.6f * RANDOM.nextFloat());
            float[] a = {f.x, f.y, f.z};
            float[] b = {f.x + dx * len, f.y + dy * len, f.z + dz * len};
            bolt(a, b, 0.07f, 2 + RANDOM.nextInt(2), 0.011f, 1.0f);

            // fork from the middle of the bolt
            float[] m = {f.x + dx * len * 0.5f, f.y + dy * len * 0.5f, f.z + dz * len * 0.5f};
            float fx = dx + (RANDOM.nextFloat() - 0.5f) * 1.4f;
            float fy = dy + (RANDOM.nextFloat() - 0.5f) * 1.4f;
            float fz = dz + (RANDOM.nextFloat() - 0.5f) * 1.4f;
            float fl = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
            if (fl < 1.0e-3f) return;
            float flen = len * (0.3f + 0.2f * RANDOM.nextFloat());
            float[] fe = {m[0] + fx / fl * flen, m[1] + fy / fl * flen, m[2] + fz / fl * flen};
            bolt(m, fe, 0.05f, 2, 0.008f, 0.85f);
        }

        /** A bolt crawling outwards over the floor from the feet. */
        void ground(float reach) {
            float th = RANDOM.nextFloat() * TWO_PI;
            float th2 = th + (RANDOM.nextFloat() - 0.5f) * 0.5f;
            float r0 = 0.2f + RANDOM.nextFloat() * 0.3f;
            float r1 = r0 + 0.25f + RANDOM.nextFloat() * reach;
            float[] a = {(float) Math.cos(th) * r0, 0.04f, (float) Math.sin(th) * r0};
            float[] b = {(float) Math.cos(th2) * r1, 0.04f + RANDOM.nextFloat() * 0.03f, (float) Math.sin(th2) * r1};
            bolt(a, b, 0.07f, 2 + RANDOM.nextInt(2), 0.010f, 0.95f);
        }

        /** A streak left behind a fast mover. vx/vz is the WORLD-space velocity. */
        void trail(float vx, float vz, float speed01) {
            float l = (float) Math.sqrt(vx * vx + vz * vz);
            if (l < 1.0e-4f) return;
            float wx = -vx / l, wz = -vz / l;                // opposite to the motion, world space
            float lx = wx * yawCos + wz * yawSin;            // world -> body-local
            float lz = -wx * yawSin + wz * yawCos;

            Surf f = surface(pickPart(), 0.05f);
            float len = (0.4f + RANDOM.nextFloat() * 0.8f) * (0.6f + 0.5f * speed01);
            float[] a = {f.x, f.y, f.z};
            float[] b = {f.x + lx * len, f.y + (RANDOM.nextFloat() - 0.5f) * 0.3f, f.z + lz * len};
            bolt(a, b, 0.06f, 2 + RANDOM.nextInt(2), 0.009f, 0.9f);
        }

        /** Long bolt blasting out of the chest/shoulders (activation flare). */
        void radial() {
            float th = RANDOM.nextFloat() * TWO_PI;
            float el = -0.1f + RANDOM.nextFloat() * 0.6f;
            float ch = (float) Math.cos(el);
            float dx = (float) Math.cos(th) * ch, dy = (float) Math.sin(el), dz = (float) Math.sin(th) * ch;
            float y0 = 0.7f + RANDOM.nextFloat() * 1.0f;
            float len = 0.9f + RANDOM.nextFloat() * 1.1f;
            float[] a = {dx * 0.25f, y0, dz * 0.25f};
            float[] b = {a[0] + dx * len, a[1] + dy * len, a[2] + dz * len};
            bolt(a, b, 0.12f, 3, 0.014f, 1.0f);
        }
    }
}