package net.narutoxboruto.main;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Main.MOD_ID)
public class NeoForgeMain {

    public NeoForgeMain(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Main.LOG.info("Hello NeoForge world!");
        Main.init();

    }
}