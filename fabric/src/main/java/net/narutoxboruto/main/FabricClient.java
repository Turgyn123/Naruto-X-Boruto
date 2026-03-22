package net.narutoxboruto.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.narutoxboruto.client.FabricClientEvents;
import net.narutoxboruto.client.gui.JutsuStorageScreen;
import net.narutoxboruto.client.overlay.FabricHudOverlay;
import net.narutoxboruto.client.model.FireBallModel;
import net.narutoxboruto.client.renderer.entity.*;
import net.narutoxboruto.client.renderer.item.FabricLightningCloakRenderer;
import net.narutoxboruto.client.renderer.FabricDojutsuEyeRenderer;
import net.narutoxboruto.client.renderer.shinobi.AbstractShinobiRender;
import net.narutoxboruto.client.renderer.throwables.*;
import net.narutoxboruto.entities.FabricEntities;
import net.narutoxboruto.menu.FabricMenuTypes;
import net.narutoxboruto.networking.FabricPacketHandler;
import net.narutoxboruto.particles.FabricParticles;
import net.narutoxboruto.particles.LightningSparksParticle;
import net.narutoxboruto.util.ModKeyBinds;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register client-side networking
        FabricPacketHandler.registerClient();

        // Register keybinds
        ModKeyBinds.register();

        // Register client events
        FabricClientEvents.register();

        // Register HUD overlay
        FabricHudOverlay.register();

        // Register lightning cloak renderer
        FabricLightningCloakRenderer.register();

        // Register dojutsu eye overlay renderer
        FabricDojutsuEyeRenderer.register();

        // Register menu screens
        MenuScreens.register(FabricMenuTypes.JUTSU_STORAGE, JutsuStorageScreen::new);

        // Register particles
        ParticleFactoryRegistry.getInstance().register(FabricParticles.LIGHTNING_SPARKS, LightningSparksParticle.Provider::new);

        // Register model layers
        EntityModelLayerRegistry.registerModelLayer(FireBallModel.LAYER_LOCATION, FireBallModel::createBodyLayer);

        // Throwable renderers
        EntityRendererRegistry.register(FabricEntities.SHURIKEN, ShurikenRenderer::new);
        EntityRendererRegistry.register(FabricEntities.KUNAI, ThrowableWeaponRenderer::new);
        EntityRendererRegistry.register(FabricEntities.EXPLOSIVE_KUNAI, ThrowableWeaponRenderer::new);
        EntityRendererRegistry.register(FabricEntities.POISON_SENBON, PoisonSenbonRenderer::new);
        EntityRendererRegistry.register(FabricEntities.SENBON, SenbonRenderer::new);
        EntityRendererRegistry.register(FabricEntities.FUMA_SHURIKEN, FumaShurikenRenderer::new);

        // Jutsu entity renderers
        EntityRendererRegistry.register(FabricEntities.FIRE_BALL, FireBallRenderer::new);
        EntityRendererRegistry.register(FabricEntities.SHARK_BOMB, SharkBombRenderer::new);
        EntityRendererRegistry.register(FabricEntities.WATER_DRAGON, WaterDragonRenderer::new);
        EntityRendererRegistry.register(FabricEntities.LIGHTNING_ARC, LightningArcRenderer::new);

        // Boss renderers
        EntityRendererRegistry.register(FabricEntities.JINPACHI_MUNASHI,
                (ctx) -> new AbstractShinobiRender(ctx, "jinpachi_munashi", true));
        EntityRendererRegistry.register(FabricEntities.KISAME_HOSHIGAKI,
                (ctx) -> new AbstractShinobiRender(ctx, "kisame_hoshigaki"));
        EntityRendererRegistry.register(FabricEntities.ZABUZA_MOMOCHI,
                (ctx) -> new AbstractShinobiRender(ctx, "zabuza_momochi"));
    }
}
