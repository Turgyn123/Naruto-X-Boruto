package net.narutoxboruto.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

/**
 * Registers the dojutsu eye render layer onto every PlayerRenderer.
 * Replaces the previous WorldRenderEvents.LAST hack which positioned eyes
 * incorrectly (floating in world space, not attached to the player head).
 */
public class FabricDojutsuEyeRenderer {

    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof PlayerRenderer playerRenderer) {
                        registrationHelper.register(new DojutsuEyeLayer(playerRenderer));
                    }
                });
    }
}
