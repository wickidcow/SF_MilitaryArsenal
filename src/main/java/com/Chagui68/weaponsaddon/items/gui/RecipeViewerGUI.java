package com.Chagui68.weaponsaddon.items.gui;

import com.github.drakescraft_labs.slimefun4.libraries.dough.items.CustomItemStack;
import com.Chagui68.weaponsaddon.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;

public class RecipeViewerGUI implements Listener {

    private static final Map<UUID, String> openViewers = new HashMap<>();

    public static void open4x4Recipe(Player p, String itemName, ItemStack result, ItemStack[] recipe) {
        Inventory inv = createInventory(null, 54, ColorUtils.component("&4⚒ Recipe: " + itemName));

        // Fondo negro
        ItemStack background = new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        // Grid 4x4 centrado
        int[] gridSlots = {
                11, 12, 13, 14,
                20, 21, 22, 23,
                29, 30, 31, 32,
                38, 39, 40, 41
        };

        for (int i = 0; i < 16 && i < recipe.length; i++) {
            inv.setItem(gridSlots[i], recipe[i]);
        }

        // Bordes naranjas
        ItemStack border = new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6▓");
        int[] borderSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 36, 45, 46, 47, 48, 49, 50, 51, 52, 53, 17, 26, 35,
                44 };
        for (int slot : borderSlots) {
            inv.setItem(slot, border);
        }

        // Indicador de resultado
        inv.setItem(25, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE,
                "&a⬇ RESULT ⬇",
                "",
                "&7Item crafted from 4×4 grid"));

        inv.setItem(34, result);

        // Info de la tabla
        inv.setItem(53, new CustomItemStack(
                Material.SMITHING_TABLE,
                "&6ℹ Military Crafting Table",
                "",
                "&e4×4 Advanced Crafting",
                "&7Place items exactly as shown"));

        // Botón de cierre
        inv.setItem(49, new CustomItemStack(
                Material.BARRIER,
                "&c✖ Close",
                "",
                "&7Click to return to guide"));

        p.openInventory(inv);
        openViewers.put(p.getUniqueId(), itemName);
    }

    public static void open6x6Recipe(Player p, String itemName, ItemStack result, ItemStack[] recipe) {
        Inventory inv = createInventory(null, 54, ColorUtils.component("&4⚙ Recipe: " + itemName));

        // Fondo negro
        ItemStack background = new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        // Grid 6x6 completo
        int[] gridSlots = {
                1, 2, 3, 4, 5, 6,
                10, 11, 12, 13, 14, 15,
                19, 20, 21, 22, 23, 24,
                28, 29, 30, 31, 32, 33,
                37, 38, 39, 40, 41, 42,
                46, 47, 48, 49, 50, 51
        };

        for (int i = 0; i < 36 && i < recipe.length; i++) {
            inv.setItem(gridSlots[i], recipe[i]);
        }

        // Bordes rojos
        ItemStack border = new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&4▓");
        int[] borderSlots = { 0, 7, 8, 9, 16, 17, 18, 25, 26, 27, 34, 35, 36, 43, 44, 45, 52, 53 };
        for (int slot : borderSlots) {
            inv.setItem(slot, border);
        }

        // Indicador de resultado
        inv.setItem(8, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE,
                "&a⬇ RESULT ⬇",
                "",
                "&7Item crafted from 6×6 grid"));

        inv.setItem(17, result);

        // Info de la máquina
        inv.setItem(0, new CustomItemStack(
                Material.RESPAWN_ANCHOR,
                "&4ℹ Machine Fabricator",
                "",
                "&c6×6 Ultimate Crafting",
                "&7Place items exactly as shown"));

        // Botón de cierre
        inv.setItem(53, new CustomItemStack(
                Material.BARRIER,
                "&c✖ Close",
                "",
                "&7Click to return to guide"));

        p.openInventory(inv);
        openViewers.put(p.getUniqueId(), itemName);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;

        Player p = (Player) e.getWhoClicked();
        if (!openViewers.containsKey(p.getUniqueId()))
            return;

        e.setCancelled(true);

        int slot = e.getRawSlot();

        // Cerrar con botón BARRIER
        if ((slot == 49 || slot == 53) && e.getCurrentItem() != null &&
                e.getCurrentItem().getType() == Material.BARRIER) {
            p.closeInventory();
            openViewers.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;

        openViewers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;

        Player p = (Player) e.getWhoClicked();
        if (openViewers.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
