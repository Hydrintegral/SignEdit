package hydrin.signedit.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SignTypeArgumentType implements CustomArgumentType.Converted<Material, String> {
    private static final Set<Material> SIGNS = Tag.ALL_SIGNS.getValues();

    private static final DynamicCommandExceptionType ERROR_INVALID_SIGN_TYPE = new DynamicCommandExceptionType(
            _ -> new LiteralMessage("Invalid sign type.")
    );

    @Override
    public Material convert(String string) throws CommandSyntaxException {
        try {
            for (Material sign : SIGNS) {
                if (sign.name().equals(string.toUpperCase(Locale.ROOT))) {
                    return sign;
                }
            }
        } catch (IllegalArgumentException _) {
            throw ERROR_INVALID_SIGN_TYPE.create(string);
        }
        return null;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (final Material sign : SIGNS) {
            String name = sign.name();

            if (name.startsWith(builder.getRemaining().toUpperCase(Locale.ROOT))) {
                SIGNS.stream()
                        .filter(s -> s.name().startsWith(name))
                        .forEach(s -> builder.suggest(s.name().toLowerCase(Locale.ROOT)));
            }
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
