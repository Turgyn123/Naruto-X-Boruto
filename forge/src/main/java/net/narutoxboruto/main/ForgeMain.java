package net.narutoxboruto.main;

import net.minecraftforge.fml.common.Mod;

@Mod(Main.MOD_ID)
public class ForgeMain {

    public ForgeMain() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Main.LOG.info("Hello Forge world!");
        Main.init();

    }
}