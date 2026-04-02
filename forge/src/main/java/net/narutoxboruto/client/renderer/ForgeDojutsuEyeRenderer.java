package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge event handler that renders dojutsu eye textures on the player model.
 */
@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class ForgeDojutsuEyeRenderer {

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        Minecraft mc = Minecraft.getInstance();

        if (player != mc.player) return;
        if (mc.screen != null) return;
        if (mc.options.getCameraType().isFirstPerson()) return;

        String leftEye = PlayerData.getDojutsuLeftEye();
        String rightEye = PlayerData.getDojutsuRightEye();
        if ((leftEye == null || leftEye.isEmpty()) && (rightEye == null || rightEye.isEmpty())) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);

        DojutsuEyeRenderer.renderEyes(poseStack, bufferSource, player,
                event.getPartialTick(), leftEye, rightEye, dojutsu);
    }
}
