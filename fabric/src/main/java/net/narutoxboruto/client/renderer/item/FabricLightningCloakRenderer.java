package net.narutoxboruto.client.renderer.item;

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
 * Fabric-specific world render callback for Lightning Chakra Mode cloak VFX.
 * Uses WorldRenderEvents.LAST to render after all other world rendering is done.
 * Handles both first-person and third-person rendering in world space.
 */
public class FabricLightningCloakRenderer {

    public static void register() {
        WorldRenderEvents.LAST.register(FabricLightningCloakRenderer::onWorldRenderLast);
    }

    private static void onWorldRenderLast(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!PlayerData.isLightningChakraModeActive()) return;

        float partialTick = context.tickCounter().getGameTimeDeltaPartialTick(false);
        Vec3 playerPos = player.getPosition(partialTick);
        Vec3 cameraPos = context.camera().getPosition();

        // Create PoseStack and apply the view matrix from the context
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(context.positionMatrix());

        poseStack.pushPose();
        poseStack.translate(
            playerPos.x - cameraPos.x,
            playerPos.y - cameraPos.y,
            playerPos.z - cameraPos.z
        );

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        if (firstPerson) {
            CloakLightningRenderer.renderCloakLightningFirstPerson(poseStack, bufferSource, player);
        } else {
            CloakLightningRenderer.renderCloakLightning(poseStack, bufferSource, player);
        }
        bufferSource.endBatch(RenderType.lightning());

        poseStack.popPose();
    }
}
