package hydrin.signedit.util;

import hydrin.signedit.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.command.CommandSender;

public class Util {
    public static Component clearClickEvents(Component component) {
        if (component.clickEvent() != null) {
            component = component.clickEvent(null);

            for (Component child : component.children()) {
                component = clearClickEvents(child);
            }
        }

        return component;
    }

    // Could probably be improved
    public static MiniMessage buildMiniMessage(CommandSender sender) {
        TagResolver.Builder builder = TagResolver.builder();

        builder.resolver(StandardTags.reset());
        builder.resolver(StandardTags.font());

        if (sender.hasPermission(Permissions.FORMATTING_COLOR)) {
            builder.resolver(StandardTags.color());
            builder.resolver(StandardTags.gradient());
            builder.resolver(StandardTags.transition());
            builder.resolver(StandardTags.pride());
        }

        if (sender.hasPermission(Permissions.FORMATTING_SHADOW)) {
            builder.resolver(StandardTags.shadowColor());
        }

        if (sender.hasPermission(Permissions.FORMATTING_STYLE)) {
            builder.resolver(StandardTags.decorations(TextDecoration.BOLD));
            builder.resolver(StandardTags.decorations(TextDecoration.STRIKETHROUGH));
            builder.resolver(StandardTags.decorations(TextDecoration.UNDERLINED));
            builder.resolver(StandardTags.decorations(TextDecoration.ITALIC));
        }

        if (sender.hasPermission(Permissions.FORMATTING_MAGIC)) {
            builder.resolver(StandardTags.decorations(TextDecoration.OBFUSCATED));
        }

        if (sender.hasPermission(Permissions.FORMATTING_HEADS)) {
            builder.resolver(StandardTags.sequentialHead());
        }

        if (sender.hasPermission(Permissions.FORMATTING_SPRITES)) {
            builder.resolver(StandardTags.sprite());
        }

        if (sender.hasPermission(Permissions.FORMATTING_MISC)) {
            builder.resolver(StandardTags.keybind());
            builder.resolver(StandardTags.translatable());
            builder.resolver(StandardTags.translatableFallback());
            builder.resolver(StandardTags.selector());
            builder.resolver(StandardTags.score());
            builder.resolver(StandardTags.nbt());
        }

        return MiniMessage.builder().tags(builder.build()).build();
    }
}
