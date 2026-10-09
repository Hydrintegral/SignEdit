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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

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

                                    checkCommandPredicates(player, sign, "signedit.edit", true);

                                    sign.getTargetSide(player).line(lineIndex, Util.buildMiniMessage(player).deserialize(text));

                                    tryLoggingRemoval(player, sign);
                                    sign.update();
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

                                            checkCommandPredicates(player, sign, "signedit.edit", true);
                                            MiniMessage miniMessage = Util.buildMiniMessage(player);

                                            SignSide side = sign.getTargetSide(player);

                                            int i = 0;
                                            boolean success = false;
                                            for (Component line : side.lines()) {
                                                String raw = miniMessage.serialize(line);
                                                if (raw.contains(text)) {
                                                    success = true;
                                                }

                                                side.line(i, miniMessage.deserialize(raw.replace(text, with)));

                                                i++;
                                            }

                                            if (!success) {
                                                player.sendRichMessage("<red>Nothing found to replace!");

                                                return Command.SINGLE_SUCCESS;
                                            }

                                            tryLoggingRemoval(player, sign);
                                            sign.update();
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

                                    checkCommandPredicates(player, sign, "signedit.dye", true);

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
                            checkCommandPredicates(player, sign, "signedit.glow", true);

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

                            checkCommandPredicates(player, sign, "signedit.wax", true);

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
                .then(Commands.literal("clickevent")
                        .then(Commands.literal("set")
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            Player player = context.getSource().getPlayerOrThrow();
                                            String command = context.getArgument("command", String.class);

                                            Sign sign = getTargetedSignOrThrow(player);

                                            checkCommandPredicates(player, sign, "signedit.clickevent.set", true);

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

                                    checkCommandPredicates(player, sign, "signedit.clickevent.clear", true);

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

                                    checkCommandPredicates(player, sign, "signedit.type", true);

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

                            checkCommandPredicates(player, sign, "signedit.print", false);

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

                            for (Component line : side.lines()) {
                                // Skip empty lines
                                if (line.equals(Component.empty())) { continue; }

                                component.appendNewline()
                                        .resetStyle()
                                        .append(
                                                line.clickEvent(ClickEvent.copyToClipboard(MINI_MESSAGE.serialize(Util.clearClickEvents(line))))
                                                .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                                        );
                            }

                            component.appendNewline().append(footer);

                            player.sendMessage(component);

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
