package com.Chagui68.weaponsaddon.items.turrets;

import com.Chagui68.weaponsaddon.items.machines.energy.EnergyManager;
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
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;

public class TurretUpgradeGUI implements Listener {
    private static final Map<UUID, TurretSession> OPEN_SESSIONS = new HashMap<>();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public static void open(
            Player player,
            String turretId,
            String turretName,
            org.bukkit.Location loc,
            double baseRange,
            double baseDamage,
            int baseCapacity,
            int baseEnergyCost
    ) {
        int currentLevel = TurretUpgradeManager.getCurrentLevel(loc);
        int maxLevel = TurretUpgradeManager.getMaxLevel(turretId);
        int energy = EnergyManager.getCharge(loc);
        String prefix = turretId.contains("ATTACK") ? "attack_tower" : "rapid_tower";

        Inventory inv = createInventory(
                null,
                54,
                ColorUtils.component("&4" + turretName + " &7Lv." + currentLevel)
        );

        ItemStack background = item(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        inv.setItem(4, item(
                Material.NETHERITE_BLOCK,
                ColorUtils.GOLD + turretName,
                "",
                ColorUtils.YELLOW + "Level: " + ColorUtils.GREEN + currentLevel + ColorUtils.GRAY + "/" + maxLevel,
                ColorUtils.YELLOW + "Energy: " + ColorUtils.AQUA + energy + " / "
                        + TurretUpgradeManager.getCapacityForLevel(baseCapacity, currentLevel) + " J",
                ColorUtils.YELLOW + "Energy/Shot: " + ColorUtils.AQUA
                        + TurretUpgradeManager.getEnergyCostForLevel(baseEnergyCost, currentLevel) + " J"
        ));

        inv.setItem(11, item(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ColorUtils.AQUA + "⬆ Progression"));
        for (int i = 0; i < 4; i++) {
            int level = i + 1;
            if (level <= maxLevel) {
                inv.setItem(
                        12 + i,
                        buildLevelCard(
                                turretId,
                                baseRange,
                                baseDamage,
                                baseCapacity,
                                baseEnergyCost,
                                level,
                                currentLevel,
                                player
                        )
                );
            }
        }
        inv.setItem(16, item(Material.CYAN_STAINED_GLASS_PANE, ColorUtils.AQUA + "📊 Stats"));

        double currentRange = TurretUpgradeManager.getRangeForLevel(baseRange, currentLevel);
        double currentDamage = TurretUpgradeManager.getDamageForLevel(baseDamage, currentLevel);

        List<String> rangeLore = new ArrayList<>();
        rangeLore.add(ColorUtils.WHITE + "Current: " + ColorUtils.GREEN + String.format("%.1f", currentRange) + " blocks");
        if (currentLevel < maxLevel) {
            rangeLore.add(ColorUtils.WHITE + "Next: " + ColorUtils.AQUA
                    + String.format("%.1f", TurretUpgradeManager.getRangeForLevel(baseRange, currentLevel + 1))
                    + " blocks");
        }
        inv.setItem(19, item(Material.ARROW, ColorUtils.AQUA + "Range", rangeLore.toArray(new String[0])));

        List<String> damageLore = new ArrayList<>();
        damageLore.add(ColorUtils.WHITE + "Current: " + ColorUtils.GREEN + String.format("%.1f", currentDamage) + " HP");
        if (currentLevel < maxLevel) {
            damageLore.add(ColorUtils.WHITE + "Next: " + ColorUtils.AQUA
                    + String.format("%.1f", TurretUpgradeManager.getDamageForLevel(baseDamage, currentLevel + 1))
                    + " HP");
        }
        inv.setItem(20, item(Material.REDSTONE, ColorUtils.RED + "Damage", damageLore.toArray(new String[0])));

        List<String> costLore = new ArrayList<>();
        costLore.add(ColorUtils.WHITE + "Current: " + ColorUtils.AQUA
                + TurretUpgradeManager.getEnergyCostForLevel(baseEnergyCost, currentLevel) + " J");
        if (currentLevel < maxLevel) {
            costLore.add(ColorUtils.WHITE + "Next: " + ColorUtils.GREEN
                    + TurretUpgradeManager.getEnergyCostForLevel(baseEnergyCost, currentLevel + 1) + " J");
        }
        inv.setItem(21, item(Material.LIGHTNING_ROD, ColorUtils.YELLOW + "Energy/Shot", costLore.toArray(new String[0])));

        List<String> capacityLore = new ArrayList<>();
        capacityLore.add(ColorUtils.WHITE + "Current: " + ColorUtils.AQUA
                + TurretUpgradeManager.getCapacityForLevel(baseCapacity, currentLevel) + " J");
        if (currentLevel < maxLevel) {
            capacityLore.add(ColorUtils.WHITE + "Next: " + ColorUtils.GREEN
                    + TurretUpgradeManager.getCapacityForLevel(baseCapacity, currentLevel + 1) + " J");
        }
        inv.setItem(22, item(Material.ENDER_CHEST, ColorUtils.LIGHT_PURPLE + "Capacity", capacityLore.toArray(new String[0])));

        inv.setItem(23, item(
                Material.EXPERIENCE_BOTTLE,
                ColorUtils.LIGHT_PURPLE + "Player XP",
                "",
                ColorUtils.WHITE + "Your XP: " + ColorUtils.GREEN + player.getLevel()
        ));

        if (currentLevel < maxLevel) {
            TurretUpgradeManager.UpgradeRequirement requirement =
                    TurretUpgradeManager.getRequirementForLevel(turretId, currentLevel);

            if (requirement != null) {
                boolean hasSpace = TurretUpgradeManager.hasSpaceForUpgrade(loc, prefix, currentLevel);
                boolean canUpgrade = TurretUpgradeManager.canUpgrade(player, turretId, currentLevel);

                inv.setItem(27, item(
                        Material.RED_STAINED_GLASS_PANE,
                        ColorUtils.RED + "Requirements for Level " + (currentLevel + 1)
                ));

                List<String> xpLore = new ArrayList<>();
                xpLore.add(ColorUtils.WHITE + "Needed: " + ColorUtils.GOLD + requirement.xpLevels);
                xpLore.add(player.getLevel() >= requirement.xpLevels
                        ? ColorUtils.GREEN + "You have enough ✓"
                        : ColorUtils.RED + "You need " + (requirement.xpLevels - player.getLevel()) + " more");
                inv.setItem(29, item(Material.EXPERIENCE_BOTTLE, ColorUtils.LIGHT_PURPLE + "XP Levels", xpLore.toArray(new String[0])));

                int slot = 30;
                for (ItemStack required : requirement.items) {
                    boolean has = TurretUpgradeManager.hasItemInInventory(player, required);
                    String itemName = itemDisplayName(required);

                    List<String> itemLore = new ArrayList<>();
                    itemLore.add(ColorUtils.WHITE + "Amount: " + ColorUtils.GOLD + required.getAmount());
                    itemLore.add(has ? ColorUtils.GREEN + "You have it ✓" : ColorUtils.RED + "Missing ✗");
                    inv.setItem(slot, item(required.getType(), ColorUtils.GOLD + itemName, itemLore.toArray(new String[0])));
                    slot++;
                }

                List<String> spaceLore = new ArrayList<>();
                spaceLore.add(hasSpace
                        ? ColorUtils.GREEN + "Space available ✓"
                        : ColorUtils.RED + "The next tower level would collide with another block!");
                inv.setItem(34, item(Material.OAK_SAPLING, ColorUtils.GREEN + "Growth Space", spaceLore.toArray(new String[0])));

                Material upgradeMaterial = canUpgrade && hasSpace
                        ? Material.LIME_STAINED_GLASS_PANE
                        : Material.RED_STAINED_GLASS_PANE;
                String upgradeColor = canUpgrade && hasSpace ? ColorUtils.GREEN : ColorUtils.RED;
                String upgradeText = canUpgrade && hasSpace ? "Click to Upgrade!" : "Requirements not met";

                inv.setItem(40, item(
                        upgradeMaterial,
                        upgradeColor + "⬆ UPGRADE TO LEVEL " + (currentLevel + 1),
                        "",
                        ColorUtils.GRAY + "The tower will grow taller",
                        upgradeColor + upgradeText
                ));
            }
        } else {
            inv.setItem(40, item(
                    Material.NETHER_STAR,
                    ColorUtils.GOLD + "MAX LEVEL",
                    "",
                    ColorUtils.GREEN + "This turret is fully upgraded!"
            ));
        }

        inv.setItem(49, item(Material.BARRIER, ColorUtils.RED + "✖ Close"));
        OPEN_SESSIONS.put(
                player.getUniqueId(),
                new TurretSession(turretId, turretName, loc, baseRange, baseDamage, baseCapacity, baseEnergyCost)
        );
        player.openInventory(inv);
    }

    private static ItemStack buildLevelCard(
            String turretId,
            double baseRange,
            double baseDamage,
            int baseCapacity,
            int baseEnergyCost,
            int level,
            int currentLevel,
            Player player
    ) {
        boolean upgraded = level < currentLevel;
        boolean current = level == currentLevel;
        Material material = current
                ? Material.GOLD_BLOCK
                : (upgraded ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE);
        String color = current ? ColorUtils.GOLD : (upgraded ? ColorUtils.GREEN : ColorUtils.DARK_GRAY);
        String name = color + "Lv." + level
                + (current ? " ◀ CURRENT" : (upgraded ? " ✓ UPGRADED" : " 🔒 LOCKED"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.WHITE + "Range: " + ColorUtils.GREEN
                + String.format("%.1f", TurretUpgradeManager.getRangeForLevel(baseRange, level)) + " blocks");
        lore.add(ColorUtils.WHITE + "Damage: " + ColorUtils.GREEN
                + String.format("%.1f", TurretUpgradeManager.getDamageForLevel(baseDamage, level)) + " HP");
        lore.add(ColorUtils.WHITE + "Capacity: " + ColorUtils.GREEN
                + TurretUpgradeManager.getCapacityForLevel(baseCapacity, level) + " J");
        lore.add(ColorUtils.WHITE + "Energy/Shot: " + ColorUtils.GREEN
                + TurretUpgradeManager.getEnergyCostForLevel(baseEnergyCost, level) + " J");

        if (level > 1) {
            TurretUpgradeManager.UpgradeRequirement requirement =
                    TurretUpgradeManager.getRequirementForLevel(turretId, level - 1);
            if (requirement != null) {
                lore.add("");
                lore.add(ColorUtils.GOLD + "Cost to reach this level:");
                lore.add(ColorUtils.YELLOW + "XP: " + ColorUtils.WHITE + requirement.xpLevels);
                for (ItemStack required : requirement.items) {
                    String itemName = itemDisplayName(required);
                    boolean has = player != null && TurretUpgradeManager.hasItemInInventory(player, required);
                    lore.add(ColorUtils.GRAY + "- " + ColorUtils.WHITE + itemName + " x" + required.getAmount()
                            + (has ? ColorUtils.GREEN + " ✓" : ColorUtils.RED + " ✗"));
                }
            }
        }

        return item(material, name, lore.toArray(new String[0]));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        TurretSession session = OPEN_SESSIONS.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot != 40) {
            return;
        }

        int currentLevel = TurretUpgradeManager.getCurrentLevel(session.loc);
        int maxLevel = TurretUpgradeManager.getMaxLevel(session.turretId);
        if (currentLevel >= maxLevel) {
            return;
        }

        if (!TurretUpgradeManager.canUpgrade(player, session.turretId, currentLevel)) {
            player.sendMessage(ColorUtils.RED + "You don't meet the upgrade requirements!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }

        String prefix = session.turretId.contains("ATTACK") ? "attack_tower" : "rapid_tower";
        if (!TurretUpgradeManager.hasSpaceForUpgrade(session.loc, prefix, currentLevel)) {
            player.sendMessage(ColorUtils.RED + "The next turret level would collide with another block!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }

        if (!TurretUpgradeManager.applyUpgrade(player, session.loc, session.turretId, prefix)) {
            player.sendMessage(ColorUtils.RED + "The turret upgrade could not be applied safely.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        open(
                player,
                session.turretId,
                session.turretName,
                session.loc,
                session.baseRange,
                session.baseDamage,
                session.baseCapacity,
                session.baseEnergyCost
        );
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            OPEN_SESSIONS.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player player
                && OPEN_SESSIONS.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    private static String itemDisplayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
            return PLAIN.serialize(meta.displayName());
        }
        return formatMaterialName(item.getType());
    }

    private static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        return sb.toString().trim();
    }

    private static ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ColorUtils.component(name));
        if (lore.length > 0) {
            meta.lore(java.util.Arrays.stream(lore).map(ColorUtils::component).toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static final class TurretSession {
        private final String turretId;
        private final String turretName;
        private final org.bukkit.Location loc;
        private final double baseRange;
        private final double baseDamage;
        private final int baseCapacity;
        private final int baseEnergyCost;

        private TurretSession(
                String turretId,
                String turretName,
                org.bukkit.Location loc,
                double baseRange,
                double baseDamage,
                int baseCapacity,
                int baseEnergyCost
        ) {
            this.turretId = turretId;
            this.turretName = turretName;
            this.loc = loc;
            this.baseRange = baseRange;
            this.baseDamage = baseDamage;
            this.baseCapacity = baseCapacity;
            this.baseEnergyCost = baseEnergyCost;
        }
    }
}
