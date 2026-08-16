package com.Chagui68.weaponsaddon.integrations;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.implementation.Slimefun;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

import static org.bukkit.Bukkit.getPluginManager;

/**
 * Integración con el addon Networks de Slimefun usando reflexión.
 * Registra automáticamente los items del addon Military Arsenal en Networks,
 * pero filtra las recetas con grids mayores a 3x3 para evitar
 * incompatibilidades.
 */
public class NetworksIntegration {

    private static int registeredItems = 0;
    private static int filteredItems = 0;

    /**
     * Registra la integración con Networks si está disponible.
     * Este metodo debe llamarse después de que todos los items del addon
     * hayan sido registrados en Slimefun.
     */
    public static void register() {
        if (!getPluginManager().isPluginEnabled("Networks")) {
            WeaponsAddon.getInstance().getLogger().info("Networks no está instalado, saltando integración.");
            return;
        }

        WeaponsAddon.getInstance().getLogger().info("Networks detectado, registrando items compatibles...");

        try {
            registerCompatibleItems();
            WeaponsAddon.getInstance().getLogger().info(
                    String.format(
                            "Integración con Networks completada: %d items registrados, %d items filtrados (recetas > 3x3)",
                            registeredItems, filteredItems));
        } catch (Exception e) {
            WeaponsAddon.getInstance().getLogger().warning(
                    "Error al integrar con Networks: " + e.getMessage());
        }
    }

    private static void registerCompatibleItems() throws Exception {
        registeredItems = 0;
        filteredItems = 0;

        Class<?> itemDictionaryClass = Class.forName("io.github.sefiraat.networks.integrations.ItemDictionary");
        Method registerMethod = itemDictionaryClass.getMethod("registerItem", String.class, SlimefunItem.class);

        for (SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
            if (!isFromMilitaryArsenal(item)) {
                continue;
            }

            if (isRecipeCompatible(item)) {
                try {
                    registerMethod.invoke(null, item.getId(), item);
                    registeredItems++;
                } catch (Exception e) {
                    WeaponsAddon.getInstance().getLogger().warning(
                            "Error al registrar " + item.getId() + " en Networks: " + e.getMessage());
                }
            } else {
                filteredItems++;
                WeaponsAddon.getInstance().getLogger().fine(
                        "Item filtrado (receta > 3x3): " + item.getId());
            }
        }
    }

    private static boolean isFromMilitaryArsenal(SlimefunItem item) {
        if (item == null || item.getAddon() == null) {
            return false;
        }
        return item.getAddon() instanceof WeaponsAddon;
    }

    private static boolean isRecipeCompatible(SlimefunItem item) {
        ItemStack[] recipe = item.getRecipe();

        if (recipe == null || recipe.length == 0) {
            return false;
        }

        if (recipe.length > 9) {
            return false;
        }

        RecipeType recipeType = item.getRecipeType();
        if (recipeType == RecipeType.ARMOR_FORGE || recipeType == RecipeType.MAGIC_WORKBENCH) {
            return false;
        }

        return true;
    }

    public static int getRegisteredItemsCount() {
        return registeredItems;
    }

    public static int getFilteredItemsCount() {
        return filteredItems;
    }
}
