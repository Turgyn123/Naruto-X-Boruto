package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.main.Main;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * NeoForge event handler that renders dojutsu eye textures on the player model.
 */
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class DojutsuEyeOverlay {

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        Minecraft mc = Minecraft.getInstance();

        if (player != mc.player) return;
        if (mc.screen != null) return; // don't render while any GUI screen is open

        String leftEye = PlayerData.getDojutsuLeftEye();
        String rightEye = PlayerData.getDojutsuRightEye();
        if ((leftEye == null || leftEye.isEmpty()) && (rightEye == null || rightEye.isEmpty())) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();

        DojutsuEyeRenderer.renderEyes(poseStack, bufferSource, player,
                event.getPartialTick(), leftEye, rightEye);
    }
}
