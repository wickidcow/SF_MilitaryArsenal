package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.items.CustomRecipeItem;
import com.Chagui68.weaponsaddon.utils.MachineSessionGuard;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.ChatColor;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;

public class MachineFabricatorHandler implements Listener {

    private static final String MACHINE_ID = "MA_MILITARY_MACHINE_FABRICATOR";
    private static final Map<UUID, Location> openFabricators = new HashMap<>();
    private static final List<CustomRecipeItem> RECIPE_CACHE = new ArrayList<>();
    private static final int[] GRID_SLOTS = {
            1, 2, 3, 4, 5, 6,
            10, 11, 12, 13, 14, 15,
            19, 20, 21, 22, 23, 24,
            28, 29, 30, 31, 32, 33,
            37, 38, 39, 40, 41, 42,
            46, 47, 48, 49, 50, 51
    };

    public static void registerRecipe(CustomRecipeItem item) {
        RECIPE_CACHE.add(item);
        System.out.println("✓ Recipe 6x6 registrado: " + item.getId() + " - Total: " + RECIPE_CACHE.size());
    }

    public static void openFabricatorGuiStatic(Player p, Location blockLoc) {
        openFabricatorGUI(p, blockLoc);
    }

    private static void openFabricatorGUI(Player p, Location blockLoc) {
        if (!MachineSessionGuard.acquire(blockLoc, p)) {
            return;
        }

        try {
            Inventory inv = createInventory(null, 54, ChatColor.DARK_RED + "Machine Fabricator");

            ItemStack background = new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " ");
            for (int i = 0; i < 54; i++) {
                inv.setItem(i, background);
            }

            ItemStack border = new CustomItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.DARK_RED + "▓");
            int[] borderSlots = { 0, 7, 8, 9, 16, 17, 18, 25, 26, 27, 34, 35, 36, 43, 44, 45, 52, 53 };
            for (int slot : borderSlots) {
                inv.setItem(slot, border);
            }

            for (int i = 0; i < GRID_SLOTS.length; i++) {
                String itemData = BlockStorage.getLocationInfo(blockLoc, "slot_" + i);
                if (itemData != null && !itemData.isEmpty()) {
                    ItemStack item = deserializeItemStack(itemData);
                    if (item != null) {
                        inv.setItem(GRID_SLOTS[i], item);
                    }
                } else {
                    inv.setItem(GRID_SLOTS[i], null);
                }
            }

