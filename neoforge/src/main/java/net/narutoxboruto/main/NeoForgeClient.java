package net.narutoxboruto.main;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.narutoxboruto.client.renderer.throwables.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import static net.narutoxboruto.entities.NeoForgeEntities.*;

@Mod(value = Main.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgeClient {
    public NeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

   // @SubscribeEvent
   // static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
   //     event.registerLayerDefinition(FireBallModel.LAYER_LOCATION, FireBallModel::createBodyLayer);
   // }

   // @SubscribeEvent
   // static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
   //     event.register(ModMenuTypes.JUTSU_STORAGE.get(), JutsuStorageScreen::new);
   // }

   // @SubscribeEvent
   // static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
   //     // Register static water fluid rendering with water textures
   //     event.registerFluidType(StaticWaterClientExtension.INSTANCE, ModFluids.STATIC_WATER_TYPE.get());

   //     // Register custom renderer for Kiba sword (lightning effects)
   //     event.registerItem(KibaClientExtension.INSTANCE, ModItems.KIBA.get());
   // }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        event.enqueueWork(() -> {
            // Register static water block as translucent for semi-transparent rendering like vanilla water
          //  ItemBlockRenderTypes.setRenderLayer(ModFluids.STATIC_WATER.get(), RenderType.translucent());
          //  ItemBlockRenderTypes.setRenderLayer(ModFluidBlocks.STATIC_WATER_BLOCK.get(), RenderType.translucent());

            EntityRenderers.register(SHURIKEN.get(), ShurikenRenderer::new);
            EntityRenderers.register(KUNAI.get(), ThrowableWeaponRenderer::new);
            EntityRenderers.register(EXPLOSIVE_KUNAI.get(), ThrowableWeaponRenderer::new);
            EntityRenderers.register(POISON_SENBON.get(), PoisonSenbonRenderer::new);
            EntityRenderers.register(SENBON.get(), SenbonRenderer::new);
            EntityRenderers.register(FUMA_SHURIKEN.get(), FumaShurikenRenderer::new);
          //  EntityRenderers.register(FIRE_BALL.get(), FireBallRenderer::new);
          //  EntityRenderers.register(SHARK_BOMB.get(), SharkBombRenderer::new);
          //  EntityRenderers.register(WATER_DRAGON.get(), WaterDragonRenderer::new);
          //  EntityRenderers.register(LIGHTNING_ARC.get(), LightningArcRenderer::new);

            // EntityRenderers.register(ModEntities.JINPACHI_MUNASHI.get(),
            //                 (ctx) -> new AbstractShinobiRender(ctx, "jinpachi_munashi", true));
            // EntityRenderers.register(ModEntities.KISAME_HOSHIGAKI.get(),
            //                 (ctx) -> new AbstractShinobiRender(ctx, "kisame_hoshigaki"));
            // EntityRenderers.register(ModEntities.ZABUZA_MOMOCHI.get(),
            //                 (ctx) -> new AbstractShinobiRender(ctx, "zabuza_momochi"));
        });
    }
}
