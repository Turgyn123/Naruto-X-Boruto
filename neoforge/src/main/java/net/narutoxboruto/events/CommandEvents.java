package net.narutoxboruto.events;

import net.narutoxboruto.command.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandEvents {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ClanCommand.register(event.getDispatcher());
        RankCommand.register(event.getDispatcher());
        AffiliationCommand.register(event.getDispatcher());
        ShinobiStatCommand.register(event.getDispatcher());
        ShinobiInfoCommand.register(event.getDispatcher());
    }
}
