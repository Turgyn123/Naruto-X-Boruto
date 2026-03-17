package net.narutoxboruto.command;

import net.narutoxboruto.main.platform.Services;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.info.Affiliation;
import net.narutoxboruto.util.ModUtil;

import java.util.Collection;

public class AffiliationCommand {
    public static int displayAffiliation(CommandSourceStack pSource, ServerPlayer serverPlayer) {
        Affiliation affiliation = Services.PLATFORM.getAffiliation(serverPlayer);
        Component message = Component.translatable("command.affiliation.display", serverPlayer.getDisplayName(),
                Component.translatable("affiliation." + affiliation.getValue()));
        pSource.sendSuccess(() -> message, false);
        return 1;
    }

    public static int setAffiliation(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, String pAffiliation) {
        // Validate the affiliation
        if (!ModUtil.AFF_LIST.contains(pAffiliation)) {
            pSource.sendFailure(Component.translatable("argument.affiliation.invalid", pAffiliation));
            return 0;
        }

        for (ServerPlayer player : pTargets) {
            Affiliation affiliation = Services.PLATFORM.getAffiliation(player);
            affiliation.setValue(pAffiliation, player);
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.affiliation.set.single",
                    pTargets.iterator().next().getDisplayName(), Component.translatable("affiliation." + pAffiliation)), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.affiliation.set.multiple", pTargets.size(),
                    Component.translatable("affiliation." + pAffiliation)), true);
        }
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("affiliation").requires((r) -> r.hasPermission(0))
                .then(Commands.literal("set").requires((r) -> r.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("affiliation", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ModUtil.AFF_LIST, builder))
                                        .executes((r) -> setAffiliation(r.getSource(),
                                                EntityArgument.getPlayers(r, "target"),
                                                StringArgumentType.getString(r, "affiliation")))))));
    }
}
