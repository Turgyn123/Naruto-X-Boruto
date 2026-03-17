package net.narutoxboruto.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.main.Main;

/**
 * Forge-specific event handler for Lightning Chakra Mode cloak VFX.
 * Delegates to the common CloakLightningRenderer for actual rendering.
 */
@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class ForgeLightningCloakRenderer {

    /**
     * First-person cloak rendering via RenderLevelStageEvent (player model not visible in first person).
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!mc.options.getCameraType().isFirstPerson()) return;
        if (!PlayerData.isLightningChakraModeActive()) return;

        float partialTick = event.getPartialTick();
        Vec3 playerPos = player.getPosition(partialTick);
        Vec3 cameraPos = event.getCamera().getPosition();

        // Create PoseStack from the event's Matrix4f
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(event.getPoseStack());

        poseStack.pushPose();
        poseStack.translate(
            playerPos.x - cameraPos.x,
            playerPos.y - cameraPos.y,
            playerPos.z - cameraPos.z
        );

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        CloakLightningRenderer.renderCloakLightningFirstPerson(poseStack, bufferSource, player);
        bufferSource.endBatch(RenderType.lightning());

        poseStack.popPose();
    }

    /**
     * Third-person cloak rendering via RenderPlayerEvent.Post.
     */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        Minecraft mc = Minecraft.getInstance();

        if (player != mc.player) return;
        if (!PlayerData.isLightningChakraModeActive()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();

        poseStack.pushPose();
        CloakLightningRenderer.renderCloakLightning(poseStack, bufferSource, player);
        poseStack.popPose();
    }
}
