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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ShinobiStatArgument implements ArgumentType<String> {
    private static final DynamicCommandExceptionType ERROR_STAT_NOT_FOUND = new DynamicCommandExceptionType(
            (f) -> Component.translatable("argument.shinobi_stat.invalid", f));

    public static ShinobiStatArgument shinobiStat() {
        return new ShinobiStatArgument();
    }

    public static String getStat(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
        String argument = pContext.getArgument(pName, String.class);
        if (!ModUtil.STAT_LIST.contains(argument)) {
            throw ERROR_STAT_NOT_FOUND.create(argument);
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
                (ModUtil.STAT_LIST), pBuilder) : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return ModUtil.STAT_LIST;
    }
}
