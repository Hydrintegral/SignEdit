package hydrin.signedit.util;

import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;

import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class    LegacyToMiniMessageConverter {
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
                case NO_FORMAT -> builder.append(string.charAt(i));
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
                            return FormattingType.NO_FORMAT;
                        }
                    }

                    return FormattingType.HEX_COLOR;
                }
            }
        }

        return FormattingType.NO_FORMAT;
    }

    private static String translate(char c) {
        switch (c) {
            // Colors
            // Legacy formatting resets all decoration styles when using colors
            case '0' -> { return "<reset><black>"; }
            case '1' -> { return "<reset><dark_blue>"; }
            case '2' -> { return "<reset><dark_green>"; }
            case '3' -> { return "<reset><dark_aqua>"; }
            case '4' -> { return "<reset><dark_red>"; }
            case '5' -> { return "<reset><dark_purple>"; }
            case '6' -> { return "<reset><gold>"; }
            case '7' -> { return "<reset><gray>"; }
            case '8' -> { return "<reset><dark_gray>"; }
            case '9' -> { return "<reset><blue>"; }
            case 'a' -> { return "<reset><green>"; }
            case 'b' -> { return "<reset><aqua>"; }
            case 'c' -> { return "<reset><red>"; }
            case 'd' -> { return "<reset><light_purple>"; }
            case 'e' -> { return "<reset><yellow>"; }
            case 'f' -> { return "<reset><white>"; }

            // Decoration
            case 'k' -> { return "<obfuscated>"; }
            case 'l' -> { return "<bold>"; }
            case 'm' -> { return "<strikethrough>"; }
            case 'n' -> { return "<underlined>"; }
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
        HEX_COLOR,
        ESCAPED,
        NO_FORMAT
    }
}
