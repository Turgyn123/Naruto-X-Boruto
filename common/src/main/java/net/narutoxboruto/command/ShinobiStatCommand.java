package net.narutoxboruto.command;

import net.narutoxboruto.main.platform.Services;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.util.ModUtil;

import java.util.Collection;

public class ShinobiStatCommand {
    public static int addStat(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, String pStat, int pValue) {
        for (ServerPlayer serverPlayer : pTargets) {
            switch (pStat) {
                case "genjutsu" -> {
                    Genjutsu stat = Services.PLATFORM.getGenjutsu(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "kenjutsu" -> {
                    Kenjutsu stat = Services.PLATFORM.getKenjutsu(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "kinjutsu" -> {
                    Kinjutsu stat = Services.PLATFORM.getKinjutsu(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "medical" -> {
                    Medical stat = Services.PLATFORM.getMedical(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "ninjutsu" -> {
                    Ninjutsu stat = Services.PLATFORM.getNinjutsu(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "senjutsu" -> {
                    Senjutsu stat = Services.PLATFORM.getSenjutsu(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "shurikenjutsu" -> {
                    Shurikenjutsu stat = Services.PLATFORM.getShurikenjutsu(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "speed" -> {
                    Speed stat = Services.PLATFORM.getSpeed(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "summoning" -> {
                    Summoning stat = Services.PLATFORM.getSummoning(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
                case "taijutsu" -> {
                    Taijutsu stat = Services.PLATFORM.getTaijutsu(serverPlayer);
                    stat.addValue(pValue, serverPlayer);
                }
            }
        }
        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_stat.add.single",
                    pTargets.iterator().next().getDisplayName(), pStat, pValue), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_stat.add.multiple",
                    pStat, pTargets.size(), pValue), true);
        }
        return 1;
    }

    public static int subStat(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, String pStat, int pValue) {
        for (ServerPlayer serverPlayer : pTargets) {
            switch (pStat) {
                case "genjutsu" -> {
                    Genjutsu stat = Services.PLATFORM.getGenjutsu(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "kenjutsu" -> {
                    Kenjutsu stat = Services.PLATFORM.getKenjutsu(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "kinjutsu" -> {
                    Kinjutsu stat = Services.PLATFORM.getKinjutsu(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "medical" -> {
                    Medical stat = Services.PLATFORM.getMedical(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "ninjutsu" -> {
                    Ninjutsu stat = Services.PLATFORM.getNinjutsu(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "senjutsu" -> {
                    Senjutsu stat = Services.PLATFORM.getSenjutsu(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "shurikenjutsu" -> {
                    Shurikenjutsu stat = Services.PLATFORM.getShurikenjutsu(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "speed" -> {
                    Speed stat = Services.PLATFORM.getSpeed(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "summoning" -> {
                    Summoning stat = Services.PLATFORM.getSummoning(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
                case "taijutsu" -> {
                    Taijutsu stat = Services.PLATFORM.getTaijutsu(serverPlayer);
                    stat.subValue(pValue, serverPlayer);
                }
            }
        }
        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_stat.sub.single",
                    pTargets.iterator().next().getDisplayName(), pStat, pValue), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_stat.sub.multiple",
                    pStat, pTargets.size(), pValue), true);
        }
        return 1;
    }

    public static int setStat(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, String pStat, int pValue) {
        for (ServerPlayer serverPlayer : pTargets) {
            switch (pStat) {
                case "genjutsu" -> {
                    Genjutsu stat = Services.PLATFORM.getGenjutsu(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "kenjutsu" -> {
                    Kenjutsu stat = Services.PLATFORM.getKenjutsu(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "kinjutsu" -> {
                    Kinjutsu stat = Services.PLATFORM.getKinjutsu(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "medical" -> {
                    Medical stat = Services.PLATFORM.getMedical(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "ninjutsu" -> {
                    Ninjutsu stat = Services.PLATFORM.getNinjutsu(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "senjutsu" -> {
                    Senjutsu stat = Services.PLATFORM.getSenjutsu(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "shurikenjutsu" -> {
                    Shurikenjutsu stat = Services.PLATFORM.getShurikenjutsu(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "speed" -> {
                    Speed stat = Services.PLATFORM.getSpeed(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "summoning" -> {
                    Summoning stat = Services.PLATFORM.getSummoning(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
                case "taijutsu" -> {
                    Taijutsu stat = Services.PLATFORM.getTaijutsu(serverPlayer);
                    stat.setValue(pValue, serverPlayer);
                }
            }
        }
        if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_stat.set.single",
                    pTargets.iterator().next().getDisplayName(), pStat, pValue), true);
        } else {
            pSource.sendSuccess(() -> Component.translatable("command.shinobi_stat.set.multiple",
                    pStat, pTargets.size(), pValue), true);
        }
        return 1;
    }

    public static int displayStat(CommandSourceStack pSource, String pStat, ServerPlayer serverPlayer) {
        int value = 0;
        switch (pStat) {
            case "genjutsu" -> {
                Genjutsu stat = Services.PLATFORM.getGenjutsu(serverPlayer);
                value = stat.getValue();
            }
            case "kenjutsu" -> {
                Kenjutsu stat = Services.PLATFORM.getKenjutsu(serverPlayer);
                value = stat.getValue();
            }
            case "kinjutsu" -> {
                Kinjutsu stat = Services.PLATFORM.getKinjutsu(serverPlayer);
                value = stat.getValue();
            }
            case "medical" -> {
                Medical stat = Services.PLATFORM.getMedical(serverPlayer);
                value = stat.getValue();
            }
            case "ninjutsu" -> {
                Ninjutsu stat = Services.PLATFORM.getNinjutsu(serverPlayer);
                value = stat.getValue();
            }
            case "senjutsu" -> {
                Senjutsu stat = Services.PLATFORM.getSenjutsu(serverPlayer);
                value = stat.getValue();
            }
            case "shurikenjutsu" -> {
                Shurikenjutsu stat = Services.PLATFORM.getShurikenjutsu(serverPlayer);
                value = stat.getValue();
            }
            case "speed" -> {
                Speed stat = Services.PLATFORM.getSpeed(serverPlayer);
                value = stat.getValue();
            }
            case "summoning" -> {
                Summoning stat = Services.PLATFORM.getSummoning(serverPlayer);
                value = stat.getValue();
            }
            case "taijutsu" -> {
                Taijutsu stat = Services.PLATFORM.getTaijutsu(serverPlayer);
                value = stat.getValue();
            }
        }
        int finalValue = value;
        pSource.sendSuccess(() -> Component.translatable("command.shinobi_stat.display",
                serverPlayer.getDisplayName(), pStat, finalValue), true);
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("shinobi_stat").requires((r) -> r.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("stat", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ModUtil.STAT_LIST, builder))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes((r) -> addStat(r.getSource(),
                                                        EntityArgument.getPlayers(r, "target"),
                                                        StringArgumentType.getString(r, "stat"),
                                                        IntegerArgumentType.getInteger(r, "value")))))))
                .then(Commands.literal("sub")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("stat", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ModUtil.STAT_LIST, builder))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0)).executes(
                                                (r) -> subStat(r.getSource(), EntityArgument.getPlayers(r, "target"),
                                                        StringArgumentType.getString(r, "stat"),
                                                        IntegerArgumentType.getInteger(r, "value")))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("stat", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(ModUtil.STAT_LIST, builder))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes((r) -> setStat(r.getSource(),
                                                        EntityArgument.getPlayers(r, "target"),
                                                        StringArgumentType.getString(r, "stat"),
                                                        IntegerArgumentType.getInteger(r, "value"))))))));

    }
}
