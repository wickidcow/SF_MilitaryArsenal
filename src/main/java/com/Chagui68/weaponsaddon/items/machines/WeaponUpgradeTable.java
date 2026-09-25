package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
import com.Chagui68.weaponsaddon.items.machines.energy.EnergyManager;
import com.Chagui68.weaponsaddon.handlers.UpgradeTableHandler;
import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.api.SlimefunAddon;
import com.github.drakescraft_labs.slimefun4.core.attributes.EnergyNetComponent;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockUseHandler;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockPlaceHandler;
import com.github.drakescraft_labs.slimefun4.core.networks.energy.EnergyNetComponentType;
import com.github.drakescraft_labs.slimefun4.implementation.SlimefunItems;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import javax.annotation.Nonnull;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class WeaponUpgradeTable extends SlimefunItem implements EnergyNetComponent {

    private static final int ENERGY_CAPACITY = 100000;

    public static final SlimefunItemStack WEAPON_UPGRADABLE_TABLE = new SlimefunItemStack(
            "MA_WEAPON_UPGRADE_TABLE",
            Material.SMITHING_TABLE,
            "&6⚒ &eWeapon Upgrade Table",
            "",
            "&7Enhance your military gear",
            "&7using advanced upgrade modules",
            "",
            "&6Energy: &e50,000 J per upgrade",
            "&6Capacity: &e100,000 J",
            "",
            "&eRight-Click to open",
            "",
            "&8⇨ Tier 3 Workbench");

    public WeaponUpgradeTable(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return ENERGY_CAPACITY;
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                Block b = e.getBlock();
                SlimefunStorageCompat.setData(b, "id", "MA_WEAPON_UPGRADE_TABLE");
            }
        });

        addItemHandler((BlockUseHandler) e -> {
            e.cancel();
            Player p = e.getPlayer();
            Block block = e.getClickedBlock().get();
            int currentEnergy = EnergyManager.getCharge(block.getLocation());
            UpgradeTableHandler.openUpgradeGui(p, block.getLocation(), currentEnergy);
        });
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[] {
                MilitaryComponents.REINFORCED_PLATING, MilitaryComponents.ADVANCED_CIRCUIT,
                MilitaryComponents.REINFORCED_PLATING,
                MilitaryComponents.POWER_CELL, new ItemStack(Material.SMITHING_TABLE), MilitaryComponents.POWER_CELL,
                MilitaryComponents.REINFORCED_PLATING, MilitaryComponents.STEEL_FRAME,
                MilitaryComponents.REINFORCED_PLATING
        };

        new WeaponUpgradeTable(category, WEAPON_UPGRADABLE_TABLE, RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
                .register(addon);
    }
}
