package com.Chagui68.weaponsaddon.protection;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Central protection gate for destructive Military Arsenal actions.
 *
 * Towny is optional at compile time. When it is installed, checks are made
 * through Towny's public PlayerCacheUtil/TownyAPI surface using reflection so
 * this addon does not impose a hard dependency. Any Towny API lookup failure
 * fails closed: a destructive action is denied rather than allowed blindly.
 */
public final class ProtectionService {

    private static final String TOWNY_PLUGIN = "Towny";
    private static volatile boolean warnedTownyFailure;

    private ProtectionService() {
    }

    public static boolean isTownyPresent() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(TOWNY_PLUGIN);
        return plugin != null && plugin.isEnabled();
    }

    public static boolean canBuild(Player player, Location location) {
        return checkTownyPermission(player, location, "BUILD");
    }

    public static boolean canDestroy(Player player, Location location) {
        return checkTownyPermission(player, location, "DESTROY");
    }

    public static boolean canSwitch(Player player, Location location) {
        return checkTownyPermission(player, location, "SWITCH");
    }

    public static boolean canUse(Player player, Location location) {
        return checkTownyPermission(player, location, "ITEM_USE");
    }

    public static boolean canModify(Player player, Location location) {
        return canBuild(player, location) && canDestroy(player, location);
    }

    /**
     * Remote bombardments are destructive and therefore require DESTROY rights
     * at the target. This deliberately fails closed in Towny claims.
     */
    public static boolean canBombard(Player player, Location location) {
        return canDestroy(player, location);
    }

    /**
     * Player-vs-player weapon damage follows Towny's PVP state. Non-player
     * targets are allowed here; normal Bukkit damage events still remain
     * available to other protection plugins to cancel.
     */
    public static boolean canDamage(Player attacker, LivingEntity target) {
        if (!(target instanceof Player)) {
            return true;
        }
        if (!isTownyPresent()) {
            return true;
        }

        try {
            Class<?> apiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method isPvp = apiClass.getMethod("isPVP", Location.class);
            boolean attackerPvp = (boolean) isPvp.invoke(api, attacker.getLocation());
            boolean targetPvp = (boolean) isPvp.invoke(api, target.getLocation());
            return attackerPvp && targetPvp;
        } catch (ReflectiveOperationException | LinkageError ex) {
            warnTownyFailure(ex);
            return false;
        }
    }

    /**
     * Used for automatic/ticker-driven world changes where there is no player
     * actor. In a Towny claim we do not mutate the world automatically.
     */
    public static boolean canAutomateWorldChange(Location location) {
        if (!isTownyPresent()) {
            return true;
        }

        try {
            Class<?> apiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method isWilderness = apiClass.getMethod("isWilderness", Location.class);
            return (boolean) isWilderness.invoke(api, location);
        } catch (ReflectiveOperationException | LinkageError ex) {
            warnTownyFailure(ex);
            return false;
        }
    }

    public static void deny(Player player, String action) {
        player.sendMessage("§cMilitary Arsenal blocked " + action + " because this location is protected.");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean checkTownyPermission(Player player, Location location, String actionName) {
        if (!isTownyPresent()) {
            return true;
        }
        if (player == null || location == null || location.getWorld() == null) {
            return false;
        }

        try {
            Class<?> actionTypeClass = Class.forName("com.palmergames.bukkit.towny.object.TownyPermission$ActionType");
            Object actionType = Enum.valueOf((Class<? extends Enum>) actionTypeClass.asSubclass(Enum.class), actionName);
            Class<?> cacheClass = Class.forName("com.palmergames.bukkit.towny.utils.PlayerCacheUtil");
            Method method = cacheClass.getMethod(
                    "getCachePermission",
                    Player.class,
                    Location.class,
                    Material.class,
                    actionTypeClass
            );
            Material material = location.getBlock().getType();
            return (boolean) method.invoke(null, player, location, material, actionType);
        } catch (ReflectiveOperationException | LinkageError ex) {
            warnTownyFailure(ex);
            return false;
        }
    }

    private static void warnTownyFailure(Throwable ex) {
        if (warnedTownyFailure) {
            return;
        }
        warnedTownyFailure = true;
        WeaponsAddon plugin = WeaponsAddon.getInstance();
        if (plugin != null) {
            plugin.getLogger().severe("Towny protection API check failed; Military Arsenal is failing closed: " + ex);
        }
    }
}
