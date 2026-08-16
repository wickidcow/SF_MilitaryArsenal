package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.utils.MachineSessionManager;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.RED;

public class AmmunitionWorkshopHandler implements Listener {

    private static final Map<UUID, Location> openWorkshops = new HashMap<>();
    private static final int[] gridSlots = { 10, 11, 12, 19, 20, 21, 28, 29, 30 };
    private static final int resultSlot = 23;
    private static final int craftButtonSlot = 25;

    public static void openGuiStatic(Player p, Location loc) {
        openGui(p, loc);
    }

    private static void openGui(Player p, Location loc) {
        if (!MachineSessionManager.tryAcquire(p, loc)) {
            p.sendMessage(RED + "This machine is already in use. Close your current machine GUI and try again.");
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

            openWorkshops.put(p.getUniqueId(), loc);
            p.openInventory(inv);
        } catch (RuntimeException ex) {
            openWorkshops.remove(p.getUniqueId());
            MachineSessionManager.release(p);
            throw ex;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p))
            return;
        if (!e.getView().getTitle().equals(DARK_GRAY + "Ammunition Workshop"))
            return;

        if (e.isShiftClick()) {
            e.setCancelled(true);
            return;
        }

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize())
            return;

        if (slot == resultSlot) {
            e.setCancelled(true);
            takeResult(p, e.getInventory());
            return;
        }

        boolean isGrid = false;
        for (int s : gridSlots) {
            if (s == slot) {
                isGrid = true;
                break;
            }
        }

        if (slot == craftButtonSlot) {
            e.setCancelled(true);
            attemptCraft(e.getInventory());
        } else if (!isGrid) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!e.getView().getTitle().equals(DARK_GRAY + "Ammunition Workshop"))
            return;

        int topSize = e.getView().getTopInventory().getSize();
        if (e.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            e.setCancelled(true);
        }
    }

    private void takeResult(Player p, Inventory inv) {
        ItemStack result = inv.getItem(resultSlot);
        if (result == null || result.getType() == Material.AIR)
            return;

        inv.setItem(resultSlot, null);
        Map<Integer, ItemStack> leftovers = p.getInventory().addItem(result);
        for (ItemStack leftover : leftovers.values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), leftover);
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
        return isPlainMaterial(grid[1], Material.COPPER_INGOT)
                && isPlainMaterial(grid[3], Material.IRON_INGOT)
                && isPlainMaterial(grid[4], Material.GUNPOWDER)
                && isPlainMaterial(grid[5], Material.IRON_INGOT)
                && isPlainMaterial(grid[7], Material.IRON_NUGGET);
    }

    private boolean isPlainMaterial(ItemStack item, Material type) {
        return item != null && item.isSimilar(new ItemStack(type));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        if (!e.getView().getTitle().equals(DARK_GRAY + "Ammunition Workshop"))
            return;

        Player p = (Player) e.getPlayer();
        Location loc = openWorkshops.remove(p.getUniqueId());
        if (loc == null)
            return;

        try {
            saveInventory(e.getInventory(), loc);
        } finally {
            MachineSessionManager.release(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Location loc = b.getLocation();

        if (BlockStorage.check(loc, "MA_AMMUNITION_WORKSHOP")) {
            e.setDropItems(false);

            for (int i = 0; i < gridSlots.length; i++) {
                String key = "slot_" + i;
                String data = BlockStorage.getLocationInfo(loc, key);
                if (data != null && !data.isEmpty()) {
                    ItemStack item = deserializeItemStack(data);
                    if (item != null)
                        loc.getWorld().dropItemNaturally(loc, item);
                    BlockStorage.addBlockInfo(loc, key, "");
                }
            }

            String resData = BlockStorage.getLocationInfo(loc, "result_slot");
            if (resData != null && !resData.isEmpty()) {
                ItemStack item = deserializeItemStack(resData);
                if (item != null)
                    loc.getWorld().dropItemNaturally(loc, item);
                BlockStorage.addBlockInfo(loc, "result_slot", "");
            }
        }
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
