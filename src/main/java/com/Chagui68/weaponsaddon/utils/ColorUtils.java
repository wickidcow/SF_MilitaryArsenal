package com.Chagui68.weaponsaddon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

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

    private ColorUtils() {
    }

    /**
     * Translates legacy ampersand and bare #RRGGBB color codes into an Adventure component.
     */
    public static Component component(String message) {
        if (message == null) {
            return Component.empty();
        }

        return AMPERSAND.deserialize(normalizeHex(message));
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
