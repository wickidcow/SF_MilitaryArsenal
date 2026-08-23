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

public class MilitaryMachineFabricator extends SlimefunItem {

    public static final SlimefunItemStack MILITARY_MACHINE_FABRICATOR = new SlimefunItemStack(
            "MA_MILITARY_MACHINE_FABRICATOR",
            Material.RESPAWN_ANCHOR,
            "&4⚙ &cMilitary Machine Fabricator",
            "",
            "&7Ultimate crafting station",
            "&7for advanced military machines",
            "",
            "&6Grid: &e6×6 (36 slots)",
            "&6Usage: &eRight-click to open",
            "",
            "&8⇨ Tier 3 Machine");

    public MilitaryMachineFabricator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType,
            ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        addItemHandler((BlockUseHandler) e -> {
            e.cancel();
            Player p = e.getPlayer();
            Block block = e.getClickedBlock().get();
            MachineFabricatorHandler.openFabricatorGuiStatic(p, block.getLocation());
        });
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[] {
                MilitaryComponents.REINFORCED_PLATING, MilitaryComponents.MILITARY_CIRCUIT,
                MilitaryComponents.MILITARY_CIRCUIT, MilitaryComponents.REINFORCED_PLATING,
                MilitaryComponents.MILITARY_CIRCUIT, MilitaryComponents.STABILIZER_UNIT,
                MilitaryComponents.REINFORCED_PLATING,
                MilitaryComponents.MILITARY_CIRCUIT,
                MilitaryComponents.MILITARY_CIRCUIT, SlimefunItems.CARGO_MANAGER, SlimefunItems.CARGO_MANAGER,
                MilitaryComponents.MILITARY_CIRCUIT,
                MilitaryComponents.REINFORCED_PLATING, MilitaryComponents.MILITARY_CIRCUIT,
                MilitaryComponents.MILITARY_CIRCUIT, MilitaryComponents.REINFORCED_PLATING
        };

        if (addon != null) {
            new MilitaryMachineFabricator(category, MILITARY_MACHINE_FABRICATOR,
                    RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
                    .register(addon);
        }
    }
}
