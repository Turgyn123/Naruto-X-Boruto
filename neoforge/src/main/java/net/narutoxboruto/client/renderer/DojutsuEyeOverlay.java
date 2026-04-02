package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;
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
        // Disabled: eyes are now rendered via DojutsuEyeLayer (RenderLayer on player model)
    }
}
