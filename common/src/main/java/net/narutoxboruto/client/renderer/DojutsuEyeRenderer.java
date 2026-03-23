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
 * Works in entity-local world-aligned coordinates (Y=0 at feet, Y up).
 */
public class DojutsuEyeRenderer {

    private static final int FULL_BRIGHT = 15728880;

    /**
     * Renders dojutsu eye textures on the player's face in third-person.
     * PoseStack should be at entity position (entity-local, world-aligned).
     */
    public static void renderEyes(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Player player, float partialTick,
                                   String leftEyeType, String rightEyeType) {
        boolean hasLeft = leftEyeType != null && !leftEyeType.isEmpty()
                && Dojutsu.DOJUTSU_EYES.containsKey(leftEyeType);
        boolean hasRight = rightEyeType != null && !rightEyeType.isEmpty()
                && Dojutsu.DOJUTSU_EYES.containsKey(rightEyeType);

        if (!hasLeft && !hasRight) return;

        poseStack.pushPose();

        // Position at eye level for current pose (1.62 standing, 1.27 crouching)
        float headY = player.getEyeHeight(player.getPose());
        poseStack.translate(0, headY, 0);

        // Rotate to match head facing direction
        float headYaw = Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        poseStack.mulPose(Axis.YN.rotationDegrees(headYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));

        // Quad dimensions — 1.3x size increase from original 0.16
        float halfW = 0.208F;
        float halfH = 0.208F;
        float z = 0.28F; // slightly in front of head surface (+Z = forward)

        if (hasLeft && hasRight && leftEyeType.equals(rightEyeType)) {
            renderFaceQuad(poseStack, bufferSource, getEyeTexture(leftEyeType),
                    z, -halfW, -halfH, halfW, halfH,
                    0.0F, 0.0F, 1.0F, 1.0F);
        } else {
            if (hasLeft) {
                // Viewer's left half = +X side, texture U 0.0-0.5
                renderFaceQuad(poseStack, bufferSource, getEyeTexture(leftEyeType),
                        z, 0.0F, -halfH, halfW, halfH,
                        0.0F, 0.0F, 0.5F, 1.0F);
            }
            if (hasRight) {
                // Viewer's right half = -X side, texture U 0.5-1.0
                renderFaceQuad(poseStack, bufferSource, getEyeTexture(rightEyeType),
                        z, -halfW, -halfH, 0.0F, halfH,
                        0.5F, 0.0F, 1.0F, 1.0F);
            }
        }

        poseStack.popPose();
    }

    private static ResourceLocation getEyeTexture(String dojutsuType) {
        return ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_EYES.get(dojutsuType) + ".png");
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
