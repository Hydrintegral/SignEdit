package hydrin.signedit;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

@SuppressWarnings("UnstableApiUsage")
public class MineSkinTag {
    static final String MINESKIN = "mineskin";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(
            MINESKIN,
            MineSkinTag::create,
            MineSkinTag::claimComponent
    );

    private MineSkinTag() {
    }

    static Tag create(ArgumentQueue args, Context context) throws ParsingException {
        String id = args.popOr("expected MineSkin Skin ID").value();
        boolean outerLayer = !args.hasNext() || Boolean.parseBoolean(args.pop().value());

        String value = MineSkinFetcher.getValue(id);

        ObjectContents contents = ObjectContents.playerHead()
                .skin(
                        ResolvableProfile.resolvableProfile()
                                // A custom MineSkin ProfileProperty is added so the serializer can identify a head
                                // sourced from mineskin. Not ideal, but I can't think of anything else
                                .addProperty(new ProfileProperty("textures", value))
                                .addProperty(new ProfileProperty(MINESKIN, id))
                                .build()
                )
                .hat(outerLayer)
                .build();

        return Tag.selfClosingInserting(Component.object(contents));
    }

    static Emitable claimComponent(final Component input) {
        if (!(input instanceof ObjectComponent objectComponent)) {
            return null;
        }

        ObjectContents contents = objectComponent.contents();
        if (!(contents instanceof PlayerHeadObjectContents playerHead)) {
            return null;
        }

        String id = playerHead.profileProperties().stream()
                .filter(property -> property.name().equals(MINESKIN))
                .map(PlayerHeadObjectContents.ProfileProperty::value)
                .findFirst()
                .orElse(null);

        if (id == null) {
            return null;
        }

        return emit -> {
            emit.selfClosingTag(MINESKIN).argument(id);

            if (playerHead.hat() != PlayerHeadObjectContents.DEFAULT_HAT) {
                emit.argument(Boolean.toString(playerHead.hat()));
            }
        };
    }
}
