package com.Chagui68.weaponsaddon.integrations;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Optional Networks compatibility for the Slimefun Legacy build.
 *
 * <p>Networks is deliberately accessed through reflection so it remains a soft dependency.
 * If its integration API changes, Military Arsenal still starts normally and logs a warning.</p>
 */
public final class LegacyNetworksIntegration {

    private static boolean attempted;
    private static int registeredItems;
    private static int filteredItems;

    private LegacyNetworksIntegration() {
    }

    public static void register() {
        if (attempted) {
            return;
        }
        attempted = true;

        if (!Bukkit.getPluginManager().isPluginEnabled("Networks")) {
            WeaponsAddon.getInstance().getLogger().info(
                    "Networks is not installed; optional Military Arsenal integration is disabled.");
            return;
        }

        WeaponsAddon.getInstance().getLogger().info(
                "Networks detected; registering compatible Military Arsenal items...");

        try {
            registerCompatibleItems();
            WeaponsAddon.getInstance().getLogger().info(
                    "Networks integration complete: " + registeredItems
                            + " item(s) registered, " + filteredItems + " filtered.");
        } catch (ReflectiveOperationException | LinkageError e) {
            WeaponsAddon.getInstance().getLogger().warning(
                    "Networks integration API is not compatible with this Networks build; "
                            + "Military Arsenal will continue without the optional bridge. Cause: "
                            + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void registerCompatibleItems() throws ReflectiveOperationException {
        registeredItems = 0;
        filteredItems = 0;

        Class<?> itemDictionaryClass = Class.forName(
                "io.github.sefiraat.networks.integrations.ItemDictionary",
                false,
                LegacyNetworksIntegration.class.getClassLoader());
        Method registerMethod = itemDictionaryClass.getMethod(
                "registerItem", String.class, SlimefunItem.class);

        for (SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
            if (!isFromMilitaryArsenal(item)) {
                continue;
            }

            if (!isRecipeCompatible(item)) {
                filteredItems++;
                continue;
            }

            try {
                registerMethod.invoke(null, item.getId(), item);
                registeredItems++;
            } catch (ReflectiveOperationException e) {
                WeaponsAddon.getInstance().getLogger().warning(
                        "Could not register " + item.getId() + " with Networks: " + e.getMessage());
            }
        }
    }

    private static boolean isFromMilitaryArsenal(SlimefunItem item) {
        return item != null
                && item.getAddon() != null
                && item.getAddon() instanceof WeaponsAddon;
    }

    private static boolean isRecipeCompatible(SlimefunItem item) {
        ItemStack[] recipe = item.getRecipe();
        if (recipe == null || recipe.length == 0 || recipe.length > 9) {
            return false;
        }

        RecipeType recipeType = item.getRecipeType();
        return recipeType != RecipeType.ARMOR_FORGE
                && recipeType != RecipeType.MAGIC_WORKBENCH;
    }

    public static int getRegisteredItemsCount() {
        return registeredItems;
    }

    public static int getFilteredItemsCount() {
        return filteredItems;
    }
}
