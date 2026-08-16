package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.utils.MachineSessionGuard;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;
import static org.bukkit.ChatColor.*;

public class AmmunitionWorkshopHandler implements Listener {

    private static final String MACHINE_ID = "MA_AMMUNITION_WORKSHOP";
    private static final Map<UUID, Location> openWorkshops = new HashMap<>();
    private static final int[] gridSlots = { 10, 11, 12, 19, 20, 21, 28, 29, 30 };
    private static final int resultSlot = 23;
    private static final int craftButtonSlot = 25;

    public static void openGuiStatic(Player p, Location loc) {
        openGui(p, loc);
    }

    private static void openGui(Player p, Location loc) {
        if (!MachineSessionGuard.acquire(loc, p)) {
            return;
        }

        try {
            Inventory inv = createInventory(null, 45, DARK_GRAY + "Ammunition Workshop");

            ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);

            for (int i = 0; i < inv.getSize(); i++)
                inv.setItem(i, glass);

            for (int i = 0; i < gridSlots.length; i++) {
                inv.setItem(gridSlots[i], null);
                String data = BlockStorage.getLocationInfo(loc, "slot_" + i);
                if (data != null && !data.isEmpty())
                    inv.setItem(gridSlots[i], deserializeItemStack(data));
            }

            String resData = BlockStorage.getLocationInfo(loc, "result_slot");
            inv.setItem(resultSlot, (resData != null && !resData.isEmpty()) ? deserializeItemStack(resData) : null);

            ItemStack anvil = new ItemStack(Material.ANVIL);
            ItemMeta anvilMeta = anvil.getItemMeta();
            anvilMeta.setDisplayName(GOLD + "Click to Craft");
            anvil.setItemMeta(anvilMeta);
            inv.setItem(craftButtonSlot, anvil);

            openWorkshops.put(p.getUniqueId(), loc.clone());
            p.openInventory(inv);
        } catch (RuntimeException ex) {
            openWorkshops.remove(p.getUniqueId());
            MachineSessionGuard.release(loc, p.getUniqueId());
            throw ex;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        if (!e.getView().getTitle().equals(DARK_GRAY + "Ammunition Workshop"))
            return;

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize())
            return;

        boolean isGrid = false;
        for (int s : gridSlots)
            if (s == slot)
                isGrid = true;

        if (slot == craftButtonSlot) {
            e.setCancelled(true);
            attemptCraft(e.getInventory());
        } else if (slot != resultSlot && !isGrid) {
            e.setCancelled(true);
        }
    }

    private void attemptCraft(Inventory inv) {
        ItemStack[] currentGrid = new ItemStack[9];
        for (int i = 0; i < gridSlots.length; i++)
            currentGrid[i] = inv.getItem(gridSlots[i]);

        if (matchesAmmoRecipe(currentGrid)) {
            ItemStack output = inv.getItem(resultSlot);
            SlimefunItem sfAmmo = SlimefunItem.getById("MA_MACHINE_GUN_AMMO");
            if (sfAmmo == null)
                return;

            ItemStack resultItem = sfAmmo.getItem().clone();
            int amountPerCraft = 8;

            if (output == null || output.getType() == Material.AIR) {
                resultItem.setAmount(amountPerCraft);
                inv.setItem(resultSlot, resultItem);
            } else if (SlimefunItem.getByItem(output) != null
                    && SlimefunItem.getByItem(output).getId().equals("MA_MACHINE_GUN_AMMO")) {
                if (output.getAmount() + amountPerCraft <= 64) {
                    output.setAmount(output.getAmount() + amountPerCraft);
                } else {
                    return;
                }
            } else {
                return;
            }

            for (int slot : gridSlots) {
                ItemStack item = inv.getItem(slot);
                if (item != null) {
                    if (item.getAmount() > 1)
                        item.setAmount(item.getAmount() - 1);
                    else
                        inv.setItem(slot, null);
                }
            }
        }
    }

    private boolean matchesAmmoRecipe(ItemStack[] grid) {
        return isMaterial(grid[1], Material.COPPER_INGOT) &&
                isMaterial(grid[3], Material.IRON_INGOT) &&
                isMaterial(grid[4], Material.GUNPOWDER) &&
                isMaterial(grid[5], Material.IRON_INGOT) &&
                isMaterial(grid[7], Material.IRON_NUGGET);
    }

    private boolean isMaterial(ItemStack item, Material type) {
        return item != null && item.getType() == type;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p))
            return;
        if (!e.getView().getTitle().equals(DARK_GRAY + "Ammunition Workshop"))
            return;

        UUID uuid = p.getUniqueId();
        Location loc = openWorkshops.remove(uuid);
        if (loc == null)
            return;

        try {
            if (BlockStorage.check(loc, MACHINE_ID)) {
                saveInventory(e.getInventory(), loc);
            }
        } finally {
            MachineSessionGuard.release(loc, uuid);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Location loc = b.getLocation();

        if (!BlockStorage.check(loc, MACHINE_ID)) {
            return;
        }

        if (MachineSessionGuard.isLocked(loc)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(RED + "Close the Ammunition Workshop before breaking it.");
            return;
        }

        e.setDropItems(false);

        for (int i = 0; i < gridSlots.length; i++) {
            String data = BlockStorage.getLocationInfo(loc, "slot_" + i);
            if (data != null && !data.isEmpty()) {
                ItemStack item = deserializeItemStack(data);
                if (item != null)
                    loc.getWorld().dropItemNaturally(loc, item);
            }
        }

        String resData = BlockStorage.getLocationInfo(loc, "result_slot");
        if (resData != null && !resData.isEmpty()) {
            ItemStack item = deserializeItemStack(resData);
            if (item != null)
                loc.getWorld().dropItemNaturally(loc, item);
        }

        BlockStorage.clearBlockInfo(loc);
    }

    private void saveInventory(Inventory inv, Location loc) {
        for (int i = 0; i < gridSlots.length; i++) {
            BlockStorage.addBlockInfo(loc, "slot_" + i, serializeItemStack(inv.getItem(gridSlots[i])));
        }
        BlockStorage.addBlockInfo(loc, "result_slot", serializeItemStack(inv.getItem(resultSlot)));
    }

    private static String serializeItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return "";

        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem != null)
            return "SF:" + sfItem.getId() + ":" + item.getAmount();

        return "V:" + item.getType().name() + ":" + item.getAmount();
    }

    private static ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty())
            return null;

        try {
            String[] parts = data.split(":");
            if (parts[0].equals("SF")) {
                SlimefunItem sf = SlimefunItem.getById(parts[1]);
                if (sf == null)
                    return null;
                ItemStack is = sf.getItem().clone();
                is.setAmount(Integer.parseInt(parts[2]));
                return is;
            }
            return new ItemStack(Material.valueOf(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception e) {
            return null;
        }
    }
}
