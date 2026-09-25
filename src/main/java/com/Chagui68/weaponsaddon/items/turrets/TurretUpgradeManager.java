package com.Chagui68.weaponsaddon.items.turrets;

import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class TurretUpgradeManager {
    private static final Map<String, UpgradeRequirement[]> UPGRADE_REQUIREMENTS = new HashMap<>();

    static {
        UPGRADE_REQUIREMENTS.put("MA_ATTACK_TURRET", new UpgradeRequirement[]{
                new UpgradeRequirement(5, new ItemStack[]{MilitaryComponents.TARGETING_SYSTEM}, 2),
                new UpgradeRequirement(10, new ItemStack[]{
                        MilitaryComponents.TARGETING_SYSTEM,
                        MilitaryComponents.ENERGY_MATRIX
                }, 4),
                new UpgradeRequirement(15, new ItemStack[]{
                        MilitaryComponents.TARGETING_SYSTEM,
                        MilitaryComponents.ENERGY_MATRIX,
                        MilitaryComponents.QUANTUM_PROCESSOR
                }, 6)
        });

        UPGRADE_REQUIREMENTS.put("MA_MACHINE_GUN_TURRET", new UpgradeRequirement[]{
                new UpgradeRequirement(5, new ItemStack[]{MilitaryComponents.MOVEMENT_CIRCUIT}, 2),
                new UpgradeRequirement(10, new ItemStack[]{
                        MilitaryComponents.MOVEMENT_CIRCUIT,
                        MilitaryComponents.KINETIC_STABILIZER
                }, 4),
                new UpgradeRequirement(15, new ItemStack[]{
                        MilitaryComponents.MOVEMENT_CIRCUIT,
                        MilitaryComponents.KINETIC_STABILIZER,
                        MilitaryComponents.QUANTUM_PROCESSOR
                }, 6)
        });
    }

    private TurretUpgradeManager() {
    }

    public static int getCurrentLevel(Location loc) {
        String levelStr = SlimefunStorageCompat.getData(loc, "turret-level");
        if (levelStr == null) {
            return 1;
        }

        try {
            return Math.max(1, Integer.parseInt(levelStr));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static void setLevel(Location loc, int level) {
        SlimefunStorageCompat.setData(loc, "turret-level", String.valueOf(Math.max(1, level)));
    }

    public static double getRangeForLevel(double baseRange, int level) {
        return baseRange + (Math.max(1, level) - 1) * 2.0;
    }

    public static double getDamageForLevel(double baseDamage, int level) {
        return baseDamage * (1.0 + (Math.max(1, level) - 1) * 0.15);
    }

    public static int getCapacityForLevel(int baseCapacity, int level) {
        return (int) Math.round(baseCapacity * (1.0 + (Math.max(1, level) - 1) * 0.25));
    }

    public static int getEnergyCostForLevel(int baseCost, int level) {
        return Math.max(1, (int) Math.round(baseCost * (1.0 - (Math.max(1, level) - 1) * 0.10)));
    }

    public static int getShotCooldownForLevel(int baseCooldown, int level) {
        return Math.max(0, baseCooldown - (Math.max(1, level) - 1));
    }

    public static UpgradeRequirement getRequirementForLevel(String turretId, int currentLevel) {
        UpgradeRequirement[] requirements = UPGRADE_REQUIREMENTS.get(turretId);
        if (requirements == null || currentLevel < 1 || currentLevel > requirements.length) {
            return null;
        }
        return requirements[currentLevel - 1];
    }

    public static int getMaxLevel(String turretId) {
        UpgradeRequirement[] requirements = UPGRADE_REQUIREMENTS.get(turretId);
        return requirements != null ? requirements.length + 1 : 1;
    }

    public static boolean canUpgrade(Player player, String turretId, int currentLevel) {
        UpgradeRequirement requirement = getRequirementForLevel(turretId, currentLevel);
        if (requirement == null || player.getLevel() < requirement.xpLevels) {
            return false;
        }

        for (ItemStack required : requirement.items) {
            if (!hasItemInInventory(player, required)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasItemInInventory(Player player, ItemStack required) {
        int needed = required.getAmount();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(required)) {
                needed -= item.getAmount();
                if (needed <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void consumeUpgradeItems(Player player, UpgradeRequirement requirement) {
        player.setLevel(player.getLevel() - requirement.xpLevels);

        for (ItemStack required : requirement.items) {
            int needed = required.getAmount();
            for (int i = 0; i < player.getInventory().getSize() && needed > 0; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item == null || !item.isSimilar(required)) {
                    continue;
                }

                int take = Math.min(item.getAmount(), needed);
                item.setAmount(item.getAmount() - take);
                needed -= take;
            }
        }
    }

    public static boolean hasSpaceForUpgrade(Location baseLoc, String prefix, int currentLevel) {
        int nextLevel = currentLevel + 1;
        String currentStructure = TurretStructureManager.getStructureName(prefix, currentLevel);
        String nextStructure = TurretStructureManager.getStructureName(prefix, nextLevel);
        return TurretStructureManager.canPlaceStructure(baseLoc, nextStructure, currentStructure);
    }

    public static boolean applyUpgrade(Player player, Location loc, String turretId, String prefix) {
        int currentLevel = getCurrentLevel(loc);
        UpgradeRequirement requirement = getRequirementForLevel(turretId, currentLevel);
        if (requirement == null || !canUpgrade(player, turretId, currentLevel)) {
            return false;
        }

        if (!hasSpaceForUpgrade(loc, prefix, currentLevel)) {
            return false;
        }

        String currentStructure = TurretStructureManager.getStructureName(prefix, currentLevel);
        String newStructure = TurretStructureManager.getStructureName(prefix, currentLevel + 1);

        TurretStructureManager.removeStructure(loc, currentStructure);
        if (!TurretStructureManager.placeStructure(loc, newStructure)) {
            // Remove any successfully placed subset before restoring the old level.
            TurretStructureManager.removeStructure(loc, newStructure);
            TurretStructureManager.placeStructure(loc, currentStructure);
            return false;
        }

        consumeUpgradeItems(player, requirement);
        setLevel(loc, currentLevel + 1);
        return true;
    }

    public static final class UpgradeRequirement {
        public final int xpLevels;
        public final ItemStack[] items;
        public final int rangeBonus;

        public UpgradeRequirement(int xpLevels, ItemStack[] items, int rangeBonus) {
            this.xpLevels = xpLevels;
            this.items = items;
            this.rangeBonus = rangeBonus;
        }
    }
}
