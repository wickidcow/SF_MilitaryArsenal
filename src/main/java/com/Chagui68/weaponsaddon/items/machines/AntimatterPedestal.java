package com.Chagui68.weaponsaddon.items.machines;

import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.api.SlimefunAddon;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockPlaceHandler;
import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class AntimatterPedestal extends SlimefunItem {

    public static final SlimefunItemStack ANTIMATTER_PEDESTAL = new SlimefunItemStack(
            "MA_ANTIMATTER_PEDESTAL",
            Material.DISPENSER,
            "&4☢ &cAntimatter Pedestal",
            "",
            "&7Part of 9×9 Antimatter Ritual Array",
            "&7Right-click to place catalyst items",
            "",
            "&6Status: &eWaiting for catalyst",
            "",
            "&c⚠ Requires complete 9×9 structure",
            "&c⚠ Must be placed in ritual pattern");

    public AntimatterPedestal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                Block b = e.getBlock();
                SlimefunStorageCompat.setData(b, "id", "MA_ANTIMATTER_PEDESTAL");
            }
        });
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[] {
                MilitaryComponents.ANTIMATTER_PARTICLE, MilitaryComponents.DIMENSIONAL_STABILIZER,
                MilitaryComponents.ANTIMATTER_PARTICLE,
                MilitaryComponents.ADVANCED_CIRCUIT, MilitaryComponents.ENERGY_MATRIX,
                MilitaryComponents.ADVANCED_CIRCUIT,
                MilitaryComponents.ANTIMATTER_PARTICLE, MilitaryComponents.ANTIMATTER_CRYSTAL,
                MilitaryComponents.ANTIMATTER_PARTICLE
        };

        new AntimatterPedestal(category, ANTIMATTER_PEDESTAL, RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
                .register(addon);
    }
}
