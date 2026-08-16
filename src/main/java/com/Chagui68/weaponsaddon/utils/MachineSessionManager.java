package com.Chagui68.weaponsaddon.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Prevents copied virtual machine inventories from being opened by multiple
 * players at the same time or destroyed while a session is active.
 *
 * Also provides a second-line transaction guard for Military Arsenal's custom
 * inventories so shift-click and drag operations cannot bypass slot rules.
 */
public final class MachineSessionManager implements Listener {

    private static final Map<String, UUID> MACHINE_OWNERS = new HashMap<>();
    private static final Map<UUID, String> PLAYER_MACHINES = new HashMap<>();

    public static synchronized boolean tryAcquire(Player player, Location location) {
        String key = key(location);
        UUID playerId = player.getUniqueId();
        UUID owner = MACHINE_OWNERS.get(key);
        String currentMachine = PLAYER_MACHINES.get(playerId);

        if (owner != null && !owner.equals(playerId)) {
            return false;
        }

        if (currentMachine != null && !currentMachine.equals(key)) {
            return false;
        }

        MACHINE_OWNERS.put(key, playerId);
        PLAYER_MACHINES.put(playerId, key);
        return true;
    }

    public static synchronized void release(Player player) {
        release(player.getUniqueId());
    }

    public static synchronized void release(UUID playerId) {
        String key = PLAYER_MACHINES.remove(playerId);
        if (key != null) {
            MACHINE_OWNERS.remove(key, playerId);
        }
    }

    public static synchronized boolean isLocked(Location location) {
        return MACHINE_OWNERS.containsKey(key(location));
    }

    public static synchronized void clear() {
        MACHINE_OWNERS.clear();
        PLAYER_MACHINES.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isLocked(event.getBlock().getLocation())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "This machine is currently in use. Close its GUI before breaking it.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> isLocked(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> isLocked(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProtectedInventoryClick(InventoryClickEvent event) {
        if (!isProtectedInventoryTitle(event.getView().getTitle())) {
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProtectedInventoryDrag(InventoryDragEvent event) {
        if (!isProtectedInventoryTitle(event.getView().getTitle())) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        release(event.getPlayer());
    }

    private static boolean isProtectedInventoryTitle(String title) {
        return title.equals(ChatColor.DARK_GRAY + "Weapon Upgrade Table")
                || title.equals(ChatColor.DARK_GRAY + "Ammunition Workshop")
                || title.equals(ChatColor.DARK_RED + "Military Crafting Table")
                || title.equals(ChatColor.DARK_RED + "Machine Fabricator")
                || title.equals(ChatColor.DARK_RED + "Bombardment Terminal");
    }

    private static String key(Location location) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Machine location must have a world");
        }

        return location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":"
                + location.getBlockZ();
    }
}
