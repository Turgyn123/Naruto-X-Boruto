package net.narutoxboruto.events;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.narutoxboruto.command.*;

public class FabricCommandEvents {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ClanCommand.register(dispatcher);
            RankCommand.register(dispatcher);
            AffiliationCommand.register(dispatcher);
            ShinobiStatCommand.register(dispatcher);
            ShinobiInfoCommand.register(dispatcher);
        });
    }
}
