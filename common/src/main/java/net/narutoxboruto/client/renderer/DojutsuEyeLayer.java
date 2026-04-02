package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;
import org.joml.Matrix4f;

/**
 * RenderLayer that draws dojutsu eye textures on the player's head.
 * This runs inside the entity renderer's push/pop block, so body rotation
 * is already applied. We use model.head.translateAndRotate for exact head tracking.
 */
public class DojutsuEyeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final int FULL_BRIGHT = 15728880;

    // GUI entity scale (110) × LER model scale (0.9375) = screen pixels per model block
    private static final float GUI_SCALE = 110.0F * 0.9375F; // 103.125
    // 1 GUI pixel offset = 1/103.125 model blocks in head-local space
    private static final float G2B = 1.0F / GUI_SCALE;

    // Eye Y baseline: derived from GUI entity rendering geometry
    // GUI face center in head-bone coords ≈ -0.127 blocks above pivot
    // Plus 7 GUI-pixel upward shift to align with actual model eye line
    private static final float EYE_LEVEL_Y = -13.09F * G2B - 7.0F * G2B;

    public DojutsuEyeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;
        if (mc.screen != null) return;
        if (mc.options.getCameraType().isFirstPerson()) return;

        Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);
        if (dojutsu != null && !dojutsu.areEyesVisible()) return;

        String leftEye = PlayerData.getDojutsuLeftEye();
        String rightEye = PlayerData.getDojutsuRightEye();

        boolean hasLeft = leftEye != null && !leftEye.isEmpty()
                && Dojutsu.DOJUTSU_LEFT_EYE.containsKey(leftEye);
        boolean hasRight = rightEye != null && !rightEye.isEmpty()
                && Dojutsu.DOJUTSU_RIGHT_EYE.containsKey(rightEye);

        if (!hasLeft && !hasRight) return;

        float scale = dojutsu != null ? dojutsu.getEyeScale() : 1.0F;

        // After model.head.translateAndRotate, coordinates are in BLOCK units (1/16 of a block per model pixel).
        // Head cube: X [-0.25, 0.25], Y [-0.5, 0], Z [-0.25, 0.25] (face at Z=-0.25)
        // In model space: +X = entity left, +Y = down (toward chin), -Z = face direction
        // After LER scale(-1,-1,1) + YP(180): visual X = model X, visual Y = -model Y
        float eyeW = 14.0F * scale * G2B;
        float eyeH = 5.0F * scale * G2B;

        // Face surface: Z = -4 model pixels = -0.25 blocks; offset slightly to avoid z-fighting
        float faceZ = -0.251F;

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);

        if (hasLeft) {
            float ox = dojutsu != null ? dojutsu.getLeftEyeOffsetX() * G2B : 0;
            float oy = dojutsu != null ? dojutsu.getLeftEyeOffsetY() * G2B : 0;
            // GUI: faceX - 16 → model X = -16 * G2B (entity right side, viewer's left)
            // +offsetX in GUI = rightward on screen = +model X
            float ex = -16.0F * G2B + ox;
            // Center eye quad at model eye level; user offset shifts from there
            float ey = EYE_LEVEL_Y - eyeH / 2.0F + oy;

            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                    "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_LEFT_EYE.get(leftEye) + ".png");
            renderEyeQuad(poseStack, bufferSource, tex, faceZ,
                    ex, ey, ex + eyeW, ey + eyeH);
        }
        if (hasRight) {
            float ox = dojutsu != null ? dojutsu.getRightEyeOffsetX() * G2B : 0;
            float oy = dojutsu != null ? dojutsu.getRightEyeOffsetY() * G2B : 0;
            // GUI: faceX + 2 → model X = 2 * G2B
            float ex = 2.0F * G2B + ox;
            float ey = EYE_LEVEL_Y - eyeH / 2.0F + oy;

            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
                    "textures/dojutsu/eyes/" + Dojutsu.DOJUTSU_RIGHT_EYE.get(rightEye) + ".png");
            renderEyeQuad(poseStack, bufferSource, tex, faceZ,
                    ex, ey, ex + eyeW, ey + eyeH);
        }

        poseStack.popPose();
    }

    private static void renderEyeQuad(PoseStack poseStack, MultiBufferSource bufferSource,
                                       ResourceLocation texture, float z,
                                       float x1, float y1, float x2, float y2) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();

        // CCW winding facing -Z (toward viewer looking at face)
        // UV: model y1 (more negative) = visual top → V=0; model y2 = visual bottom → V=1
        buffer.addVertex(matrix, x1, y2, z)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, -1);
        buffer.addVertex(matrix, x2, y2, z)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, -1);
        buffer.addVertex(matrix, x2, y1, z)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, -1);
        buffer.addVertex(matrix, x1, y1, z)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, -1);
    }
}
