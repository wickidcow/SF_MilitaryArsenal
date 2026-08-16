package com.Chagui68.weaponsaddon.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main-thread session lock for custom virtual machine inventories.
 *
 * The legacy machine GUIs deserialize BlockStorage contents into a temporary
 * Bukkit inventory. Without a per-block lock, two players can open the same
 * machine and receive independent copies of the same stored items.
 */
public final class MachineSessionGuard {

    private static final Map<MachineKey, UUID> LOCKS = new HashMap<>();

    private MachineSessionGuard() {
    }

    public static boolean acquire(Location location, Player player) {
        MachineKey key = MachineKey.of(location);
        UUID owner = LOCKS.get(key);

        if (owner != null && !owner.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "This machine is already being used by another player.");
            return false;
        }

        LOCKS.put(key, player.getUniqueId());
        return true;
    }

    public static boolean isLocked(Location location) {
        return LOCKS.containsKey(MachineKey.of(location));
    }

    public static boolean isLockedBy(Location location, UUID playerId) {
        return playerId.equals(LOCKS.get(MachineKey.of(location)));
    }

    public static void release(Location location, UUID playerId) {
        LOCKS.remove(MachineKey.of(location), playerId);
    }

    private record MachineKey(UUID worldId, int x, int y, int z) {
        private static MachineKey of(Location location) {
            if (location.getWorld() == null) {
                throw new IllegalArgumentException("Machine location must have a world");
            }
            return new MachineKey(
                    location.getWorld().getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ());
        }
    }
}
