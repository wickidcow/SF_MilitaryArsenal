package com.Chagui68.weaponsaddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class VersionSafe {

    /**
     * Resolves legacy enum-style attribute names through Paper's modern attribute registry.
     * Paper 26.2 removed the GENERIC_ prefix from the Java constants and schedules the old
     * values()/name() compatibility surface for removal, so registry keys are the stable path.
     */
    public static Attribute getAttribute(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        try {
            String key = name.toLowerCase(Locale.ROOT);
            if (key.startsWith("generic_")) {
                key = key.substring("generic_".length());
            }
            if (key.equals("horse_jump_strength")) {
                key = "jump_strength";
            }
            return Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Safely sets the base value of an attribute on an entity.
     * Does nothing if the attribute does not exist or the entity doesn't have it.
     */
    public static void setAttributeBaseValue(LivingEntity entity, String attributeName, double value) {
        Attribute attr = getAttribute(attributeName);
        if (attr != null && entity.getAttribute(attr) != null) {
            entity.getAttribute(attr).setBaseValue(value);
        }
    }

    /**
     * Safely gets an Enchantment by a key that is valid across versions.
     */
    public static Enchantment getEnchantment(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        String normalized = switch (key.toLowerCase(Locale.ROOT)) {
            case "damage_all" -> "sharpness";
            case "arrow_damage" -> "power";
            case "arrow_knockback" -> "punch";
            case "protection_environmental" -> "protection";
            case "protection_projectile" -> "projectile_protection";
            case "protection_explosions" -> "blast_protection";
            case "protection_fire" -> "fire_protection";
            case "oxygen" -> "respiration";
            case "loot_bonus_mobs" -> "looting";
            case "durability" -> "unbreaking";
            case "dig_speed" -> "efficiency";
            case "damage_undead" -> "smite";
            case "damage_arthropods" -> "bane_of_arthropods";
            default -> key.toLowerCase(Locale.ROOT);
        };

        try {
            return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(normalized));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Safely gets a Particle by name and retains the upstream legacy aliases.
     */
    public static Particle getParticle(String name) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e1) {
            try {
                if (name.equals("DUST"))
                    return Particle.valueOf("REDSTONE");
                if (name.equals("HAPPY_VILLAGER"))
                    return Particle.valueOf("VILLAGER_HAPPY");
                if (name.equals("ANGRY_VILLAGER"))
                    return Particle.valueOf("VILLAGER_ANGRY");
                if (name.equals("EXPLOSION"))
                    return Particle.valueOf("EXPLOSION_NORMAL");
                if (name.equals("LARGE_SMOKE"))
                    return Particle.valueOf("SMOKE_LARGE");
                if (name.equals("EXPLOSION_EMITTER"))
                    return Particle.valueOf("HUGE_EXPLOSION");
                if (name.equals("WITCH"))
                    return Particle.valueOf("SPELL_WITCH");
            } catch (IllegalArgumentException ignored) {
                // Ignore unavailable aliases.
            }
            return null;
        }
    }

    /**
     * Resolves Bukkit Sound constants without using the OldEnum valueOf compatibility method,
     * which Paper 26.2 schedules for removal.
     */
    public static Sound getSound(String name) {
        Sound sound = getSoundConstant(name);
        if (sound != null) {
            return sound;
        }

        if (name.contains("FIREWORK_ROCKET")) {
            sound = getSoundConstant(name.replace("FIREWORK_ROCKET", "FIREWORK"));
            if (sound != null) {
                return sound;
            }
        }

        if (name.equals("BLOCK_NOTE_BLOCK_HAT")) {
            return getSoundConstant("BLOCK_NOTE_HAT");
        }

        return null;
    }

    private static Sound getSoundConstant(String name) {
        try {
            Object value = Sound.class.getField(name).get(null);
            return value instanceof Sound sound ? sound : null;
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }

    /**
     * Safely gets a PotionEffectType by name.
     */
    public static PotionEffectType getPotionEffectType(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String normalized = switch (name.toUpperCase(Locale.ROOT)) {
            case "SLOW" -> "slowness";
            case "SLOW_DIGGING" -> "mining_fatigue";
            case "FAST_DIGGING" -> "haste";
            case "INCREASE_DAMAGE" -> "strength";
            case "HEAL" -> "instant_health";
            case "HARM" -> "instant_damage";
            case "CONFUSION" -> "nausea";
            case "DAMAGE_RESISTANCE" -> "resistance";
            case "JUMP" -> "jump_boost";
            default -> name.toLowerCase(Locale.ROOT);
        };

        try {
            return Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(normalized));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Detects the Minecraft Java version currently running on the server.
     * Returns a plain version string such as "1.21.11" or "unknown" when it cannot
     * be determined.
     */
    public static String getMinecraftVersion() {
        try {
            String version = Bukkit.getMinecraftVersion();
            if (version != null && !version.isBlank()) {
                return version;
            }
        } catch (Throwable ignored) {
            // Older Bukkit/Paper builds without Bukkit#getMinecraftVersion().
        }

        String bukkit = Bukkit.getBukkitVersion();
        if (bukkit == null || bukkit.isBlank()) {
            return "unknown";
        }
        int dash = bukkit.indexOf('-');
        String version = dash > 0 ? bukkit.substring(0, dash) : bukkit;
        return version.isBlank() ? "unknown" : version;
    }

    /**
     * Checks whether the server is running the exact given Minecraft Java version,
     * e.g. isMinecraft("1.21.11").
     */
    public static boolean isMinecraft(String version) {
        return getMinecraftVersion().equalsIgnoreCase(version);
    }

    /**
     * Checks whether the server is running the given Minecraft Java version or a
     * newer one, e.g. isMinecraftAtLeast("1.21.11").
     */
    public static boolean isMinecraftAtLeast(String version) {
        int[] current = parseVersion(getMinecraftVersion());
        int[] required = parseVersion(version);
        int max = Math.max(current.length, required.length);
        for (int i = 0; i < max; i++) {
            int a = i < current.length ? current[i] : 0;
            int b = i < required.length ? required[i] : 0;
            if (a != b) {
                return a > b;
            }
        }
        return true;
    }

    private static int[] parseVersion(String version) {
        if (version == null) {
            return new int[0];
        }
        String[] parts = version.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            StringBuilder digits = new StringBuilder();
            for (char c : parts[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    digits.append(c);
                } else {
                    break;
                }
            }
            result[i] = digits.length() == 0 ? 0 : Integer.parseInt(digits.toString());
        }
        return result;
    }
}
