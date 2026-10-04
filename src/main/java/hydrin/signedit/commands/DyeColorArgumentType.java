package hydrin.signedit.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.DyeColor;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class DyeColorArgumentType implements CustomArgumentType.Converted<DyeColor, String> {
    private static final SimpleCommandExceptionType ERROR_INVALID_DYE_COLOR = new SimpleCommandExceptionType(
            new LiteralMessage("Invalid dye color.")
    );

    @Override
    public DyeColor convert(final String string) throws CommandSyntaxException {
        try {
            return DyeColor.valueOf(string.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException _) {
            throw ERROR_INVALID_DYE_COLOR.create();
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (DyeColor dye : DyeColor.values()) {
            String name = dye.name().toLowerCase(Locale.ROOT);

            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }


    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
