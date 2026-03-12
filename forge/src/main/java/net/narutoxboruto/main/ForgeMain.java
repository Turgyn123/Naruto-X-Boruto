package net.narutoxboruto.main;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.narutoxboruto.entities.ForgeEntities;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.items.ForgeItems;
import net.narutoxboruto.items.ForgeTab;

@Mod(Main.MOD_ID)
public class ForgeMain {

    public ForgeMain(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();

        ForgeItems.register(eventBus);
        ForgeTab.register(eventBus);
        ForgeEntities.register(eventBus);
        eventBus.addListener(this::Entities);
        Main.init();
    }

    private void Entities(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModEntities.KUNAI = ForgeEntities.KUNAI.get();
            ModEntities.EXPLOSIVE_KUNAI = ForgeEntities.EXPLOSIVE_KUNAI.get();
            ModEntities.SHURIKEN = ForgeEntities.SHURIKEN.get();
            ModEntities.SENBON = ForgeEntities.SENBON.get();
            ModEntities.POISON_SENBON = ForgeEntities.POISON_SENBON.get();
            ModEntities.FUMA_SHURIKEN = ForgeEntities.FUMA_SHURIKEN.get();
        });
    }
}