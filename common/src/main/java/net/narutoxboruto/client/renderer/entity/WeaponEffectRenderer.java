package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.main.Main;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws the weapon effects as real shapes with textures, so that they look like wind, energy and thread and not
 * like particles:
 * <ul>
 *   <li>wind: three ribbons of air that spiral around the way it flies, widening and scrolling forward;</li>
 *   <li>crescent: a flat crescent blade that grows a little as it flies and fades out;</li>
 *   <li>Nuibari: the needle (the model of the sword) that flies to the target and sticks in the ground, with thin threads and a red flash.</li>
 * </ul>
 */
public class WeaponEffectRenderer extends EntityRenderer<WeaponEffectEntity> {

    private static final ResourceLocation WIND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/wind_streak.png");
    private static final ResourceLocation CRESCENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/crescent.png");
    private static final float NEEDLE_FLIGHT_TICKS = 5.0F;
    private static final double NEEDLE_LENGTH = 1.62D;
    private static final float RED_FLASH_TICKS = 14.0F;
    private static final ResourceLocation THREAD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/thread.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final ResourceLocation FLAME_WALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/flame_wall.png");
    private static final ResourceLocation FIRE_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/fire_puff.png");
    private static final ResourceLocation ASH_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/effects/ash_puff.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public WeaponEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WeaponEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        float age = entity.tickCount + partialTick;
        int kind = entity.getKind();
        if (kind == WeaponEffectEntity.FIRE_WAVE) {
            poseStack.pushPose();
            renderFireWave(entity, age, partialTick, poseStack, bufferSource);
            poseStack.popPose();
            return;
        }
        if (kind == WeaponEffectEntity.ASH_CLOUD) {
            poseStack.pushPose();
            renderAshCloud(entity, age, partialTick, poseStack, bufferSource, packedLight);
            poseStack.popPose();
            return;
        }

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

    // ---------------------------------------------------------------- fire jutsu

    /**
     * Fire Release: Great Fire Annihilation: a huge wave of fire that spreads out in front of the caster in a wide fan,
     * as a wall of flame along the ground. The wall is made of curved sheets of flames, one behind the other, each
     * as high as the wave is far, that follow the ground and sway; the front one is the brightest. Round the front
     * there are balls of fire for volume. It is drawn bright and adds its light to what is behind it. The server
     * burns everything in the fan, using the same numbers (see {@link WeaponEffectEntity#WAVE_RANGE}).
     */
    private void renderFireWave(WeaponEffectEntity entity, float age, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        Level level = entity.level();
        Vec3 origin = entity.getPosition(partialTick);
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) * ((float) Math.PI / 180.0F);
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
        Vec3 right = new Vec3(Mth.cos(yaw), 0.0D, Mth.sin(yaw));
        double front = Math.min(WeaponEffectEntity.WAVE_RANGE, age * WeaponEffectEntity.WAVE_SPEED);
        if (front < 1.5D) return;

        double half = Math.toRadians(WeaponEffectEntity.WAVE_HALF_ANGLE);
        float fade = Mth.clamp((entity.getMaxLife() - age) / 14.0F, 0.0F, 1.0F);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer wall = bufferSource.getBuffer(RenderType.eyes(FLAME_WALL_TEXTURE));
        int columns = 36;
        int sheets = 6;
        for (int k = 0; k < sheets; k++) {
            double radius = front - k * 3.6D;
            if (radius < 1.5D) break;
            double height = (2.5D + 4.5D * radius / WeaponEffectEntity.WAVE_RANGE) * Math.min(1.0D, radius / 6.0D);
            // The front sheet is the brightest, the ones behind it fade
            float alpha = fade * (1.0F - k * 0.11F);
            double sway = 0.12D * height;
            Vec3 previousBottom = null;
            Vec3 previousTop = null;
            float previousU = 0.0F;
            float previousAlpha = 0.0F;
            for (int i = 0; i <= columns; i++) {
                double angle = -half + 2.0D * half * i / columns;
                Vec3 spot = forward.scale(Math.cos(angle) * radius).add(right.scale(Math.sin(angle) * radius));
                double groundY = groundAt(level, origin.x + spot.x, origin.z + spot.z, origin.y) - origin.y;
                double lick = Math.sin(age * 0.35D + i * 0.9D + k * 1.7D) * sway;
                Vec3 bottom = new Vec3(spot.x, groundY, spot.z);
                Vec3 top = new Vec3(spot.x + right.x * lick, groundY + height, spot.z + right.z * lick);
                float u = (float) (i * radius * 2.0D * half / columns / 6.0D) + k * 0.37F + (k % 2 == 0 ? 1 : -1) * age * 0.012F;
                // Every part of the sheet flickers a little on its own
                float flicker = 0.85F + 0.15F * Mth.sin(age * 0.9F + i * 1.7F + k * 2.3F);
                float a = alpha * flicker;
                if (previousBottom != null) {
                    quad(wall, matrix, previousBottom, previousU, 1.0F, previousAlpha, bottom, u, 1.0F, a,
                            top, u, 0.0F, a, previousTop, previousU, 0.0F, previousAlpha, 1.0F, 1.0F, 1.0F);
                }
                previousBottom = bottom;
                previousTop = top;
                previousU = u;
                previousAlpha = a;
            }
        }

