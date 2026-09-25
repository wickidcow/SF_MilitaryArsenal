package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import com.Chagui68.weaponsaddon.items.machines.energy.EnergyManager;
import com.Chagui68.weaponsaddon.protection.ProtectionService;
import com.Chagui68.weaponsaddon.utils.ColorUtils;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getScheduler;

public class TerminalClickHandler implements Listener {

    private static final Map<UUID, Location> awaitingCoordinates = new ConcurrentHashMap<>();
    private static final Map<UUID, Location> playerTerminalLocations = new HashMap<>();
    private static final Map<UUID, Long> lastBombardments = new HashMap<>();

    public static void registerInventory(Player p, Inventory inv, Location blockLoc) {
        playerTerminalLocations.put(p.getUniqueId(), blockLoc);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;

        if (!e.getView().title().equals(BombardmentTerminal.GUI_TITLE))
            return;

        if (e.isShiftClick()) {
            e.setCancelled(true);
            return;
        }

        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == 11 || slot == 16) {
            return;
        }

        if (slot == 22) {
            e.setCancelled(true);

            Location terminalLoc = playerTerminalLocations.get(p.getUniqueId());
            if (terminalLoc == null || !SlimefunStorageCompat.is(terminalLoc, "MA_BOMBARDMENT_TERMINAL")) {
                p.sendMessage(ColorUtils.RED + "[Terminal] Error: terminal is no longer available.");
                return;
            }

            if (!ProtectionService.canUse(p, terminalLoc)) {
                ProtectionService.deny(p, "terminal use");
                return;
            }

            if (awaitingCoordinates.containsKey(p.getUniqueId())) {
                p.sendMessage(ColorUtils.RED + "[Terminal] You already have a paid bombardment awaiting coordinates.");
                return;
            }

            long cooldownRemaining = getCooldownRemainingMillis(p.getUniqueId());
            if (cooldownRemaining > 0) {
                p.sendMessage(ColorUtils.RED + "[Terminal] Cooling down. Try again in "
                        + Math.max(1, (cooldownRemaining + 999) / 1000) + "s.");
                return;
            }

            int currentEnergy = EnergyManager.getCharge(terminalLoc);
            Inventory inv = e.getInventory();
            ItemStack tntSlot = inv.getItem(11);
            ItemStack starSlot = inv.getItem(16);

            int tntCount = isPlainMaterial(tntSlot, Material.TNT) ? tntSlot.getAmount() : 0;
            int starCount = isPlainMaterial(starSlot, Material.NETHER_STAR) ? starSlot.getAmount() : 0;

            if (currentEnergy < BombardmentTerminal.getEnergyRequired()) {
                p.sendMessage(ColorUtils.RED + "✗ [Terminal] Insufficient energy!");
                p.sendMessage(ColorUtils.GRAY + "You need: "
                        + formatEnergy(BombardmentTerminal.getEnergyRequired() - currentEnergy) + " J more");
                p.sendMessage(ColorUtils.YELLOW + "Connect to Slimefun power grid");
                return;
            }

            if (tntCount < 10 || starCount < 5) {
                p.sendMessage(ColorUtils.RED + "✗ [Terminal] Insufficient resources!");
                p.sendMessage(ColorUtils.GRAY + "You need:");
                if (tntCount < 10)
                    p.sendMessage(ColorUtils.YELLOW + " • " + (10 - tntCount) + " more plain TNT");
                if (starCount < 5)
                    p.sendMessage(ColorUtils.YELLOW + " • " + (5 - starCount) + " more plain Nether Stars");
                return;
            }

            if (!EnergyManager.removeCharge(terminalLoc, BombardmentTerminal.getEnergyRequired())) {
                p.sendMessage(ColorUtils.RED + "✗ [Terminal] Energy changed before activation. Try again.");
                return;
            }

            consume(inv, 11, 10);
            consume(inv, 16, 5);
            awaitingCoordinates.put(p.getUniqueId(), terminalLoc.clone());

            p.closeInventory();
            p.sendMessage(ColorUtils.GREEN + "✓ [Terminal] Resources consumed:");
            p.sendMessage(ColorUtils.GRAY + " • 10 TNT");
            p.sendMessage(ColorUtils.GRAY + " • 5 Nether Stars");
            p.sendMessage(ColorUtils.AQUA + " • 2,000,000 J energy");
            p.sendMessage(ColorUtils.YELLOW + "→ [Terminal] Enter coordinates: X Y Z");
            p.sendMessage(ColorUtils.GRAY + "Target must be in the terminal's world, within range, loaded, and allowed by land protection.");
            return;
        }

