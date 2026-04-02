package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.main.Main;
import org.joml.Matrix4f;

/**
 * Shared renderer for dojutsu eye overlays on the player model.
 */
public class DojutsuEyeRenderer {

    private static final int FULL_BRIGHT = 15728880;

    /**
     * Renders dojutsu eye textures on the player's face in third-person.
     * PoseStack should be at entity position (entity-local, world-aligned).
     * Coordinates match the DojutsuScreen GUI positioning system exactly.
     */
    public static void renderEyes(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Player player, float partialTick,
                                   String leftEyeType, String rightEyeType,
                                   Dojutsu dojutsu) {
        // If eyes are hidden, don't render
        if (dojutsu != null && !dojutsu.areEyesVisible()) return;

        boolean hasLeft = leftEyeType != null && !leftEyeType.isEmpty()
                && Dojutsu.DOJUTSU_LEFT_EYE.containsKey(leftEyeType);
        boolean hasRight = rightEyeType != null && !rightEyeType.isEmpty()
                && Dojutsu.DOJUTSU_RIGHT_EYE.containsKey(rightEyeType);

        if (!hasLeft && !hasRight) return;

        poseStack.pushPose();

        // Position at eye level for current pose (1.62 standing, 1.27 crouching)
        float headY = player.getEyeHeight(player.getPose());
        poseStack.translate(0, headY, 0);

        // Rotate to match head facing direction (YP for correct yaw direction)
        float headYaw = Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(headYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));

        // --- Map GUI coordinates to world coordinates ---
        // GUI entity scale 110 with player height ~1.8 → ~61 GUI pixels per block
        float guiToWorld = 1.0F / 61.0F;
        float scale = dojutsu != null ? dojutsu.getEyeScale() : 1.0F;

        // Eye quad size in world blocks (GUI: 14 * scale wide, 5 * scale tall)
        float eyeW = 14.0F * scale * guiToWorld;
        float eyeH = 5.0F * scale * guiToWorld;

        // Z = slightly in front of head face surface
        float z = 0.26F;

        // Base positions from GUI: left eye at faceX - 16, right eye at faceX + 2
        // After YP rotation: +X = entity's left. Looking at entity's face:
        //   viewer's left = entity's right = -X
        //   viewer's right = entity's left = +X
        // GUI "left eye" at faceX - 16 (16px viewer's left = -X direction)
        float leftBaseX = -16.0F * guiToWorld;
        float rightBaseX = 2.0F * guiToWorld;
        // Center eyes vertically at eye level (quad goes from -eyeH/2 to +eyeH/2)
        float baseY = -eyeH / 2.0F;

        if (hasLeft) {
            float ox = dojutsu != null ? dojutsu.getLeftEyeOffsetX() * guiToWorld : 0;
            float oy = dojutsu != null ? -dojutsu.getLeftEyeOffsetY() * guiToWorld : 0;
            float ex = leftBaseX + ox;
            float ey = baseY + oy;
            renderFaceQuad(poseStack, bufferSource, getLeftEyeTexture(leftEyeType),
                    z, ex, ey, ex + eyeW, ey + eyeH,
                    0.0F, 0.0F, 1.0F, 1.0F);
        }
        if (hasRight) {
            float ox = dojutsu != null ? dojutsu.getRightEyeOffsetX() * guiToWorld : 0;
            float oy = dojutsu != null ? -dojutsu.getRightEyeOffsetY() * guiToWorld : 0;
            float ex = rightBaseX + ox;
            float ey = baseY + oy;
            renderFaceQuad(poseStack, bufferSource, getRightEyeTexture(rightEyeType),
                    z, ex, ey, ex + eyeW, ey + eyeH,
                    0.0F, 0.0F, 1.0F, 1.0F);
        }

        poseStack.popPose();
    }

    /**
     * Overload for backward compatibility — renders without offset/scale.
     */
    public static void renderEyes(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Player player, float partialTick,
                                   String leftEyeType, String rightEyeType) {
        renderEyes(poseStack, bufferSource, player, partialTick, leftEyeType, rightEyeType, null);
    }

    private static ResourceLocation getLeftEyeTexture(String dojutsuType) {
        return ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_LEFT_EYE.get(dojutsuType) + ".png");
    }

    private static ResourceLocation getRightEyeTexture(String dojutsuType) {
        return ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_RIGHT_EYE.get(dojutsuType) + ".png");
    }

    /**
     * Renders a textured quad facing +Z (toward the viewer when looking at the entity's face).
     * UV mapping: texture left (U=texU1) maps to viewer's left (+X), texture right (U=texU2) maps to viewer's right (-X).
     */
    private static void renderFaceQuad(PoseStack poseStack, MultiBufferSource bufferSource,
                                        ResourceLocation texture, float z,
                                        float x1, float y1, float x2, float y2,
                                        float texU1, float texV1, float texU2, float texV2) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();

        // CCW winding from +Z direction (front-facing quad)
        // x1 = left edge in model (-X = viewer's right), x2 = right edge (+X = viewer's left)
        buffer.addVertex(matrix, x1, y1, z)
                .setColor(255, 255, 255, 255)
                .setUv(texU2, texV2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);
        buffer.addVertex(matrix, x2, y1, z)
                .setColor(255, 255, 255, 255)
                .setUv(texU1, texV2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);
        buffer.addVertex(matrix, x2, y2, z)
                .setColor(255, 255, 255, 255)
                .setUv(texU1, texV1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);
        buffer.addVertex(matrix, x1, y2, z)
                .setColor(255, 255, 255, 255)
                .setUv(texU2, texV1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);
    }
}
