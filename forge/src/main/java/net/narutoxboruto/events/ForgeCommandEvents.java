package net.narutoxboruto.events;

import net.narutoxboruto.command.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeCommandEvents {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ClanCommand.register(event.getDispatcher());
        RankCommand.register(event.getDispatcher());
        AffiliationCommand.register(event.getDispatcher());
        ShinobiStatCommand.register(event.getDispatcher());
        ShinobiInfoCommand.register(event.getDispatcher());
        DojutsuCommand.register(event.getDispatcher());
    }
}
