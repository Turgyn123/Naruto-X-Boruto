package net.narutoxboruto.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.main.platform.Services;

import java.util.Collection;

public class DojutsuCommand {

    public static int giveDojutsu(CommandSourceStack source, Collection<? extends ServerPlayer> targets, String type) {
        if (!Dojutsu.DOJUTSU_TYPES.contains(type)) {
            source.sendFailure(Component.translatable("command.dojutsu.invalid", type));
            return 0;
        }

        for (ServerPlayer player : targets) {
            Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);
            if (dojutsu.hasUnlocked(type)) {
                source.sendFailure(Component.translatable("command.dojutsu.already_has",
                        player.getDisplayName(), Component.translatable("dojutsu." + type)));
                continue;
            }
            // Check if this is a sharingan upgrade (replaces previous level)
            String prerequisite = Dojutsu.SHARINGAN_UPGRADES.get(type);
            if (prerequisite != null && dojutsu.hasUnlocked(prerequisite)) {
                dojutsu.upgrade(prerequisite, type);
            } else {
                dojutsu.unlock(type);
            }
            Services.PLATFORM.setDojutsu(player, dojutsu);
            dojutsu.syncValue(player);
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable("command.dojutsu.give.single",
                    targets.iterator().next().getDisplayName(),
                    Component.translatable("dojutsu." + type)), true);
        } else {
            source.sendSuccess(() -> Component.translatable("command.dojutsu.give.multiple",
                    targets.size(), Component.translatable("dojutsu." + type)), true);
        }
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dojutsu").requires(r -> r.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(Dojutsu.DOJUTSU_TYPES, builder))
                                        .executes(r -> giveDojutsu(r.getSource(),
                                                EntityArgument.getPlayers(r, "target"),
                                                StringArgumentType.getString(r, "type")))))));
    }
}
