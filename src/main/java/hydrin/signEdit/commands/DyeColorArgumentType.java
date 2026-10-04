package hydrin.signEdit.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.DyeColor;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DyeColorArgument implements CustomArgumentType.Converted<DyeColor, String> {
    private static final DynamicCommandExceptionType ERROR_INVALID_DYE_COLOR = new DynamicCommandExceptionType(
            _ -> new LiteralMessage("Invalid dye color.")
    );

    @Override
    public DyeColor convert(final String string) throws CommandSyntaxException {
        try {
            return DyeColor.valueOf(string.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException _) {
            throw ERROR_INVALID_DYE_COLOR.create(string);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (final DyeColor dye : DyeColor.values()) {
            final String name = dye.toString().toUpperCase(Locale.ROOT);

            if (name.startsWith(builder.getRemaining().toUpperCase(Locale.ROOT))) {
                Arrays.stream(DyeColor.values())
                        .filter(d -> d.name().startsWith(name))
                        .forEach(d -> builder.suggest(d.name().toLowerCase(Locale.ROOT)));
            }
        }

        return builder.buildFuture();
    }


    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
