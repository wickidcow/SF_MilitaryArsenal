package com.Chagui68.weaponsaddon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public static final String BLACK = translate("&0");
    public static final String DARK_BLUE = translate("&1");
    public static final String DARK_GREEN = translate("&2");
    public static final String DARK_AQUA = translate("&3");
    public static final String DARK_RED = translate("&4");
    public static final String DARK_PURPLE = translate("&5");
    public static final String GOLD = translate("&6");
    public static final String GRAY = translate("&7");
    public static final String DARK_GRAY = translate("&8");
    public static final String BLUE = translate("&9");
    public static final String GREEN = translate("&a");
    public static final String AQUA = translate("&b");
    public static final String RED = translate("&c");
    public static final String LIGHT_PURPLE = translate("&d");
    public static final String YELLOW = translate("&e");
    public static final String WHITE = translate("&f");
    public static final String RESET = translate("&r");

    private ColorUtils() {
    }

    /**
     * Translates legacy ampersand and bare #RRGGBB color codes into an Adventure component.
     */
    public static Component component(String message) {
        if (message == null) {
            return Component.empty();
        }

        return message.indexOf('§') >= 0
                ? SECTION.deserialize(message)
                : AMPERSAND.deserialize(normalizeHex(message));
    }

    /**
     * Retains the historical section-code String output for APIs/config fields that still
     * consume legacy strings, without using Bukkit's deprecated ChatColor API.
     */
    public static String translate(String message) {
        if (message == null) {
            return null;
        }

        return SECTION.serialize(component(message));
    }

    public static List<String> translateList(List<String> list) {
        if (list == null) {
            return null;
        }

        List<String> translated = new ArrayList<>(list.size());
        for (String line : list) {
            translated.add(translate(line));
        }
        return translated;
    }

    public static String strip(String message) {
        if (message == null) {
            return null;
        }
        return PLAIN.serialize(SECTION.deserialize(message));
    }

    public static List<Component> componentList(List<String> list) {
        if (list == null) {
            return null;
        }

        List<Component> translated = new ArrayList<>(list.size());
        for (String line : list) {
            translated.add(component(line));
        }
        return translated;
    }

    private static String normalizeHex(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
