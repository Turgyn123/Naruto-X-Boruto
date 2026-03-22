package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.client.PlayerData;

/**
 * Fabric world render callback that renders dojutsu eye textures on the player's face.
 */
public class FabricDojutsuEyeRenderer {

    public static void register() {
        WorldRenderEvents.LAST.register(FabricDojutsuEyeRenderer::onWorldRenderLast);
    }

    private static void onWorldRenderLast(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Only render in third-person
        if (mc.options.getCameraType().isFirstPerson()) return;

        String leftEye = PlayerData.getDojutsuLeftEye();
        String rightEye = PlayerData.getDojutsuRightEye();
        if ((leftEye == null || leftEye.isEmpty()) && (rightEye == null || rightEye.isEmpty())) return;

        float partialTick = context.tickCounter().getGameTimeDeltaPartialTick(false);
        Vec3 playerPos = player.getPosition(partialTick);
        Vec3 cameraPos = context.camera().getPosition();

        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(context.positionMatrix());

        poseStack.pushPose();
        poseStack.translate(
                playerPos.x - cameraPos.x,
                playerPos.y - cameraPos.y,
                playerPos.z - cameraPos.z
        );

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        DojutsuEyeRenderer.renderEyes(poseStack, bufferSource, player, partialTick, leftEye, rightEye);
        bufferSource.endLastBatch();

        poseStack.popPose();
    }
}
