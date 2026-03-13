package net.narutoxboruto.main;


import net.narutoxboruto.capabilities.info.Affiliation;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.entities.NeoForgeEntities;
import net.narutoxboruto.items.NeoForgeItems;
import net.narutoxboruto.items.NeoForgeTab;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Main.MOD_ID)
public class NeoForgeMain {

    public NeoForgeMain(IEventBus eventBus) {

        NeoForgeItems.register(eventBus);
        NeoForgeTab.register(eventBus);
        NeoForgeEntities.register(eventBus);
        NeoForgeCapabilities.register(eventBus);
        eventBus.addListener(this::Entities);
        Main.init();
    }

    private void Entities(final FMLCommonSetupEvent event) {
        ModEntities.KUNAI = NeoForgeEntities.KUNAI.get();
        ModEntities.EXPLOSIVE_KUNAI = NeoForgeEntities.EXPLOSIVE_KUNAI.get();
        ModEntities.SHURIKEN = NeoForgeEntities.SHURIKEN.get();
        ModEntities.SENBON = NeoForgeEntities.SENBON.get();
        ModEntities.POISON_SENBON = NeoForgeEntities.POISON_SENBON.get();
        ModEntities.FUMA_SHURIKEN = NeoForgeEntities.FUMA_SHURIKEN.get();
    }

}