        // Balls of fire round the front of the wave, for volume
        org.joml.Quaternionf camera = this.entityRenderDispatcher.cameraOrientation();
        VertexConsumer fire = bufferSource.getBuffer(RenderType.eyes(FIRE_PUFF_TEXTURE));
        java.util.Random random = new java.util.Random(entity.getId() * 104729L + 5L);
        for (int i = 0; i < 22; i++) {
            double radius = front - random.nextDouble() * 9.0D;
            double angle = (random.nextDouble() * 2.0D - 1.0D) * half * 0.9D;
            double size = 3.5D + random.nextDouble() * 4.0D;
            double lift = 0.5D + random.nextDouble() * (2.0D + 0.08D * radius) + Math.sin(age * 0.2D + i) * 0.4D;
            if (radius < 2.0D) continue;
            Vec3 spot = forward.scale(Math.cos(angle) * radius).add(right.scale(Math.sin(angle) * radius));
            double groundY = groundAt(level, origin.x + spot.x, origin.z + spot.z, origin.y) - origin.y;
            poseStack.pushPose();
            poseStack.translate(spot.x, groundY + lift, spot.z);
            poseStack.mulPose(camera);
            poseStack.mulPose(Axis.ZP.rotation((float) (random.nextDouble() * Math.PI * 2.0D + age * 0.05D)));
            billboard(fire, poseStack.last().pose(), (float) size, 0.55F * fade, FULL_BRIGHT, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }

    /** The height of the ground at a spot, as far as is loaded, kept near {@code around} so a tree or a cliff does not lift the fire. */
    private static double groundAt(Level level, double x, double z, double around) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
        if (y <= level.getMinBuildHeight()) return around;
        return Mth.clamp(y, around - 3.0D, around + 3.0D);
    }

    /** A quad drawn from both sides: its four corners with their uv and alpha, and a colour. */
    private static void quad(VertexConsumer vc, Matrix4f matrix, Vec3 p0, float u0, float v0, float a0, Vec3 p1, float u1, float v1, float a1,
                             Vec3 p2, float u2, float v2, float a2, Vec3 p3, float u3, float v3, float a3, float r, float g, float b) {
        vertex(vc, matrix, (float) p0.x, (float) p0.y, (float) p0.z, u0, v0, r, g, b, a0);
        vertex(vc, matrix, (float) p1.x, (float) p1.y, (float) p1.z, u1, v1, r, g, b, a1);
        vertex(vc, matrix, (float) p2.x, (float) p2.y, (float) p2.z, u2, v2, r, g, b, a2);
        vertex(vc, matrix, (float) p3.x, (float) p3.y, (float) p3.z, u3, v3, r, g, b, a3);
        vertex(vc, matrix, (float) p3.x, (float) p3.y, (float) p3.z, u3, v3, r, g, b, a3);
        vertex(vc, matrix, (float) p2.x, (float) p2.y, (float) p2.z, u2, v2, r, g, b, a2);
        vertex(vc, matrix, (float) p1.x, (float) p1.y, (float) p1.z, u1, v1, r, g, b, a1);
        vertex(vc, matrix, (float) p0.x, (float) p0.y, (float) p0.z, u0, v0, r, g, b, a0);
    }

