package hydrin.signedit.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import hydrin.signedit.Permissions;
import hydrin.signedit.SignEdit;
import hydrin.signedit.util.LegacyToMiniMessageConverter;
import hydrin.signedit.util.Util;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.coreprotect.CoreProtectAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class SignEditCommand {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final CoreProtectAPI CORE_PROTECT_API = SignEdit.getCoreProtectAPI();

    private static final SimpleCommandExceptionType ERROR_NOT_A_SIGN = new SimpleCommandExceptionType(
            new LiteralMessage("You must be looking at a sign to run this command.")
    );
    private static final SimpleCommandExceptionType ERROR_NO_PERMISSION = new SimpleCommandExceptionType(
            new LiteralMessage("You do not have permission to run this command.")
    );
    private static final SimpleCommandExceptionType ERROR_NO_PLOT_PERMISSION = new SimpleCommandExceptionType(
            new LiteralMessage("You do not have permission to edit this sign.")
    );

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands.literal("signedit")
                .then(Commands.argument("line", IntegerArgumentType.integer(1, 4))
                        .suggests((_, builder) -> {
                            for (int i = 1; i <= 4; i++) {
                                builder.suggest(i);
                            }

                            return builder.buildFuture();
                        })
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrThrow();
                                    int lineIndex = context.getArgument("line", int.class) - 1;
                                    String text = LegacyToMiniMessageConverter.convert(
                                            context.getArgument("text", String.class)
                                    );

                                    Sign sign = getTargetedSignOrThrow(player);

                                    checkCommandPredicates(player, sign, Permissions.EDIT, true);

                                    Side frontOrBack = sign.getInteractableSideFor(player);
                                    SignSide side = sign.getSide(frontOrBack);
                                    ClickEvent<?> clickEvent = Util.getFirstClickEvent(side.line(lineIndex));

                                    side.line(lineIndex, Util.buildMiniMessage(player).deserialize(text));
                                    if (clickEvent != null) { side.line(lineIndex, side.line(lineIndex).clickEvent(clickEvent)); }

                                    tryLoggingRemoval(player, sign);
                                    sign.update();
                                    fireSignChange(player, sign, side, frontOrBack);
                                    tryLoggingPlacement(player, sign);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("replace")
                        .then(Commands.argument("text", StringArgumentType.string())
                                .then(Commands.argument("with", StringArgumentType.string())
                                        .executes(context -> {
                                            Player player = context.getSource().getPlayerOrThrow();
                                            String text = context.getArgument("text", String.class);
                                            String with = context.getArgument("with", String.class);

                                            Sign sign = getTargetedSignOrThrow(player);

                                            checkCommandPredicates(player, sign, Permissions.REPLACE, true);
                                            MiniMessage miniMessage = Util.buildMiniMessage(player);

                                            Side frontOrBack = sign.getInteractableSideFor(player);
                                            SignSide side = sign.getSide(frontOrBack);

                                            int i = 0;
                                            boolean success = false;
                                            for (Component line : side.lines()) {
                                                String raw = miniMessage.serialize(line);
                                                ClickEvent<?> clickEvent = Util.getFirstClickEvent(line);

                                                if (raw.contains(text)) {
                                                    success = true;
                                                } else {
                                                    i++;
                                                    continue;
                                                }

                                                side.line(i, miniMessage.deserialize(raw.replace(text, with)));

                                                if (clickEvent != null) { side.line(i, side.line(i).clickEvent(clickEvent)); }

                                                i++;
                                            }

                                            if (!success) {
                                                player.sendRichMessage("<red>Nothing found to replace!");

                                                return Command.SINGLE_SUCCESS;
                                            }

                                            tryLoggingRemoval(player, sign);
                                            sign.update();
                                            fireSignChange(player, sign, side, frontOrBack);
                                            tryLoggingPlacement(player, sign);

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("dye")
                        .then(Commands.argument("color", new DyeColorArgumentType())
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrThrow();
                                    DyeColor color = context.getArgument("color", DyeColor.class);

                                    Sign sign = getTargetedSignOrThrow(player);

                                    checkCommandPredicates(player, sign, Permissions.DYE, true);

                                    sign.getTargetSide(player).setColor(color);

                                    tryLoggingRemoval(player, sign);
                                    sign.update();
                                    tryLoggingPlacement(player, sign);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("glow")
                        .executes(context -> {
                            Player player = context.getSource().getPlayerOrThrow();

                            Sign sign = getTargetedSignOrThrow(player);
                            checkCommandPredicates(player, sign, Permissions.GLOW, true);

                            SignSide side = sign.getTargetSide(player);

                            side.setGlowingText(!side.isGlowingText());

                            tryLoggingRemoval(player, sign);
                            sign.update();
                            tryLoggingPlacement(player, sign);

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("wax")
                        .executes(context -> {
                            Player player = context.getSource().getPlayerOrThrow();

                            Sign sign = getTargetedSignOrThrow(player);

                            checkCommandPredicates(player, sign, Permissions.WAX, true);

                            sign.setWaxed(!sign.isWaxed());

                            tryLoggingRemoval(player, sign);
                            sign.update();
                            tryLoggingPlacement(player, sign);

                            player.sendRichMessage(
                                    "<gray>Toggled wax "
                                            + (!sign.isWaxed() ? "<red>off" : "<green>on")
                                            + "<gray> for sign [<aqua>" + getSignXyz(sign) + "<gray>]"
                            );

                            return Command.SINGLE_SUCCESS;
                        })
                )
                // This subcommand uses some delicate logic that assumes the signs don't already have a clickevent added
                // on a different line. Maybe this should be different, but it seems unlikely to cause problems?
                .then(Commands.literal("clickevent")
                        .then(Commands.literal("set")
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            Player player = context.getSource().getPlayerOrThrow();
                                            String command = context.getArgument("command", String.class);

                                            Sign sign = getTargetedSignOrThrow(player);

                                            checkCommandPredicates(player, sign, Permissions.CLICKEVENT, true);

                                            SignSide side = sign.getTargetSide(player);

                                            side.line(0, Util.clearClickEvents(side.line(0)));
                                            side.line(0, side.line(0).clickEvent(ClickEvent.runCommand(command)));

                                            tryLoggingRemoval(player, sign);
                                            sign.update();
                                            tryLoggingPlacement(player, sign);

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("clear")
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrThrow();

                                    Sign sign = getTargetedSignOrThrow(player);

                                    checkCommandPredicates(player, sign, Permissions.CLICKEVENT, true);

                                    SignSide side = sign.getTargetSide(player);

                                    side.line(0, Util.clearClickEvents(side.line(0)));

                                    tryLoggingRemoval(player, sign);
                                    sign.update();
                                    tryLoggingPlacement(player, sign);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("type")
                        .then(Commands.argument("type", new SignTypeArgumentType())
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrThrow();
                                    Material type = context.getArgument("type", Material.class);

                                    Sign sign = getTargetedSignOrThrow(player);

                                    checkCommandPredicates(player, sign, Permissions.TYPE, true);

                                    if (sign.getType().equals(type)) {
                                        player.sendRichMessage("<red>That sign is already that type!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    tryLoggingRemoval(player, sign);

                                    SignSide front = sign.getSide(Side.FRONT);
                                    SignSide back = sign.getSide(Side.BACK);

                                    // `sign` breaks if you use #setType() on it, hence this workaround
                                    Block block = sign.getBlock();
                                    block.setType(type, false);

                                    Sign newSign = (Sign) block.getState();
                                    SignSide newFront = newSign.getSide(Side.FRONT);
                                    SignSide newBack = newSign.getSide(Side.BACK);

                                    for (int i = 0; i <= 3; i++) {
                                        newFront.line(i, front.line(i));
                                        newBack.line(i, back.line(i));
                                    }
                                    newFront.setColor(front.getColor());;
                                    newBack.setColor(back.getColor());

                                    newFront.setGlowingText(front.isGlowingText());
                                    newBack.setGlowingText(back.isGlowingText());

                                    newSign.setWaxed(sign.isWaxed());

                                    newSign.setBlockData(Util.translateRotation(sign.getBlockData(), newSign.getBlockData()));

                                    Waterlogged wat = (Waterlogged) newSign.getBlockData();
                                    wat.setWaterlogged(((Waterlogged) sign.getBlockData()).isWaterlogged());
                                    newSign.setBlockData(wat);

                                    newSign.update(true, false);

                                    tryLoggingPlacement(player, newSign);

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("print")
                        .executes(context -> {
                            Player player = context.getSource().getPlayerOrThrow();

                            Sign sign = getTargetedSignOrThrow(player);

                            checkCommandPredicates(player, sign, Permissions.PRINT, false);

                            SignSide side = sign.getTargetSide(player);

                            // (The bold zero-width non-joiner adds 1 texel of size, which makes the header and footer
                            // the same width)
                            // This looks nicer in game, I promise
                            Component header = MINI_MESSAGE.deserialize(
                                    "<gray><st>          <reset><aqua> Click a line to copy: <gray><st>          "
                            );
                            Component footer = MINI_MESSAGE.deserialize(
                                    "<gray><st>                                              <bold>\u200C"
                            );

                            TextComponent.Builder component = Component.text().append(header);

                            boolean success = false;

                            for (Component line : side.lines()) {
                                String raw = MINI_MESSAGE.serialize(Util.clearClickEvents(line));

                                if (line.equals(Component.empty()) || raw.isBlank()) {
                                    continue;
                                }

                                component.appendNewline()
                                        .resetStyle()
                                        .append(
                                                line.clickEvent(ClickEvent.copyToClipboard(raw))
                                                .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                                        );
                                success = true;
                            }

                            if (!success) {
                                player.sendRichMessage("<red>Nothing found to print!");

                                return Command.SINGLE_SUCCESS;
                            }

                            component.appendNewline().resetStyle().append(footer);

                            player.sendMessage(component);

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("help")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();

                            if (!(sender.hasPermission(Permissions.HELP))) {
                                throw ERROR_NO_PERMISSION.create();
                            }

                            Component header = MINI_MESSAGE.deserialize(
                                    "<gray><st>              <bold>\u200C<reset><aqua> SignEdit Help <reset><gray><st>              <bold>\u200C"
                            );
                            Component footer = MINI_MESSAGE.deserialize(
                                    "<gray><st>                                              <bold>\u200C"
                            );
                            Component disclaimer = MINI_MESSAGE.deserialize(
                                    "<white>Commands that modify signs will target the sign face that you're looking at:"
                            );

                            TextComponent.Builder component = Component.text()
                                    .append(header)
                                    .appendNewline()
                                    .resetStyle()
                                    .append(disclaimer)
                                    .appendNewline();

                            for (hydrin.signedit.commands.Command command : hydrin.signedit.commands.Command.values()) {
                                component.appendNewline()
                                        .resetStyle()
                                        .append(Component.text("/").color(NamedTextColor.GRAY))
                                        .append(Component.text("signedit ").color(NamedTextColor.AQUA));

                                if (command.getUsage() == null) {
                                    component.append(Component.text(command.getName()).color(NamedTextColor.DARK_AQUA));
                                } else {
                                    component.append(Component.text(command.getUsage()).color(NamedTextColor.DARK_AQUA));
                                }

                                component.append(Component.text(" | ").color(NamedTextColor.GRAY))
                                        .append(Component.text(command.getDescription()).color(NamedTextColor.WHITE));
                            }

                            component.appendNewline().resetStyle().append(footer);

                            sender.sendMessage(component);

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
    }

    private static void checkCommandPredicates(
            Player player,
            Sign sign,
            String permission,
            boolean checkPlotPermissions
    ) throws CommandSyntaxException {
        if (!player.hasPermission(permission)) {
            throw ERROR_NO_PERMISSION.create();
        }

        if (SignEdit.hasPlotSquared() && checkPlotPermissions && !canEditSign(player, sign)) {
            throw ERROR_NO_PLOT_PERMISSION.create();
        }
    }

    private static Sign getTargetedSignOrThrow(Player player) throws CommandSyntaxException {
        Block targetedBlock = player.getTargetBlockExact(5);

        if (targetedBlock == null || !(targetedBlock.getState() instanceof Sign)) {
            throw ERROR_NOT_A_SIGN.create();
        }

        if (targetedBlock.getState() instanceof Sign sign) {
            return sign;
        }

        return null;
    }

    private static boolean canEditSign(Player sender, Sign sign) {
        PlotPlayer<Player> player = PlotPlayer.from(sender);

        Location loc = sign.getLocation();

        var location = com.plotsquared.core.location.Location.at(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );

        if (location.isPlotRoad()) {
            return player.hasPermission(Permission.PERMISSION_ADMIN_BUILD_ROAD);
        }

        if (location.isPlotArea()) {
            if (location.getPlot() == null) {
                return false;
            }

            return location.getPlot().isAdded(player.getUUID()) || player.hasPermission(Permission.PERMISSION_ADMIN_BUILD_OTHER);
        }

        return true;
    }

    private static void tryLoggingRemoval(Player player, Sign sign) {
        if (CORE_PROTECT_API != null) {
            CORE_PROTECT_API.logRemoval(player.getName(), sign);
        }
    }

    // Enables coreprotect to roll back sign edits.
    private static void fireSignChange(Player player, Sign sign, SignSide signSide, Side side) {
        SignChangeEvent ev = new SignChangeEvent(
            sign.getBlock(),
            player,
            signSide.lines(), side);
        ev.callEvent();
    }

    private static void tryLoggingPlacement(Player player, Sign sign) {
        if (CORE_PROTECT_API != null) {
            CORE_PROTECT_API.logPlacement(player.getName(), sign);
        }
    }

    private static String getSignXyz(Sign sign) {
        Location loc = sign.getLocation();

        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}
