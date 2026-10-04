package hydrin.signEdit;

import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;

import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class LegacyToMiniMessageConverter {
    private static final Set<Character> FORMATTING_CODES = CharacterAndFormat.defaults().stream()
            .map(CharacterAndFormat::character)
            .collect(Collectors.toSet());

    public static String convert(String string) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            String substring;
            int remaining = string.length() - i + 1;

            if (remaining <= 8) {
                substring = string.substring(i, string.length() - 1);
            } else {
                // The second argument of String#substring is exclusive
                substring = string.substring(i, i + 8);
            }

            switch (getFormattingType(substring)) {
                case INVALID -> builder.append(string.charAt(i));
                case VALID -> {
                    i++;
                    builder.append(translate(string.charAt(i)));
                }
                case HEX_COLOR -> {
                    i += 2;
                    builder.append(translateHex(string.substring(i, i + 6)));

                    // Skip past the hex code since it doesn't need to be checked at this point
                    i += 5;
                }
                case ESCAPED -> {
                    i++;
                    builder.append(string, i, i + 2);

                    i++;
                }
            }
        }

        return builder.toString();
    }

    private static FormattingType getFormattingType(String substring) {
        if (substring.length() > 1) {
            if ((substring.charAt(0) == '\\' || substring.charAt(0) == '&') && substring.charAt(1) == '&') {
                return FormattingType.ESCAPED;
            }
            if (substring.charAt(0) == '&') {
                if (FORMATTING_CODES.contains(substring.toLowerCase(Locale.ROOT).charAt(1))) {
                    return FormattingType.VALID;
                }
                if ((substring.charAt(1) == '#') && substring.length() == 8) {
                    // Iterate through indexes 2 to 7
                    for (int i = 2; i <= 7; i++) {
                        if (!(HexFormat.isHexDigit(substring.charAt(i)))) {
                            return FormattingType.INVALID;
                        }
                    }

                    return FormattingType.HEX_COLOR;
                }
            }
        }

        return FormattingType.INVALID;
    }

    private static String translate(char c) {
        switch (c) {
            // Colors
            case '0' -> { return "<black>"; }
            case '1' -> { return "<dark_blue>"; }
            case '2' -> { return "<dark_green>"; }
            case '3' -> { return "<dark_aqua>"; }
            case '4' -> { return "<dark_red>"; }
            case '5' -> { return "<dark_purple>"; }
            case '6' -> { return "<gold>"; }
            case '7' -> { return "<gray>"; }
            case '8' -> { return "<dark_gray>"; }
            case '9' -> { return "<blue>"; }
            case 'a' -> { return "<green>"; }
            case 'b' -> { return "<aqua>"; }
            case 'c' -> { return "<red>"; }
            case 'd' -> { return "<light_purple>"; }
            case 'e' -> { return "<yellow>"; }
            case 'f' -> { return "<white>"; }

            // Decoration
            case 'k' -> { return "<obfuscated>"; }
            case 'l' -> { return "<bold>"; }
            case 'm' -> { return "<strike>"; }
            case 'n' -> { return "<underline>"; }
            case 'o' -> { return "<italic>"; }

            // Reset
            case 'r' -> { return "<reset>"; }
        }

        return null;
    }

    private static String translateHex(String substring) {
        return "<#" + substring + ">";
    }

    private enum FormattingType {
        VALID,
        INVALID,
        HEX_COLOR,
        ESCAPED
    }
}
