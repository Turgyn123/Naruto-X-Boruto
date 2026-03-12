package net.narutoxboruto.main;


import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.narutoxboruto.client.renderer.throwables.*;

import static net.narutoxboruto.entities.ForgeEntities.*;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClient {

   // @SubscribeEvent
   // static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
   //     event.registerLayerDefinition(FireBallModel.LAYER_LOCATION, FireBallModel::createBodyLayer);
   // }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        event.enqueueWork(() -> {
            // Register static water block as translucent for semi-transparent rendering like vanilla water
           // ItemBlockRenderTypes.setRenderLayer(ModFluids.STATIC_WATER.get(), RenderType.translucent());
           // ItemBlockRenderTypes.setRenderLayer(ModFluidBlocks.STATIC_WATER_BLOCK.get(), RenderType.translucent());

            // your client, only entity renderers, packet receivers, etc.

            EntityRenderers.register(SHURIKEN.get(), ShurikenRenderer::new);
            EntityRenderers.register(KUNAI.get(), ThrowableWeaponRenderer::new);
            EntityRenderers.register(EXPLOSIVE_KUNAI.get(), ThrowableWeaponRenderer::new);
            EntityRenderers.register(POISON_SENBON.get(), PoisonSenbonRenderer::new);
            EntityRenderers.register(SENBON.get(), SenbonRenderer::new);
            EntityRenderers.register(FUMA_SHURIKEN.get(), FumaShurikenRenderer::new);
           // EntityRenderers.register(ModEntities.FIRE_BALL.get(), FireBallRenderer::new);
           // EntityRenderers.register(ModEntities.SHARK_BOMB.get(), SharkBombRenderer::new);
           // EntityRenderers.register(ModEntities.WATER_DRAGON.get(), WaterDragonRenderer::new);
           // EntityRenderers.register(ModEntities.LIGHTNING_ARC.get(), LightningArcRenderer::new);

          //  EntityRenderers.register(ModEntities.JINPACHI_MUNASHI.get(),
          //          (ctx) -> new AbstractShinobiRender(ctx, "jinpachi_munashi", true));
          //  EntityRenderers.register(ModEntities.KISAME_HOSHIGAKI.get(),
          //          (ctx) -> new AbstractShinobiRender(ctx, "kisame_hoshigaki"));
          //  EntityRenderers.register(ModEntities.ZABUZA_MOMOCHI.get(),
          //          (ctx) -> new AbstractShinobiRender(ctx, "zabuza_momochi"));
        });
    }
}
