package com.Chagui68.weaponsaddon.items.machines;

import com.Chagui68.weaponsaddon.items.AntimatterRifle;
import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
import com.Chagui68.weaponsaddon.items.vouchers.MilitaryVouchers;
import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.api.SlimefunAddon;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockUseHandler;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockPlaceHandler;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import com.Chagui68.weaponsaddon.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class AntimatterRitual extends SlimefunItem {

    public static final SlimefunItemStack ANTIMATTER_RITUAL_CORE = new SlimefunItemStack(
            "MA_ANTIMATTER_RITUAL_CORE",
            Material.BEACON,
            "&4☢ &fAntimatter Ritual Core",
            "",
            "&7Central trigger for 9×9 ritual array",
            "&7Place catalysts on 16 pedestals",
            "&7Right-click to start ritual",
            "",
            "&6Required Structure:",
            "&7- 9×9 iron block border",
            "&7- 4 netherite blocks (cross pattern)",
            "&7- 16 Antimatter Pedestals (odd positions)",
            "&7- Crying obsidian filler",
            "",
            "&c⚠ Ritual destroys the altar after use",
            "&eClick core to begin annihilation process");

    private static class RitualStructure {
        private static boolean validateStructure(Block core, Player p) {

            Material[][] expected = {
                    { Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK,
                            Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK,
                            Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, null, Material.CRYING_OBSIDIAN, null, Material.CRYING_OBSIDIAN, null,
                            Material.CRYING_OBSIDIAN, null, Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN,
                            Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN,
                            Material.CRYING_OBSIDIAN, Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, null, Material.CRYING_OBSIDIAN, null, Material.NETHERITE_BLOCK, null,
                            Material.CRYING_OBSIDIAN, null, Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN, Material.NETHERITE_BLOCK,
                            Material.BEACON, Material.NETHERITE_BLOCK, Material.CRYING_OBSIDIAN,
                            Material.CRYING_OBSIDIAN, Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, null, Material.CRYING_OBSIDIAN, null, Material.NETHERITE_BLOCK, null,
                            Material.CRYING_OBSIDIAN, null, Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN,
                            Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN, Material.CRYING_OBSIDIAN,
                            Material.CRYING_OBSIDIAN, Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, null, Material.CRYING_OBSIDIAN, null, Material.CRYING_OBSIDIAN, null,
                            Material.CRYING_OBSIDIAN, null, Material.IRON_BLOCK },
                    { Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK,
                            Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK, Material.IRON_BLOCK,
                            Material.IRON_BLOCK }
            };

            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    // Saltear el centro donde está el beacon clickeado
                    if (row == 4 && col == 4) {
                        continue;
                    }

                    int offsetX = col - 4;
                    int offsetZ = row - 4;
                    Block checkBlock = core.getRelative(offsetX, 0, offsetZ);
                    Material expectedMat = expected[row][col];

                    if (expectedMat == null) {
                        SlimefunItem sfItem = SlimefunStorageCompat.getItem(checkBlock);
                        if (sfItem == null || !sfItem.getId().equals("MA_ANTIMATTER_PEDESTAL")) {
                            p.sendMessage(ColorUtils.component("&cMissing pedestal at Row:" + row + " Col:" + col));
                            p.sendMessage(ColorUtils.component("&7Position: X:" + checkBlock.getX() + " Y:" + checkBlock.getY() + " Z:" + checkBlock.getZ()));
                            p.sendMessage(ColorUtils.component("&7Found: " + (sfItem != null ? sfItem.getId() : checkBlock.getType())));
                            return false;
                        }
                    } else {
                        if (checkBlock.getType() != expectedMat) {
                            p.sendMessage(ColorUtils.component("&cWrong block at Row:" + row + " Col:" + col));
                            p.sendMessage(ColorUtils.component("&7Position: X:" + checkBlock.getX() + " Y:" + checkBlock.getY() + " Z:" + checkBlock.getZ()));
                            p.sendMessage(ColorUtils.component("&7Expected: " + expectedMat + " | Found: " + checkBlock.getType()));
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        private static void destroyRitual(Block core) {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int offsetX = col - 4;
                    int offsetZ = row - 4;
                    Block checkBlock = core.getRelative(offsetX, 0, offsetZ);

                    // Efecto de partículas antes de destruir
                    checkBlock.getWorld().spawnParticle(
                            Particle.SOUL_FIRE_FLAME,
                            checkBlock.getLocation().add(0.5, 0.5, 0.5),
                            10, 0.3, 0.3, 0.3, 0.05);

                    // Remove Slimefun block data before destroying the ritual block
                    SlimefunStorageCompat.clear(checkBlock);

                    // Destruir el bloque
                    checkBlock.setType(Material.AIR);
                }
            }

            // Efecto final en el centro
            core.getWorld().spawnParticle(Particle.EXPLOSION, core.getLocation().add(0.5, 0.5, 0.5), 20, 2, 2, 2, 0.1);
            core.getWorld().playSound(core.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);
        }
    }

    public AntimatterRitual(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                Block b = e.getBlock();
                SlimefunStorageCompat.setData(b, "id", "MA_ANTIMATTER_RITUAL_CORE");
            }
        });

        addItemHandler((BlockUseHandler) e -> {
            e.cancel();
            Player p = e.getPlayer();
            Block block = e.getClickedBlock().get();

            if (!RitualStructure.validateStructure(block, p)) {
                p.sendMessage(ColorUtils.component("&4☢ &cInvalid ritual structure!"));
                p.sendMessage(ColorUtils.component("&7Check the required pattern"));
                return;
            }

            p.sendMessage(ColorUtils.component("&4☢ &fAntimatter Ritual &4ACTIVATED"));
            p.sendMessage(ColorUtils.component("&7Annihilating matter... &c⚠"));
            p.getInventory().addItem(AntimatterRifle.ANTIMATTER_RIFLE.clone());
            p.sendMessage(ColorUtils.component("&a✓ &fAntimatter Rifle created successfully!"));
            p.sendMessage(
                    ColorUtils.component("&7Right-click entities for instant annihilation"));

            // Destruir el altar después de usarlo
            RitualStructure.destroyRitual(block);
            p.sendMessage(ColorUtils.component("&c⚠ &7Ritual altar consumed by antimatter"));
        });
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[] {
                MilitaryVouchers.VOUCHER_TANK_PART, MilitaryComponents.TUNGSTEN_INGOT, new ItemStack(Material.IRON_BLOCK),
                MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.VOID_CORE_MACHINE, MilitaryComponents.TUNGSTEN_INGOT,
                new ItemStack(Material.IRON_BLOCK), MilitaryComponents.TUNGSTEN_INGOT, MilitaryVouchers.VOUCHER_TANK_PART
        };

        new AntimatterRitual(category, ANTIMATTER_RITUAL_CORE, RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
                .register(addon);
    }
}
