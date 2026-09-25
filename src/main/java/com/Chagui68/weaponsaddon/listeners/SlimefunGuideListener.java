package com.Chagui68.weaponsaddon.listeners;

import com.Chagui68.weaponsaddon.items.CustomRecipeItem;
import com.Chagui68.weaponsaddon.items.gui.RecipeViewerGUI;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.Chagui68.weaponsaddon.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getScheduler;

public class SlimefunGuideListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    // Known Slimefun Guide titles (check multiple languages/versions)
    private static final String[] SLIMEFUN_GUIDE_IDENTIFIERS = {
            "Slimefun Guide",
            "Slimefun",
            "Item Group:",
            "Search Results"
    };

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSlimefunGuideClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        Component title = e.getView().title();

        // ONLY trigger in actual Slimefun Guide - strict check
        if (!isSlimefunGuide(title)) {
            return;
        }

        SlimefunItem sfItem = SlimefunItem.getByItem(clicked);

        if (sfItem == null) {
            return;
        }

        // Only handle CustomRecipeItem clicks
        if (sfItem instanceof CustomRecipeItem) {
            CustomRecipeItem customItem = (CustomRecipeItem) sfItem;

            e.setCancelled(true);
            p.closeInventory();

            // Open custom recipe viewer after a tick
            getScheduler().runTaskLater(
                    getPluginManager().getPlugin("WeaponsAddon"),
                    () -> {
                        try {
                            if (customItem.getGridSize() == CustomRecipeItem.RecipeGridSize.GRID_4x4) {
                                RecipeViewerGUI.open4x4Recipe(p,
                                        displayName(customItem.getResultItem()),
                                        customItem.getResultItem(),
                                        customItem.getFullRecipe());
                            } else {
                                RecipeViewerGUI.open6x6Recipe(p,
                                        displayName(customItem.getResultItem()),
                                        customItem.getResultItem(),
                                        customItem.getFullRecipe());
                            }
                        } catch (Exception ex) {
                            p.sendMessage(ColorUtils.component("&cError opening recipe viewer."));
                            ex.printStackTrace();
                        }
                    },
                    1L);
        }
    }

    private static String displayName(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta().displayName() == null) {
            return item == null ? "" : item.getType().name();
        }
        return PLAIN.serialize(item.getItemMeta().displayName());
    }

    /**
     * Strictly check if the inventory is a Slimefun Guide.
     * Only matches actual Slimefun Guide titles, NOT crafting tables or other GUIs.
     */
    private boolean isSlimefunGuide(Component title) {
        if (title == null)
            return false;

        String plainTitle = PLAIN.serialize(title);

        // Exclude our own GUIs
        if (plainTitle.contains("Military") ||
                plainTitle.contains("Crafting Table") ||
                plainTitle.contains("Workshop") ||
                plainTitle.contains("Upgrade") ||
                plainTitle.contains("Recipe:") ||
                plainTitle.contains("Fabricator") ||
                plainTitle.contains("Terminal")) {
            return false;
        }

        // Check for Slimefun Guide identifiers
        for (String identifier : SLIMEFUN_GUIDE_IDENTIFIERS) {
            if (plainTitle.contains(identifier)) {
                return true;
            }
        }

        return false;
    }
}
