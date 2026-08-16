package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.items.CustomRecipeItem;
import com.Chagui68.weaponsaddon.utils.MachineSessionManager;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

public class MilitaryCraftingHandler implements Listener {

    private static final Map<UUID, Location> openTables = new HashMap<>();
    private static final List<CustomRecipeItem> RECIPE_CACHE = new ArrayList<>();

    public static void registerRecipe(CustomRecipeItem item) {
        RECIPE_CACHE.add(item);
        System.out.println("✓ Recipe 4x4 registrado: " + item.getId() + " - Total: " + RECIPE_CACHE.size());
    }

    public static void openTableGuiStatic(Player p, Location blockLoc) {
        openTableGUI(p, blockLoc);
    }

    private static void openTableGUI(Player p, Location blockLoc) {
        if (!MachineSessionManager.tryAcquire(p, blockLoc)) {
            p.sendMessage(ChatColor.RED + "This machine is already in use. Close your current machine GUI and try again.");
            return;
        }

        try {
            Inventory inv = createInventory(null, 54, ChatColor.DARK_RED + "Military Crafting Table");

            ItemStack background = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ");
            for (int i = 0; i < 54; i++) {
                inv.setItem(i, background);
            }

            ItemStack border = new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, ChatColor.GOLD + "▓");
            int[] borderSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 36, 45, 46, 47, 48, 49, 50, 51, 52,
                    17, 26, 35, 44 };
            for (int slot : borderSlots) {
                inv.setItem(slot, border);
            }

            int[] gridSlots = {
                    11, 12, 13, 14,
                    20, 21, 22, 23,
                    29, 30, 31, 32,
                    38, 39, 40, 41
            };

            for (int i = 0; i < 16; i++) {
                String itemData = BlockStorage.getLocationInfo(blockLoc, "slot_" + i);
                if (itemData != null && !itemData.isEmpty()) {
                    ItemStack item = deserializeItemStack(itemData);
                    if (item != null) {
                        inv.setItem(gridSlots[i], item);
                    }
                } else {
                    inv.setItem(gridSlots[i], null);
                }
            }

            inv.setItem(25, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE,
                    ChatColor.GREEN + "⬇ RESULT ⬇",
                    "",
                    ChatColor.GRAY + "Place items in 4×4 grid",
                    ChatColor.GRAY + "Click CRAFT button"));

            inv.setItem(34, null);

            inv.setItem(53, new CustomItemStack(Material.SMITHING_TABLE,
                    ChatColor.GOLD + "⚒ Military Crafting Table",
                    "",
                    ChatColor.YELLOW + "4×4 Advanced Crafting",
                    ChatColor.GRAY + "For weapons & components",
                    "",
                    ChatColor.AQUA + "Grid: 16 slots (4×4)",
                    ChatColor.GREEN + "✓ Inventory persists"));

            inv.setItem(49, new CustomItemStack(Material.CRAFTING_TABLE,
                    ChatColor.GREEN + "▶ CRAFT ◀",
                    "",
                    ChatColor.GRAY + "Click to craft item",
                    ChatColor.YELLOW + "Recipe must match exactly"));

            openTables.put(p.getUniqueId(), blockLoc);
            p.openInventory(inv);
        } catch (RuntimeException ex) {
            openTables.remove(p.getUniqueId());
            MachineSessionManager.release(p);
            throw ex;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        if (!e.getView().getTitle().equals(ChatColor.DARK_RED + "Military Crafting Table"))
            return;

        Player p = (Player) e.getPlayer();
        Location blockLoc = openTables.remove(p.getUniqueId());
        if (blockLoc == null)
            return;

        try {
            int[] gridSlots = { 11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41 };
            Inventory inv = e.getInventory();

            for (int i = 0; i < 16; i++) {
                ItemStack item = inv.getItem(gridSlots[i]);
                if (item != null && item.getType() != Material.AIR) {
                    BlockStorage.addBlockInfo(blockLoc, "slot_" + i, serializeItemStack(item));
                } else {
                    BlockStorage.addBlockInfo(blockLoc, "slot_" + i, "");
                }
            }
        } finally {
            MachineSessionManager.release(p);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        if (!e.getView().getTitle().equals(ChatColor.DARK_RED + "Military Crafting Table"))
            return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        int[] allowedSlots = { 11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41, 34 };
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

        if (slot == 49 && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.CRAFTING_TABLE) {
            e.setCancelled(true);
            attemptCraft(p, e.getInventory());
        }
    }

    private static void attemptCraft(Player p, Inventory inv) {
        int[] gridSlots = { 11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41 };
        ItemStack[] grid = new ItemStack[16];

        for (int i = 0; i < 16; i++) {
            grid[i] = inv.getItem(gridSlots[i]);
        }

        System.out.println("=== CRAFTEO 4x4 ===");
        System.out.println("Recipes: " + RECIPE_CACHE.size());

        for (CustomRecipeItem customItem : RECIPE_CACHE) {
            if (customItem.getGridSize() != CustomRecipeItem.RecipeGridSize.GRID_4x4)
                continue;

            ItemStack[] recipe = customItem.getFullRecipe();
            System.out.println("Checkeando: " + customItem.getId());

            if (matchesRecipe(grid, recipe)) {
                for (int i = 0; i < 16; i++) {
                    ItemStack item = grid[i];
                    if (item != null && item.getType() != Material.AIR) {
                        item.setAmount(item.getAmount() - 1);
                        if (item.getAmount() <= 0) {
                            inv.setItem(gridSlots[i], null);
                        }
                    }
                }

                ItemStack output = customItem.getItem().clone();
                inv.setItem(34, output);
                p.sendMessage(ChatColor.GREEN + "✓ Crafted: " + ChatColor.WHITE
                        + ChatColor.stripColor(output.getItemMeta().getDisplayName()));
                System.out.println("✓ CRAFTEO EXITOSO!");
                return;
            }
        }

        System.out.println("❌ Receta inválida");
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
            return item1.isSimilar(item2);
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
