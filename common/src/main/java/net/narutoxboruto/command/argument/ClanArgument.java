package net.narutoxboruto.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.narutoxboruto.util.ModUtil;

import java.util.concurrent.CompletableFuture;

public class ClanArgument implements ArgumentType<String> {
    private static final DynamicCommandExceptionType ERROR_CLAN_INVALID = new DynamicCommandExceptionType(
            (f) -> Component.translatable("argument.clan.invalid", f));

    public static ClanArgument clan() {
        return new ClanArgument();
    }

    public static String getClan(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
        String argument = pContext.getArgument(pName, String.class);
        if (!ModUtil.CLAN_MAP.containsKey(argument)) {
            throw ERROR_CLAN_INVALID.create(argument);
        }
        else {
            return argument;
        }
    }

    public String parse(StringReader pReader) {
        return pReader.readUnquotedString();
    }


    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return pContext.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggest(
                (ModUtil.CLAN_MAP.keySet()), pBuilder) : Suggestions.empty();
    }
}
