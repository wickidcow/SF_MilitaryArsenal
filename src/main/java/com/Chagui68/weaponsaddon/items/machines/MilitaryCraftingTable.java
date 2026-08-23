package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
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

public class MilitaryCraftingTable extends SlimefunItem {

    public static final SlimefunItemStack MILITARY_CRAFTING_TABLE = new SlimefunItemStack(
            "MA_MILITARY_CRAFTING_TABLE",
            Material.SMITHING_TABLE,
            "&c⚔ &4Military Crafting Table",
            "",
            "&74×4 Advanced Crafting Station",
            "&7For military weapons & components",
            "",
            "&eRight-Click to open");

    public MilitaryCraftingTable(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType,
            ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        addItemHandler((BlockUseHandler) e -> {
            e.cancel();
            Player p = e.getPlayer();
            Block block = e.getClickedBlock().get();
            MilitaryCraftingHandler.openTableGuiStatic(p, block.getLocation());
        });
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[] {
                SlimefunItems.STEEL_PLATE, MilitaryComponents.BASIC_CIRCUIT, SlimefunItems.STEEL_PLATE,
                MilitaryComponents.BASIC_CIRCUIT, new ItemStack(Material.SMITHING_TABLE),
                MilitaryComponents.BASIC_CIRCUIT,
                SlimefunItems.STEEL_PLATE, SlimefunItems.HARDENED_METAL_INGOT, SlimefunItems.STEEL_PLATE
        };

        new MilitaryCraftingTable(category, MILITARY_CRAFTING_TABLE, RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
                .register(addon);
    }
}