        if (slot < 27) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!e.getView().title().equals(BombardmentTerminal.GUI_TITLE))
            return;

        int topSize = e.getView().getTopInventory().getSize();
        if (e.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;

        if (!e.getView().title().equals(BombardmentTerminal.GUI_TITLE))
            return;

        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();

        returnItemToPlayer(p, inv.getItem(11));
        returnItemToPlayer(p, inv.getItem(16));
        inv.setItem(11, null);
        inv.setItem(16, null);

        playerTerminalLocations.remove(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Location loc = b.getLocation();

        if (SlimefunStorageCompat.is(loc, "MA_BOMBARDMENT_TERMINAL")) {
            awaitingCoordinates.entrySet().removeIf(entry -> sameBlock(entry.getValue(), loc));
            BombardmentTerminal.removeSatelliteModel(loc);
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();
        Location terminalLoc = awaitingCoordinates.get(playerId);
        if (terminalLoc == null)
            return;

        e.setCancelled(true);
        String[] parts = PlainTextComponentSerializer.plainText().serialize(e.message()).trim().split("\\s+");

        if (parts.length != 3) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Invalid format. Use: X Y Z");
            p.sendMessage(ColorUtils.GRAY + "Example: 100 64 -200");
            return;
        }

        final int x;
        final int y;
        final int z;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
            z = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Invalid coordinates. Use whole numbers.");
            p.sendMessage(ColorUtils.GRAY + "Example: 100 64 -200");
            return;
        }

        if (!awaitingCoordinates.remove(playerId, terminalLoc)) {
            return;
        }

        getScheduler().runTask(WeaponsAddon.getInstance(), () -> validateAndExecute(p, terminalLoc, x, y, z));
    }

    private void validateAndExecute(Player p, Location terminalLoc, int x, int y, int z) {
        if (!p.isOnline()) {
            return;
        }

        if (!SlimefunStorageCompat.is(terminalLoc, "MA_BOMBARDMENT_TERMINAL")) {
            p.sendMessage(ColorUtils.RED + "[Terminal] The terminal was removed before targeting completed.");
            return;
        }

        if (!ProtectionService.canUse(p, terminalLoc)) {
            ProtectionService.deny(p, "terminal use");
            return;
        }

        World world = terminalLoc.getWorld();
        if (world == null) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Terminal world is unavailable.");
            return;
        }

        if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Invalid Y coordinate for this world ("
                    + world.getMinHeight() + " to " + (world.getMaxHeight() - 1) + ").");
            awaitingCoordinates.put(p.getUniqueId(), terminalLoc);
            return;
        }

        int maxRange = Math.max(16, WeaponsAddon.getInstance().getConfig().getInt("bombardment.max_horizontal_range", 256));
        long dx = (long) x - terminalLoc.getBlockX();
        long dz = (long) z - terminalLoc.getBlockZ();
        long distanceSquared = dx * dx + dz * dz;
        long maxRangeSquared = (long) maxRange * maxRange;
        if (distanceSquared > maxRangeSquared) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Target is too far away. Maximum horizontal range: " + maxRange + " blocks.");
            awaitingCoordinates.put(p.getUniqueId(), terminalLoc);
            return;
        }

        Location target = new Location(world, x + 0.5, y, z + 0.5);
        if (!world.getWorldBorder().isInside(target)) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Target is outside the world border.");
            awaitingCoordinates.put(p.getUniqueId(), terminalLoc);
            return;
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Target chunk is not loaded. Airstrikes cannot force-load remote chunks.");
            awaitingCoordinates.put(p.getUniqueId(), terminalLoc);
            return;
        }

        if (!ProtectionService.canBombard(p, target)) {
            ProtectionService.deny(p, "bombardment");
            awaitingCoordinates.put(p.getUniqueId(), terminalLoc);
            return;
        }

        long cooldownRemaining = getCooldownRemainingMillis(p.getUniqueId());
        if (cooldownRemaining > 0) {
            p.sendMessage(ColorUtils.RED + "[Terminal] Cooling down. Try again in "
                    + Math.max(1, (cooldownRemaining + 999) / 1000) + "s.");
            awaitingCoordinates.put(p.getUniqueId(), terminalLoc);
            return;
        }

        lastBombardments.put(p.getUniqueId(), System.currentTimeMillis());
        p.sendMessage(ColorUtils.GREEN + "✓ [Terminal] Coordinates confirmed: " + x + " " + y + " " + z);
        p.sendMessage(ColorUtils.DARK_RED + "⚠ [Terminal] BOMBARDMENT INITIATED");
        p.sendMessage(ColorUtils.GRAY + "Impact in 3 seconds...");
        AirstrikeExecutor.executeBombardment(target, p);
    }

    private static boolean isPlainMaterial(ItemStack item, Material type) {
        return item != null && item.isSimilar(new ItemStack(type));
    }

    private static void consume(Inventory inv, int slot, int amount) {
        ItemStack item = inv.getItem(slot);
        if (item == null)
            return;

        int remaining = item.getAmount() - amount;
        if (remaining <= 0) {
            inv.setItem(slot, null);
        } else {
            item.setAmount(remaining);
        }
    }

    private static void returnItemToPlayer(Player p, ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return;

        Map<Integer, ItemStack> leftovers = p.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), leftover);
        }
    }

    private static boolean sameBlock(Location first, Location second) {
        return first.getWorld() != null && second.getWorld() != null
                && first.getWorld().getUID().equals(second.getWorld().getUID())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }

    private static long getCooldownRemainingMillis(UUID playerId) {
        int cooldownSeconds = Math.max(0,
                WeaponsAddon.getInstance().getConfig().getInt("bombardment.cooldown_seconds", 30));
        if (cooldownSeconds == 0)
            return 0;

        Long last = lastBombardments.get(playerId);
        if (last == null)
            return 0;

        long remaining = cooldownSeconds * 1000L - (System.currentTimeMillis() - last);
        return Math.max(0, remaining);
    }

    private static String formatEnergy(int energy) {
        if (energy >= 1000000) {
            return String.format("%.1fM", energy / 1000000.0);
        } else if (energy >= 1000) {
            return String.format("%.1fK", energy / 1000.0);
        }
        return String.valueOf(energy);
    }
}
