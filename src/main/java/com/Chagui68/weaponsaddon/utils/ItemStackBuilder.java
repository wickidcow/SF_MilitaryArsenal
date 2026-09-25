package com.Chagui68.weaponsaddon.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public final class ItemStackBuilder {
    private ItemStackBuilder() {
    }

    public static ItemStack create(Material material, String displayName, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ColorUtils.component(displayName));
        if (lore.length > 0) {
            meta.lore(java.util.Arrays.stream(lore).map(ColorUtils::component).toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack create(ItemStack item, int amount) {
        ItemStack stack = item.clone();
        stack.setAmount(amount);
        return stack;
    }
}
