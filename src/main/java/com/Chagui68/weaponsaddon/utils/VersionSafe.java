package com.Chagui68.weaponsaddon.utils;

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
        try {
            Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
            if (ench != null) {
                return ench;
            }
        } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
            // Retain the original fallback behavior for older Slimefun-compatible environments.
        }

        String name = null;
        switch (key.toLowerCase(Locale.ROOT)) {
            case "sharpness":
                name = "DAMAGE_ALL";
                break;
            case "power":
                name = "ARROW_DAMAGE";
                break;
            case "punch":
                name = "ARROW_KNOCKBACK";
                break;
            case "protection":
                name = "PROTECTION_ENVIRONMENTAL";
                break;
            case "projectile_protection":
                name = "PROTECTION_PROJECTILE";
                break;
            case "blast_protection":
                name = "PROTECTION_EXPLOSIONS";
                break;
            case "fire_protection":
                name = "PROTECTION_FIRE";
                break;
            case "respiration":
                name = "OXYGEN";
                break;
            case "looting":
                name = "LOOT_BONUS_MOBS";
                break;
            case "unbreaking":
                name = "DURABILITY";
                break;
            case "efficiency":
                name = "DIG_SPEED";
                break;
            case "smite":
                name = "DAMAGE_UNDEAD";
                break;
            case "bane_of_arthropods":
                name = "DAMAGE_ARTHROPODS";
                break;
            default:
                break;
        }

        if (name != null) {
            try {
                return Enchantment.getByName(name);
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
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
    @SuppressWarnings("deprecation")
    public static PotionEffectType getPotionEffectType(String name) {
        try {
            PotionEffectType type = PotionEffectType.getByName(name);
            if (type != null)
                return type;

            if (name.equalsIgnoreCase("SLOWNESS"))
                return PotionEffectType.getByName("SLOW");
            if (name.equalsIgnoreCase("MINING_FATIGUE"))
                return PotionEffectType.getByName("SLOW_DIGGING");
            if (name.equalsIgnoreCase("HASTE"))
                return PotionEffectType.getByName("FAST_DIGGING");
            if (name.equalsIgnoreCase("STRENGTH"))
                return PotionEffectType.getByName("INCREASE_DAMAGE");
            if (name.equalsIgnoreCase("INSTANT_HEALTH"))
                return PotionEffectType.getByName("HEAL");
            if (name.equalsIgnoreCase("INSTANT_DAMAGE"))
                return PotionEffectType.getByName("HARM");
            if (name.equalsIgnoreCase("NAUSEA"))
                return PotionEffectType.getByName("CONFUSION");
            if (name.equalsIgnoreCase("RESISTANCE"))
                return PotionEffectType.getByName("DAMAGE_RESISTANCE");
            if (name.equalsIgnoreCase("SPEED"))
                return PotionEffectType.getByName("SPEED");
            if (name.equalsIgnoreCase("FIRE_RESISTANCE"))
                return PotionEffectType.getByName("FIRE_RESISTANCE");
            if (name.equalsIgnoreCase("JUMP_BOOST"))
                return PotionEffectType.getByName("JUMP");
            if (name.equalsIgnoreCase("NIGHT_VISION"))
                return PotionEffectType.getByName("NIGHT_VISION");
            if (name.equalsIgnoreCase("ABSORPTION"))
                return PotionEffectType.getByName("ABSORPTION");
            if (name.equalsIgnoreCase("SATURATION"))
                return PotionEffectType.getByName("SATURATION");
            if (name.equalsIgnoreCase("LEVITATION"))
                return PotionEffectType.getByName("LEVITATION");
            if (name.equalsIgnoreCase("GLOWING"))
                return PotionEffectType.getByName("GLOWING");
            if (name.equalsIgnoreCase("WITHER"))
                return PotionEffectType.getByName("WITHER");
            if (name.equalsIgnoreCase("HUNGER"))
                return PotionEffectType.getByName("HUNGER");
            if (name.equalsIgnoreCase("WEAKNESS"))
                return PotionEffectType.getByName("WEAKNESS");
            if (name.equalsIgnoreCase("DARKNESS"))
                return PotionEffectType.getByName("DARKNESS");

            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
