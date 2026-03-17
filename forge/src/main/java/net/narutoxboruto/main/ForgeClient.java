package net.narutoxboruto.main;


import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.narutoxboruto.client.gui.JutsuStorageScreen;
import net.narutoxboruto.client.model.FireBallModel;
import net.narutoxboruto.client.renderer.shinobi.AbstractShinobiRender;
import net.narutoxboruto.client.renderer.entity.*;
import net.narutoxboruto.client.renderer.throwables.*;
import net.narutoxboruto.menu.ForgeMenuTypes;

import static net.narutoxboruto.entities.ForgeEntities.*;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClient {

    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireBallModel.LAYER_LOCATION, FireBallModel::createBodyLayer);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ForgeMenuTypes.JUTSU_STORAGE.get(), JutsuStorageScreen::new);

            EntityRenderers.register(SHURIKEN.get(), ShurikenRenderer::new);
            EntityRenderers.register(KUNAI.get(), ThrowableWeaponRenderer::new);
            EntityRenderers.register(EXPLOSIVE_KUNAI.get(), ThrowableWeaponRenderer::new);
            EntityRenderers.register(POISON_SENBON.get(), PoisonSenbonRenderer::new);
            EntityRenderers.register(SENBON.get(), SenbonRenderer::new);
            EntityRenderers.register(FUMA_SHURIKEN.get(), FumaShurikenRenderer::new);
            EntityRenderers.register(FIRE_BALL.get(), FireBallRenderer::new);
            EntityRenderers.register(SHARK_BOMB.get(), SharkBombRenderer::new);
            EntityRenderers.register(WATER_DRAGON.get(), WaterDragonRenderer::new);
            EntityRenderers.register(LIGHTNING_ARC.get(), LightningArcRenderer::new);

            EntityRenderers.register(JINPACHI_MUNASHI.get(),
                    (ctx) -> new AbstractShinobiRender(ctx, "jinpachi_munashi", true));
            EntityRenderers.register(KISAME_HOSHIGAKI.get(),
                    (ctx) -> new AbstractShinobiRender(ctx, "kisame_hoshigaki"));
            EntityRenderers.register(ZABUZA_MOMOCHI.get(),
                    (ctx) -> new AbstractShinobiRender(ctx, "zabuza_momochi"));
        });
    }
}
