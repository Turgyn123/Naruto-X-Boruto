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
import net.narutoxboruto.capabilities.info.Rank;
import net.narutoxboruto.command.argument.RankArgument;
import net.narutoxboruto.util.ModUtil;

import java.util.Collection;

public class RankCommand {
    public static int displayRank(CommandSourceStack pSource, ServerPlayer serverPlayer) {
        Rank rank = Services.PLATFORM.getRank(serverPlayer);
        Component message = Component.translatable("command.rank.display", serverPlayer.getDisplayName(),
                Component.translatable("rank." + rank.getValue()));
        pSource.sendSuccess(() -> message, false);
        return 1;
    }

    public static int setRank(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, String pRank) {
        if (!ModUtil.RANK_LIST.contains(pRank)) {
            pSource.sendFailure(Component.translatable("argument.rank.invalid", pRank));
            return 0;
        }

        for (ServerPlayer player : pTargets) {
            Rank rank = Services.PLATFORM.getRank(player);
            rank.setValue(pRank, player);
        }

        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.rank.set.single",
                    pTargets.iterator().next().getDisplayName(),
                    Component.translatable("rank." + pRank)), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.rank.set.multiple", pTargets.size(),
                    Component.translatable("rank." + pRank)), true);
        }
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("rank").requires((r) -> r.hasPermission(0))
                .then(Commands.literal("set").requires((r) -> r.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("rank", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ModUtil.RANK_LIST, builder))
                                        .executes((r) -> setRank(r.getSource(),
                                                EntityArgument.getPlayers(r, "target"),
                                                StringArgumentType.getString(r, "rank")))))));
    }
}