    /**
     * Fire Release: Ash Pile Burning. The cloud is made of puffs of ash that hang in the air round the cast point,
     * drift slowly round it and bob a little. Once it is set alight fire runs through the puffs, and when it burns
     * the puffs become balls of fire that swell and fade while the ash is consumed.
     */
    private void renderAshCloud(WeaponEffectEntity entity, float age, float partialTick, PoseStack poseStack,
                                MultiBufferSource bufferSource, int packedLight) {
        int ignition = entity.getTargetId();
        float ignitionAge = ignition < 0 ? -1.0F : ignition + partialTick;
        float fuse = WeaponEffectEntity.ASH_FUSE;
        float burn = WeaponEffectEntity.ASH_BURN;

        float ashAlpha = Mth.clamp(age / 12.0F, 0.0F, 1.0F) * Mth.clamp((entity.getMaxLife() - age) / 30.0F, 0.0F, 1.0F);
        // The ash is consumed by the fire after the explosion
        if (ignitionAge > fuse) ashAlpha *= Mth.clamp(1.0F - (ignitionAge - fuse) / 5.0F, 0.0F, 1.0F);

        int puffs = 34;
        double radius = WeaponEffectEntity.ASH_RADIUS;
        float[][] puff = new float[puffs][5]; // x, y, z, size, roll
        java.util.Random random = new java.util.Random(entity.getId() * 7919L + 13L);
        for (int i = 0; i < puffs; i++) {
            double r = radius * 0.95D * Math.sqrt(random.nextDouble());
            double theta = random.nextDouble() * Math.PI * 2.0D + age * 0.004D * (0.5D + random.nextDouble());
            double phase = random.nextDouble() * Math.PI * 2.0D;
            puff[i][0] = (float) (Math.cos(theta) * r);
            puff[i][1] = (float) (0.4D + random.nextDouble() * 4.2D + Math.sin(age * 0.05D + phase) * 0.35D);
            puff[i][2] = (float) (Math.sin(theta) * r);
            puff[i][3] = 3.2F + random.nextFloat() * 3.0F;
            puff[i][4] = (float) (random.nextDouble() * Math.PI * 2.0D + age * 0.003D);
        }

        org.joml.Quaternionf camera = this.entityRenderDispatcher.cameraOrientation();
        if (ashAlpha > 0.01F) {
            VertexConsumer ash = bufferSource.getBuffer(RenderType.entityTranslucent(ASH_PUFF_TEXTURE));
            for (float[] p : puff) {
                poseStack.pushPose();
                poseStack.translate(p[0], p[1], p[2]);
                poseStack.mulPose(camera);
                poseStack.mulPose(Axis.ZP.rotation(p[4]));
                billboard(ash, poseStack.last().pose(), p[3], 0.7F * ashAlpha, packedLight, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }

        if (ignitionAge >= 0.0F) {
            float fireAlpha;
            float swell;
            if (ignitionAge < fuse) {
                // Fire runs through the cloud
                fireAlpha = 0.55F * Mth.clamp(ignitionAge / fuse, 0.0F, 1.0F);
                swell = 0.8F;
            } else {
                float t = Mth.clamp((ignitionAge - fuse) / burn, 0.0F, 1.0F);
                fireAlpha = 1.0F - t * t;
                swell = 1.0F + t * 0.9F;
            }
            VertexConsumer fire = bufferSource.getBuffer(RenderType.eyes(FIRE_PUFF_TEXTURE));
            for (float[] p : puff) {
                poseStack.pushPose();
                poseStack.translate(p[0], p[1], p[2]);
                poseStack.mulPose(camera);
                poseStack.mulPose(Axis.ZP.rotation(-p[4] * 2.0F));
                billboard(fire, poseStack.last().pose(), p[3] * 1.2F * swell, fireAlpha, FULL_BRIGHT, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    /** A square facing the camera (the pose is already turned to it), with the whole texture on it. */
    private static void billboard(VertexConsumer vc, Matrix4f matrix, float size, float alpha, int light, float r, float g, float b) {
        float h = size * 0.5F;
        billboardVertex(vc, matrix, -h, -h, 0.0F, 1.0F, r, g, b, alpha, light);
        billboardVertex(vc, matrix, h, -h, 1.0F, 1.0F, r, g, b, alpha, light);
        billboardVertex(vc, matrix, h, h, 1.0F, 0.0F, r, g, b, alpha, light);
        billboardVertex(vc, matrix, -h, h, 0.0F, 0.0F, r, g, b, alpha, light);
    }

    private static void billboardVertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float u, float v,
                                        float red, float green, float blue, float alpha, int light) {
        vc.addVertex(matrix, x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
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

        // The needle is the model of the Nuibari itself. Its point is at the top of the model, which is half a block
        // from the middle, so it is turned to point along the flight and the middle goes half a block behind the tip.
        if (ModItems.NUIBARI_ITEM != null) {
            Vec3 middle = tip.subtract(direction.scale(0.5D)).subtract(hand);
            poseStack.pushPose();
            poseStack.translate(middle.x, middle.y, middle.z);
            poseStack.mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F),
                    new Vector3f((float) direction.x, (float) direction.y, (float) direction.z)));
            ItemStack needle = ModItems.NUIBARI_ITEM.getDefaultInstance();
            Minecraft.getInstance().getItemRenderer().renderStatic(needle, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());
            poseStack.popPose();
        }

        Matrix4f matrix = poseStack.last().pose();

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

    /**
     * Rope tied round the body, as in a drawing of it: two slanted wraps cross over the chest and shoulders, and two
     * spiral round the hips and the knees. The wraps are not perfect rings: they slant, they are a little uneven, as
     * if they were tied by hand, and they follow the body, wider across the shoulders than front to back, turning
     * with it. They do not move.
     */
    private static void stitches(VertexConsumer vc, Matrix4f matrix, Entity target, Vec3 feetFromHand, float grow) {
        double height = target.getBbHeight();
        // The sizes are for a body as tall as a person, and scale with the target
        double k = height / 1.8D;
        double yaw = Math.toRadians(target.yBodyRot);
        Vec3 side = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
        Vec3 front = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        // Start height, end height (both as a part of the body), the angle it starts at and how many turns it makes
        // (negative the other way round)
        double[][] wraps = {
                {0.90D, 0.50D, 2.2D, 1.05D},
                {0.88D, 0.46D, 0.9D, -1.05D},
                {0.44D, 0.35D, 0.4D, 1.6D},
                {0.22D, 0.11D, 2.8D, -1.5D}};
        // The wraps start a little wide and pull tight over the first moments
        double pull = 1.0D + 0.5D * (1.0D - grow);
        // The same unevenness every frame for the same target, so the rope does not shimmer
        java.util.Random random = new java.util.Random(target.getId() * 31L + 7L);
        for (double[] wrap : wraps) {
            int segments = 30;
            double jitterPhase = random.nextDouble() * Math.PI * 2.0D;
            Vec3 previous = null;
            for (int i = 0; i <= segments; i++) {
                double t = (double) i / segments;
                double angle = wrap[2] + wrap[3] * Math.PI * 2.0D * t;
                double along = wrap[0] + (wrap[1] - wrap[0]) * t;
                // Slightly loose and uneven, with a little sag
                double loose = 1.0D + 0.05D * Math.sin(t * 9.0D + jitterPhase);
                double y = (along - 0.015D * Math.sin(t * Math.PI)) * height;
                double rx = bodyHalfWidth(along) * k * loose * pull;
                double rz = bodyHalfDepth(along) * k * loose * pull;
                Vec3 point = feetFromHand
                        .add(side.scale(Math.cos(angle) * rx))
                        .add(front.scale(Math.sin(angle) * rz))
                        .add(0.0D, y, 0.0D);
                if (previous != null) strip(vc, matrix, previous, point, 0.006D, 1.0F, true);
                previous = point;
            }
        }
    }

    /** Half the width of a person's body across at a part of its height: the legs, the hips, then the arms at the sides. */
    private static double bodyHalfWidth(double along) {
        return piecewise(along, 0.0D, 0.28D, 0.30D, 0.28D, 0.46D, 0.38D, 0.60D, 0.47D, 0.88D, 0.50D);
    }

    /** Half the depth of the body, front to back. */
    private static double bodyHalfDepth(double along) {
        return piecewise(along, 0.0D, 0.18D, 0.30D, 0.19D, 0.46D, 0.21D, 0.60D, 0.22D, 0.88D, 0.24D);
    }

    /** A line through the points (x, value) given in pairs, between them and flat outside them. */
    private static double piecewise(double x, double... points) {
        if (x <= points[0]) return points[1];
        for (int i = 2; i < points.length; i += 2) {
            if (x <= points[i]) {
                double t = (x - points[i - 2]) / (points[i] - points[i - 2]);
                return points[i - 1] + (points[i + 1] - points[i - 1]) * t;
            }
        }
        return points[points.length - 1];
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
