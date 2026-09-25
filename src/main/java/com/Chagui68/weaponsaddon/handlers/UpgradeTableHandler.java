package com.Chagui68.weaponsaddon.handlers;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
import com.Chagui68.weaponsaddon.items.machines.energy.EnergyManager;
import com.Chagui68.weaponsaddon.utils.ColorUtils;
import com.Chagui68.weaponsaddon.utils.WeaponUtils;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.createInventory;

public class UpgradeTableHandler implements Listener {

    private static final int UPGRADE_COST = 50000;
    private static final int MAX_UPGRADE_LEVEL = 5;
    private static final double DAMAGE_PER_LEVEL = 2.0; // +2 damage per level
    private static final double SPEED_PER_LEVEL = 0.2; // +0.15 speed per level
    private static final Map<UUID, Location> openTables = new HashMap<>();

    private static final Component TITLE = ColorUtils.component("&8Weapon Upgrade Table");
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    // Valid weapon/tool materials
    private static final Set<Material> VALID_UPGRADE_ITEMS = EnumSet.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
            Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.BOW, Material.CROSSBOW, Material.TRIDENT);

    // Base damage values
    private static final Map<Material, Double> BASE_DAMAGE = new HashMap<>();
    static {
        BASE_DAMAGE.put(Material.WOODEN_SWORD, 4.0);
        BASE_DAMAGE.put(Material.GOLDEN_SWORD, 4.0);
        BASE_DAMAGE.put(Material.STONE_SWORD, 5.0);
        BASE_DAMAGE.put(Material.IRON_SWORD, 6.0);
        BASE_DAMAGE.put(Material.DIAMOND_SWORD, 7.0);
        BASE_DAMAGE.put(Material.NETHERITE_SWORD, 8.0);
        BASE_DAMAGE.put(Material.WOODEN_AXE, 7.0);
        BASE_DAMAGE.put(Material.GOLDEN_AXE, 7.0);
        BASE_DAMAGE.put(Material.STONE_AXE, 9.0);
        BASE_DAMAGE.put(Material.IRON_AXE, 9.0);
        BASE_DAMAGE.put(Material.DIAMOND_AXE, 9.0);
        BASE_DAMAGE.put(Material.NETHERITE_AXE, 10.0);
        BASE_DAMAGE.put(Material.TRIDENT, 9.0);
        BASE_DAMAGE.put(Material.DIAMOND_HOE, 5.0); // Machine Gun
        BASE_DAMAGE.put(Material.NETHERITE_SWORD, 8.0); // Base Netherite Sword
    }

    // Base attack speed values (Minecraft default is 4.0 for hand)
    private static final Map<Material, Double> BASE_SPEED = new HashMap<>();
    static {
        // Swords: 1.6 attack speed
        BASE_SPEED.put(Material.WOODEN_SWORD, 1.6);
        BASE_SPEED.put(Material.STONE_SWORD, 1.6);
        BASE_SPEED.put(Material.IRON_SWORD, 1.6);
        BASE_SPEED.put(Material.GOLDEN_SWORD, 1.6);
        BASE_SPEED.put(Material.DIAMOND_SWORD, 1.6);
        BASE_SPEED.put(Material.NETHERITE_SWORD, 1.6);
        // Axes: 0.8-1.0 attack speed
        BASE_SPEED.put(Material.WOODEN_AXE, 0.8);
        BASE_SPEED.put(Material.STONE_AXE, 0.8);
        BASE_SPEED.put(Material.IRON_AXE, 0.9);
        BASE_SPEED.put(Material.GOLDEN_AXE, 1.0);
        BASE_SPEED.put(Material.DIAMOND_AXE, 1.0);
        BASE_SPEED.put(Material.NETHERITE_AXE, 1.0);
        // Pickaxes: 1.2 attack speed
        BASE_SPEED.put(Material.WOODEN_PICKAXE, 1.2);
        BASE_SPEED.put(Material.STONE_PICKAXE, 1.2);
        BASE_SPEED.put(Material.IRON_PICKAXE, 1.2);
        BASE_SPEED.put(Material.GOLDEN_PICKAXE, 1.2);
        BASE_SPEED.put(Material.DIAMOND_PICKAXE, 1.2);
        BASE_SPEED.put(Material.NETHERITE_PICKAXE, 1.2);
        // Shovels: 1.0 attack speed
        BASE_SPEED.put(Material.WOODEN_SHOVEL, 1.0);
        BASE_SPEED.put(Material.STONE_SHOVEL, 1.0);
        BASE_SPEED.put(Material.IRON_SHOVEL, 1.0);
        BASE_SPEED.put(Material.GOLDEN_SHOVEL, 1.0);
        BASE_SPEED.put(Material.DIAMOND_SHOVEL, 1.0);
        BASE_SPEED.put(Material.NETHERITE_SHOVEL, 1.0);
        // Hoes: variable
        BASE_SPEED.put(Material.WOODEN_HOE, 1.0);
        BASE_SPEED.put(Material.STONE_HOE, 2.0);
        BASE_SPEED.put(Material.IRON_HOE, 3.0);
        BASE_SPEED.put(Material.GOLDEN_HOE, 1.0);
        BASE_SPEED.put(Material.DIAMOND_HOE, 4.0);
        BASE_SPEED.put(Material.NETHERITE_HOE, 4.0);
        // Trident
        BASE_SPEED.put(Material.TRIDENT, 1.1);
    }

    private static final int INFO_SLOT = 4;
    private static final int WEAPON_SLOT = 10;
    private static final int MODULE_SLOT = 16;
    private static final int UPGRADE_BUTTON = 13;
    private static final int OUTPUT_SLOT = 22;

    public static void openUpgradeGui(Player p, Location loc, int energy) {
        Inventory inv = createInventory(null, 27, TITLE);

        ItemStack darkBg = new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack lightBg = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkBg);
        }

        int[] frameSlots = { 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 23, 24, 25, 26 };
        for (int slot : frameSlots) {
            inv.setItem(slot, lightBg);
        }

        inv.setItem(11, new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6→"));
        inv.setItem(12, new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, "&e→"));
        inv.setItem(14, new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, "&e←"));
        inv.setItem(15, new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6←"));

        inv.setItem(WEAPON_SLOT, null);
        inv.setItem(MODULE_SLOT, null);
        inv.setItem(OUTPUT_SLOT, null);

        String energyColor = energy >= UPGRADE_COST ? "&a" : "&c";
        inv.setItem(INFO_SLOT, new CustomItemStack(Material.ANVIL, "&e&l⚒ Weapon Upgrade Table",
                "",
                "&7Place a &bweapon/tool &7on the left",
                "&7Place a &dmodule &7on the right",
                "&7Click &aUPGRADE &7to enhance!",
                "",
                "&6Energy: " + energyColor + formatEnergy(energy) + "&7/&e50K J",
                "",
                "&eMax upgrade level: &f5"));

        updateButton(inv, energy);

        p.openInventory(inv);
        openTables.put(p.getUniqueId(), loc);
    }

    private static String formatEnergy(int energy) {
        if (energy >= 1000000)
            return String.format("%.1fM", energy / 1000000.0);
        if (energy >= 1000)
            return String.format("%.1fK", energy / 1000.0);
        return String.valueOf(energy);
    }

    private static void updateButton(Inventory inv, int energy) {
        if (energy >= UPGRADE_COST) {
            inv.setItem(UPGRADE_BUTTON, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE,
                    "&a&l⚡ UPGRADE ⚡", "", "&7Cost: &e50,000 J", "&aClick to upgrade!"));
        } else {
            inv.setItem(UPGRADE_BUTTON, new CustomItemStack(Material.RED_STAINED_GLASS_PANE,
                    "&c&l✘ NO ENERGY ✘", "", "&7Cost: &e50,000 J", "&cInsufficient energy!"));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(TITLE))
            return;

        Player p = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        int slot = e.getRawSlot();

        if (slot >= 0 && slot < 27) {
            if (slot != WEAPON_SLOT && slot != MODULE_SLOT && slot != OUTPUT_SLOT) {
                e.setCancelled(true);
            }
            if (slot == UPGRADE_BUTTON) {
                e.setCancelled(true);
                handleUpgrade(p, inv);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!e.getView().title().equals(TITLE))
            return;

        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();

        returnItemToPlayer(p, inv.getItem(WEAPON_SLOT));
        returnItemToPlayer(p, inv.getItem(MODULE_SLOT));
        returnItemToPlayer(p, inv.getItem(OUTPUT_SLOT));
        openTables.remove(p.getUniqueId());
    }

    private void returnItemToPlayer(Player p, ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(item);
            for (ItemStack drop : leftover.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
        }
    }

    private void handleUpgrade(Player p, Inventory inv) {
        ItemStack weaponItem = inv.getItem(WEAPON_SLOT);
        ItemStack moduleItem = inv.getItem(MODULE_SLOT);
        ItemStack outputItem = inv.getItem(OUTPUT_SLOT);
        Location loc = openTables.get(p.getUniqueId());

        if (outputItem != null && outputItem.getType() != Material.AIR) {
            p.sendMessage(ColorUtils.component("&c✕ Output slot not empty!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (weaponItem == null || weaponItem.getType() == Material.AIR) {
            p.sendMessage(ColorUtils.component("&c✕ Place a weapon in the left slot!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!isValidUpgradeItem(weaponItem)) {
            p.sendMessage(ColorUtils.component("&c✕ Only weapons and tools can be upgraded!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (moduleItem == null || moduleItem.getType() == Material.AIR) {
            p.sendMessage(ColorUtils.component("&c✕ Place a module in the right slot!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (loc == null)
            return;

        int energy = EnergyManager.getCharge(loc);
        if (energy < UPGRADE_COST) {
            p.sendMessage(ColorUtils.component("&c✕ Not enough energy! Need 50,000 J"));
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
            return;
        }

        ItemStack upgradedWeapon = weaponItem.clone();
        boolean success = false;
        String upgradeType = "";

        if (MilitaryComponents.DAMAGE_MODULE_I.isSimilar(moduleItem)) {
            int currentLevel = getUpgradeLevel(upgradedWeapon, "damage");
            if (currentLevel >= MAX_UPGRADE_LEVEL) {
                p.sendMessage(ColorUtils.component("&c✕ Max damage level reached! (5/5)"));
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            success = applyDamageUpgrade(upgradedWeapon, currentLevel + 1);
            upgradeType = "DAMAGE Lv." + (currentLevel + 1);
        } else if (MilitaryComponents.SPEED_MODULE_I.isSimilar(moduleItem)) {
            int currentLevel = getUpgradeLevel(upgradedWeapon, "speed");
            if (currentLevel >= MAX_UPGRADE_LEVEL) {
                p.sendMessage(ColorUtils.component("&c✕ Max speed level reached! (5/5)"));
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            success = applySpeedUpgrade(upgradedWeapon, currentLevel + 1);
            upgradeType = "SPEED Lv." + (currentLevel + 1);
        } else {
            p.sendMessage(ColorUtils.component("&c✕ Invalid upgrade module!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (success) {
            inv.setItem(WEAPON_SLOT, null);
            moduleItem.setAmount(moduleItem.getAmount() - 1);
            inv.setItem(OUTPUT_SLOT, upgradedWeapon);
            EnergyManager.removeCharge(loc, UPGRADE_COST);

            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            p.sendMessage(ColorUtils.component("&a✓ Upgrade successful! &e" + upgradeType));

            refreshEnergyDisplay(inv, loc);
        }
    }

    private void refreshEnergyDisplay(Inventory inv, Location loc) {
        int newEnergy = EnergyManager.getCharge(loc);
        updateButton(inv, newEnergy);
        String energyColor = newEnergy >= UPGRADE_COST ? "&a" : "&c";
        inv.setItem(INFO_SLOT, new CustomItemStack(Material.ANVIL, "&e&l⚒ Weapon Upgrade Table",
                "", "&7Place a &bweapon/tool &7on the left", "&7Place a &dmodule &7on the right",
                "&7Click &aUPGRADE &7to enhance!", "",
                "&6Energy: " + energyColor + formatEnergy(newEnergy) + "&7/&e50K J", "",
                "&eMax upgrade level: &f5"));
    }

    public static int getUpgradeLevel(ItemStack item, String type) {
        if (item == null || !item.hasItemMeta())
            return 0;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(WeaponsAddon.getInstance(), "upgrade_" + type + "_level");
        return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    private boolean applyDamageUpgrade(ItemStack item, int newLevel) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;

        // Store new level
        NamespacedKey levelKey = new NamespacedKey(WeaponsAddon.getInstance(), "upgrade_damage_level");
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, newLevel);

        // Apply both attributes to ensure consistency
        applySynchronizedAttributes(meta, item.getType());

        // Hide vanilla attributes
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        // Update all lore at once
        updateWeaponLore(item);
        return true;
    }

    private void applySynchronizedAttributes(ItemMeta meta, Material type) {
        // 1. Clear ALL our military modifiers first
        removeOurModifiers(meta, Attribute.GENERIC_ATTACK_DAMAGE, "military_damage");
        removeOurModifiers(meta, Attribute.GENERIC_ATTACK_SPEED, "military_speed");

        // 1.1 Clear legacy boss modifiers if they exist (to prevent stacking)
        removeOurModifiers(meta, Attribute.GENERIC_ATTACK_DAMAGE, "att");
        removeOurModifiers(meta, Attribute.GENERIC_ATTACK_SPEED, "att_speed");

        // 2. Resolve Levels
        int dmgLevel = meta.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(WeaponsAddon.getInstance(), "upgrade_damage_level"), PersistentDataType.INTEGER, 0);
        int spdLevel = meta.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(WeaponsAddon.getInstance(), "upgrade_speed_level"), PersistentDataType.INTEGER, 0);

        // 3. Resolve Boss Bonuses
        double bossDmgBonus = meta.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(WeaponsAddon.getInstance(), "boss_damage_bonus"), PersistentDataType.DOUBLE, 0.0);

        // 4. Apply Damage
        double upgradeDmgBonus = dmgLevel * DAMAGE_PER_LEVEL;
        double totalDmgBonus = bossDmgBonus + upgradeDmgBonus;

        if (totalDmgBonus > 0) {
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(
                    new NamespacedKey(WeaponsAddon.getInstance(), "military_damage"),
                    totalDmgBonus,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND));
        }

        // 5. Apply Speed (CRITICAL: Always apply a base modifier if we hide attributes,
        // even if spdLevel is 0)
        double baseSpdModifier = getBaseSpeedModifier(type);
        double spdBonus = spdLevel * SPEED_PER_LEVEL;

        // We ALWAYS apply a speed modifier if ANY upgrade exists, to prevent reset to
        // 4.0
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(
                new NamespacedKey(WeaponsAddon.getInstance(), "military_speed"),
                baseSpdModifier + spdBonus,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND));
    }

    private double getBaseSpeedModifier(Material type) {
        String name = type.name();
        if (name.contains("SWORD"))
            return -2.4; // 1.6 total
        if (name.contains("_AXE"))
            return -3.0; // 1.0 total
        if (name.contains("_SHOVEL"))
            return -3.0; // 1.0 total
        if (name.contains("_PICKAXE"))
            return -2.8; // 1.2 total
        if (name.contains("_HOE"))
            return 0.0; // Varies, but usually fast
        return -2.4; // Default safe bet
    }

    private boolean applySpeedUpgrade(ItemStack item, int newLevel) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;

        // Store new level
        NamespacedKey levelKey = new NamespacedKey(WeaponsAddon.getInstance(), "upgrade_speed_level");
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, newLevel);

        // Apply synchronized attributes
        applySynchronizedAttributes(meta, item.getType());

        // Hide vanilla attributes
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        // Update all lore at once
        updateWeaponLore(item);
        return true;
    }

    private void removeOurModifiers(ItemMeta meta, Attribute attr, String namePrefix) {
        if (!meta.hasAttributeModifiers())
            return;
        Collection<AttributeModifier> mods = meta.getAttributeModifiers(attr);
        if (mods == null)
            return;

        for (AttributeModifier mod : new ArrayList<>(mods)) {
            if (mod.getKey().getKey().startsWith(namePrefix)) {
                meta.removeAttributeModifier(attr, mod);
            }
        }
    }

    public static void updateWeaponLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        List<Component> lore = meta.hasLore() && meta.lore() != null
                ? new ArrayList<>(meta.lore())
                : new ArrayList<>();

        lore.removeIf(line -> {
            String stripped = PLAIN.serialize(line).toLowerCase().trim();
            return stripped.isEmpty()
                    || stripped.contains("damage:")
                    || stripped.contains("speed:")
                    || stripped.contains("attack speed")
                    || stripped.contains("weapon damage")
                    || stripped.contains("velocidad de ataque")
                    || stripped.contains("upgrade:");
        });

        double totalDamage = calculateTotalDamage(item);
        double totalSpeed = calculateTotalSpeed(item);

        int dmgLevel = getUpgradeLevel(item, "damage");
        int spdLevel = getUpgradeLevel(item, "speed");
        double dmgBonusAtCurrentLevel = dmgLevel * DAMAGE_PER_LEVEL;
        double spdBonusAtCurrentLevel = spdLevel * SPEED_PER_LEVEL;

        int insertIndex = 0;
        for (int i = 0; i < lore.size(); i++) {
            String line = PLAIN.serialize(lore.get(i)).trim();
            if (!line.isEmpty()) {
                insertIndex = i + 1;
                if (line.startsWith("minecraft:") || line.equals("Unbreakable")) {
                    insertIndex = i;
                    break;
                }
            }
        }

        List<Component> newStats = new ArrayList<>();
        if (insertIndex > 0) {
            newStats.add(Component.empty());
        }

        if (dmgLevel > 0) {
            newStats.add(ColorUtils.component(
                    "&c⚔ DAMAGE: &f" + String.format("%.1f", totalDamage)
                            + (dmgBonusAtCurrentLevel > 0
                                    ? " &7(+" + String.format("%.1f", dmgBonusAtCurrentLevel) + ")"
                                    : "")));
            newStats.add(ColorUtils.component(
                    "&7Upgrade: " + createProgressBar(dmgLevel, MAX_UPGRADE_LEVEL)
                            + " &f" + dmgLevel + "/" + MAX_UPGRADE_LEVEL));
        }

        if (spdLevel > 0) {
            newStats.add(ColorUtils.component(
                    "&b⚡ SPEED: &f" + String.format("%.2f", totalSpeed)
                            + (spdBonusAtCurrentLevel > 0
                                    ? " &7(+" + String.format("%.2f", spdBonusAtCurrentLevel) + ")"
                                    : "")));
            newStats.add(ColorUtils.component(
                    "&7Upgrade: " + createProgressBar(spdLevel, MAX_UPGRADE_LEVEL)
                            + " &f" + spdLevel + "/" + MAX_UPGRADE_LEVEL));
        }

        if (insertIndex < lore.size() && !newStats.isEmpty()) {
            newStats.add(Component.empty());
        }

        lore.addAll(insertIndex, newStats);
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public static double calculateTotalSpeed(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        double speed;

        if (meta != null && meta.hasAttributeModifiers()) {
            Collection<AttributeModifier> mods = meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED);
            if (mods != null && !mods.isEmpty()) {
                speed = 4.0;
                for (AttributeModifier mod : mods) {
                    if (mod.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                        speed += mod.getAmount();
                    }
                }
                return speed;
            }
        }
        return BASE_SPEED.getOrDefault(item.getType(), 4.0);
    }

    public static String createProgressBar(int current, int max) {
        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < current; i++)
            bar.append("▮");
        bar.append("&8");
        for (int i = current; i < max; i++)
            bar.append("▯");
        return bar.toString();
    }

    public static double calculateTotalDamage(ItemStack item) {
        return WeaponUtils.calculateDamage(item, BASE_DAMAGE.getOrDefault(item.getType(), 1.0), null);
    }

    private boolean isValidUpgradeItem(ItemStack item) {
        if (item == null)
            return false;
        if (VALID_UPGRADE_ITEMS.contains(item.getType()))
            return true;
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        return sfItem != null && VALID_UPGRADE_ITEMS.contains(item.getType());
    }
}
