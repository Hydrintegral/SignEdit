package hydrin.signedit;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.Nullable;

public class MineSkinTag implements TagResolver {
    private static final String NAME = "mineskin";
    public static final TagResolver RESOLVER = new MineSkinTag();

    @Override
    public @Nullable Tag resolve(String name, ArgumentQueue args, Context context) throws ParsingException {
        String id = args.popOr("expected MineSkin Skin ID").value();
        boolean outerLayer = !args.hasNext() || Boolean.parseBoolean(args.pop().value());

        String value = MineSkinFetcher.getValue(id);

        ObjectContents contents = ObjectContents.playerHead()
                .skin(
                        ResolvableProfile.resolvableProfile()
                                .addProperty(new ProfileProperty("textures", value))
                                .build()
                )
                .hat(outerLayer)
                .build();

        return Tag.selfClosingInserting(Component.object(contents));
    }

    @Override
    public boolean has(String name) {
        return name.equals(NAME);
    }
}
