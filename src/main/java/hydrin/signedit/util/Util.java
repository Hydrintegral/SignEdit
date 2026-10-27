package hydrin.signedit.util;

import com.destroystokyo.paper.profile.ProfileProperty;
import hydrin.signedit.MineSkinFetcher;
import hydrin.signedit.MineSkinTag;
import hydrin.signedit.Permissions;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.CommandSender;

public class Util {
    public static BlockData translateRotation(BlockData oldData, BlockData newData) {
        if (oldData instanceof Rotatable rot && newData instanceof Directional dir) {
            if (rot.getRotation().isCartesian()) {
                dir.setFacing(rot.getRotation());

                return dir;
            }

            switch (rot.getRotation()) {
                case BlockFace.NORTH_WEST, BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH_NORTH_EAST -> dir.setFacing(BlockFace.NORTH);
                case BlockFace.NORTH_EAST, BlockFace.EAST_NORTH_EAST, BlockFace.EAST_SOUTH_EAST -> dir.setFacing(BlockFace.EAST);
                case BlockFace.SOUTH_EAST, BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH_SOUTH_WEST -> dir.setFacing(BlockFace.SOUTH);
                case BlockFace.SOUTH_WEST, BlockFace.WEST_SOUTH_WEST, BlockFace.WEST_NORTH_WEST -> dir.setFacing(BlockFace.WEST);
                default -> {}
            }

            return dir;
        }

        if (oldData instanceof Directional dir && newData instanceof Rotatable rot) {
            rot.setRotation(dir.getFacing());

            return rot;
        }

        if (oldData instanceof Rotatable oldRot && newData instanceof Rotatable newRot) {
            newRot.setRotation(oldRot.getRotation());

            return newRot;
        }

        if (oldData instanceof Directional oldDir && newData instanceof Directional newDir) {
            newDir.setFacing(oldDir.getFacing());

            return newDir;
        }

        // Failsafe
        return newData;
    }

    public static ClickEvent<?> getFirstClickEvent(Component component) {
        if (component.clickEvent() != null) {
            return component.clickEvent();
        }

        for (Component child : component.children()) {
            if (getFirstClickEvent(child) != null) {
                return getFirstClickEvent(child);
            }
        }

        return null;
    }

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

        if (sender.hasPermission(Permissions.FORMATTING_MINESKIN)) {
            builder.resolver(MineSkinTag.RESOLVER);
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
