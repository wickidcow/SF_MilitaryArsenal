package com.Chagui68.weaponsaddon.items.machines;

import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.api.SlimefunAddon;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockUseHandler;
import com.github.drakescraft_labs.slimefun4.implementation.SlimefunItems;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AmmunitionWorkshop extends SlimefunItem {

    public static final SlimefunItemStack AMMUNITION_WORKSHOP = new SlimefunItemStack(
            "MA_AMMUNITION_WORKSHOP",
            Material.SMITHING_TABLE,
            "&6⚒ &eAmmunition Workshop",
            "",
            "&73×3 Basic Crafting Station",
            "&7For ammunition components",
            "",
            "&eRight-Click to open");

    public AmmunitionWorkshop(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        addItemHandler((BlockUseHandler) e -> {
            e.cancel();
            Player p = e.getPlayer();
            Block block = e.getClickedBlock().get();
            AmmunitionWorkshopHandler.openGuiStatic(p, block.getLocation());
        });
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[] {
                new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.CRAFTING_TABLE),
                new ItemStack(Material.IRON_BLOCK),
                new ItemStack(Material.IRON_INGOT), SlimefunItems.STEEL_PLATE, new ItemStack(Material.IRON_INGOT),
                new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.ANVIL), new ItemStack(Material.IRON_BLOCK)
        };

        new AmmunitionWorkshop(category, AMMUNITION_WORKSHOP, RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
                .register(addon);
    }
}