            inv.setItem(8, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE,
                    ChatColor.GREEN + "⬇ RESULT ⬇",
                    "",
                    ChatColor.GRAY + "Place items in 6×6 grid",
                    ChatColor.GRAY + "Click CRAFT button"));

            inv.setItem(17, null);

            inv.setItem(0, new CustomItemStack(Material.RESPAWN_ANCHOR,
                    ChatColor.DARK_RED + "⚙ Machine Fabricator",
                    "",
                    ChatColor.RED + "6×6 Ultimate Crafting",
                    ChatColor.GRAY + "For advanced machines",
                    "",
                    ChatColor.AQUA + "Grid: 36 slots (6×6)"));

            inv.setItem(53, new CustomItemStack(Material.CRAFTING_TABLE,
                    ChatColor.GREEN + "▶ CRAFT ◀",
                    "",
                    ChatColor.GRAY + "Click to craft machine",
                    ChatColor.YELLOW + "Recipe must match exactly"));

            openFabricators.put(p.getUniqueId(), blockLoc.clone());
            p.openInventory(inv);
        } catch (RuntimeException ex) {
            openFabricators.remove(p.getUniqueId());
            MachineSessionGuard.release(blockLoc, p.getUniqueId());
            throw ex;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p))
            return;
        if (!e.getView().getTitle().equals(ChatColor.DARK_RED + "Machine Fabricator"))
            return;

        Location blockLoc = openFabricators.remove(p.getUniqueId());
        if (blockLoc == null)
            return;

        try {
            if (!BlockStorage.check(blockLoc, MACHINE_ID)) {
                return;
            }

            Inventory inv = e.getInventory();
            for (int i = 0; i < GRID_SLOTS.length; i++) {
                ItemStack item = inv.getItem(GRID_SLOTS[i]);
                BlockStorage.addBlockInfo(blockLoc, "slot_" + i,
                        item != null && item.getType() != Material.AIR ? serializeItemStack(item) : "");
            }
        } finally {
            MachineSessionGuard.release(blockLoc, p.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        if (!e.getView().getTitle().equals(ChatColor.DARK_RED + "Machine Fabricator"))
            return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        int[] allowedSlots = { 1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15, 19, 20, 21, 22, 23, 24,
                28, 29, 30, 31, 32, 33, 37, 38, 39, 40, 41, 42, 46, 47, 48, 49, 50, 51, 17 };
        boolean allowed = false;
        for (int s : allowedSlots) {
            if (slot == s) {
                allowed = true;
                break;
            }
        }

        if (!allowed && slot >= 0 && slot < 54) {
            e.setCancelled(true);
        }

        if (slot == 53 && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.CRAFTING_TABLE) {
            e.setCancelled(true);
            attemptCraft(p, e.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        SlimefunItem sfItem = BlockStorage.check(block);
        if (sfItem == null || !MACHINE_ID.equals(sfItem.getId())) {
            return;
        }

        Location blockLoc = block.getLocation();
        if (MachineSessionGuard.isLocked(blockLoc)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Close the Machine Fabricator before breaking it.");
            return;
        }

        for (int i = 0; i < GRID_SLOTS.length; i++) {
            String itemData = BlockStorage.getLocationInfo(blockLoc, "slot_" + i);
            if (itemData != null && !itemData.isEmpty()) {
                ItemStack item = deserializeItemStack(itemData);
                if (item != null) {
                    block.getWorld().dropItemNaturally(blockLoc, item);
                }
            }
        }
    }

    private static void attemptCraft(Player p, Inventory inv) {
        ItemStack[] grid = new ItemStack[GRID_SLOTS.length];
        for (int i = 0; i < GRID_SLOTS.length; i++) {
            grid[i] = inv.getItem(GRID_SLOTS[i]);
        }

        for (CustomRecipeItem customItem : RECIPE_CACHE) {
            if (customItem.getGridSize() != CustomRecipeItem.RecipeGridSize.GRID_6x6)
                continue;

            ItemStack[] recipe = customItem.getFullRecipe();
            if (matchesRecipe(grid, recipe)) {
                for (int i = 0; i < GRID_SLOTS.length; i++) {
                    ItemStack item = grid[i];
                    if (item != null && item.getType() != Material.AIR) {
                        item.setAmount(item.getAmount() - 1);
                        if (item.getAmount() <= 0) {
                            inv.setItem(GRID_SLOTS[i], null);
                        }
                    }
                }

                ItemStack output = customItem.getItem().clone();
                inv.setItem(17, output);
                p.sendMessage(ChatColor.GREEN + "✓ Crafted: " + ChatColor.WHITE
                        + ChatColor.stripColor(output.getItemMeta().getDisplayName()));
                return;
            }
        }

        p.sendMessage(ChatColor.RED + "✗ Invalid recipe!");
    }

    private static boolean matchesRecipe(ItemStack[] grid, ItemStack[] recipe) {
        if (grid.length != recipe.length)
            return false;

        for (int i = 0; i < grid.length; i++) {
            if (!itemsMatch(grid[i], recipe[i])) {
                return false;
            }
        }

        return true;
    }

    private static boolean itemsMatch(ItemStack item1, ItemStack item2) {
        if (isEmpty(item1) && isEmpty(item2))
            return true;
        if (isEmpty(item1) || isEmpty(item2))
            return false;

        SlimefunItem sf1 = SlimefunItem.getByItem(item1);
        SlimefunItem sf2 = SlimefunItem.getByItem(item2);

        if (sf1 != null && sf2 != null) {
            return sf1.getId().equals(sf2.getId());
        }

        if (sf1 == null && sf2 == null) {
            return item1.getType() == item2.getType();
        }

        return false;
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private static String serializeItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return "";

        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem != null) {
            return "SF:" + sfItem.getId() + ":" + item.getAmount();
        }

        return "VANILLA:" + item.getType() + ":" + item.getAmount();
    }

    private static ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty())
            return null;

        try {
            String[] parts = data.split(":");
            if (parts[0].equals("SF")) {
                SlimefunItem sfItem = SlimefunItem.getById(parts[1]);
                if (sfItem != null) {
                    ItemStack item = sfItem.getItem().clone();
                    item.setAmount(Integer.parseInt(parts[2]));
                    return item;
                }
            } else if (parts[0].equals("VANILLA")) {
                Material material = Material.valueOf(parts[1]);
                int amount = Integer.parseInt(parts[2]);
                return new ItemStack(material, amount);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
