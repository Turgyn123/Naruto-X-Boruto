package net.narutoxboruto.command;

import net.narutoxboruto.main.platform.Services;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.info.ShinobiPoints;


import java.util.Collection;

public class ShinobiInfoCommand {
    public static int addSP(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int pValue) {
        for (ServerPlayer serverPlayer : pTargets) {
            ShinobiPoints sP = Services.PLATFORM.getShinobiPoints(serverPlayer);
            sP.addValue(pValue, serverPlayer);
        }
        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_info.add.single",
                    pTargets.iterator().next().getDisplayName(), "shinobi points", pValue), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_info.add.multiple",
                    "shinobi_points", pTargets.size(), pValue), true);
        }
        return 1;
    }

    public static int subSP(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int pValue) {
        for (ServerPlayer serverPlayer : pTargets) {
            ShinobiPoints sP = Services.PLATFORM.getShinobiPoints(serverPlayer);
            sP.subValue(pValue, serverPlayer);
        }
        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_info.sub.single",
                    pTargets.iterator().next().getDisplayName(), "shinobi points", pValue), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_info.sub.multiple",
                    "shinobi_points", pTargets.size(), pValue), true);
        }
        return 1;
    }

    public static int setSP(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int pValue) {
        for (ServerPlayer serverPlayer : pTargets) {
            ShinobiPoints sP = Services.PLATFORM.getShinobiPoints(serverPlayer);
            sP.setValue(pValue, serverPlayer);
        }
        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_info.set.single",
                    pTargets.iterator().next().getDisplayName(), "shinobi points", pValue), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_info.set.multiple",
                    "shinobi_points", pTargets.size(), pValue), true);
        }
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("shinobi_info").requires((r) -> r.hasPermission(2))
                .then(Commands.literal("sp")
                        .then(Commands.literal("set")
                                .then(Commands.argument("target", EntityArgument.players())
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0)).executes(
                                                (r) -> setSP(r.getSource(), EntityArgument.getPlayers(r, "target"),
                                                        IntegerArgumentType.getInteger(r, "value"))))))
                        .then(Commands.literal("sub")
                                .then(Commands.argument("target", EntityArgument.players())
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0)).executes(
                                                (r) -> subSP(r.getSource(), EntityArgument.getPlayers(r, "target"),
                                                        IntegerArgumentType.getInteger(r, "value"))))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("target", EntityArgument.players())
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0)).executes(
                                                (r) -> addSP(r.getSource(), EntityArgument.getPlayers(r, "target"),
                                                        IntegerArgumentType.getInteger(r, "value"))))))));
    }
}
