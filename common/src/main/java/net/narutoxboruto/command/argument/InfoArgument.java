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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InfoArgument implements ArgumentType<String> {
    public static final List<String> INFO_LIST = Arrays.asList("affiliation", "clan", "rank");
    private static final DynamicCommandExceptionType ERROR_INFO_INVALID = new DynamicCommandExceptionType(
            (f) -> Component.translatable("argument.info.invalid", f));

    public static InfoArgument info() {
        return new InfoArgument();
    }

    public static String getInfo(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
        String argument = pContext.getArgument(pName, String.class);
        if (!INFO_LIST.contains(argument)) {
            throw ERROR_INFO_INVALID.create(argument);
        }
        else {
            return argument;
        }
    }

    public String parse(StringReader pReader) {
        return pReader.readUnquotedString();
    }


    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return SharedSuggestionProvider.suggest((INFO_LIST), pBuilder);
    }
}