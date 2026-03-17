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

public class RankArgument implements ArgumentType<String> {
    private static final DynamicCommandExceptionType ERROR_RANK_INVALID = new DynamicCommandExceptionType(
            (f) -> Component.translatable("argument.rank.invalid", f));

    public static RankArgument rank() {
        return new RankArgument();
    }

    public static String getRank(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
        String argument = pContext.getArgument(pName, String.class);
        if (!ModUtil.RANK_LIST.contains(argument)) {
            throw ERROR_RANK_INVALID.create(argument);
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
                (ModUtil.RANK_LIST), pBuilder) : Suggestions.empty();
    }
}

