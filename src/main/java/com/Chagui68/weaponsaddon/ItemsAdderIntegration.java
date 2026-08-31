package com.Chagui68.weaponsaddon;

import com.Chagui68.weaponsaddon.items.AntimatterRifle;
import com.Chagui68.weaponsaddon.items.MachineGun;
import com.Chagui68.weaponsaddon.items.MachineGunAmmo;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitTask;

/**
 * Optional ItemsAdder visual bridge for Military Arsenal.
 *
 * <p>Military Arsenal keeps ownership of Slimefun IDs, recipes and gameplay. Only the
 * visual model metadata from configured ItemsAdder items is borrowed.</p>
 */
public final class ItemsAdderIntegration {

    private static final String CONFIG_ROOT = "itemsadder.visuals";
    private static final long READY_TIMEOUT_TICKS = 20L * 30L;

    private ItemsAdderIntegration() {
    }

    public static void runWhenReady(WeaponsAddon plugin, Runnable onReady) {
        if (!plugin.getConfig().getBoolean(CONFIG_ROOT + ".enabled", true)
                || !plugin.getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
            onReady.run();
            return;
        }

        plugin.getLogger().info(
                "ItemsAdder detected. Waiting for custom item data before registering Military Arsenal items.");
        new ReadyGate(plugin, onReady).start();
    }

    public static void applyVisuals(WeaponsAddon plugin) {
        if (!plugin.getConfig().getBoolean(CONFIG_ROOT + ".enabled", true)) {
            return;
        }

        if (!plugin.getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
            return;
        }

        ConfigurationSection mappings = plugin.getConfig().getConfigurationSection(CONFIG_ROOT + ".mappings");
        if (mappings == null) {
            plugin.getLogger().warning("ItemsAdder is installed, but no Military Arsenal visual mappings are configured.");
            return;
        }

        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Method getInstance = customStackClass.getMethod("getInstance", String.class);
            Method getItemStack = customStackClass.getMethod("getItemStack");

            int applied = 0;
            for (Map.Entry<String, SlimefunItemStack> entry : supportedItems().entrySet()) {
                String militaryId = entry.getKey();
                String itemsAdderId = mappings.getString(militaryId, "").trim();
                if (itemsAdderId.isEmpty()) {
                    continue;
                }

                if (applyVisual(plugin, entry.getValue(), itemsAdderId, getInstance, getItemStack)) {
                    applied++;
                }
            }

            plugin.getLogger().info(
                    "ItemsAdder visual integration applied to " + applied + " Military Arsenal item(s).");
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "ItemsAdder was detected but its CustomStack API could not be loaded. Military Arsenal will use vanilla visuals.",
                    ex);
        }
    }

    private static boolean applyVisual(
            WeaponsAddon plugin,
            SlimefunItemStack target,
            String itemsAdderId,
            Method getInstance,
            Method getItemStack) {
        try {
            Object customStack = getInstance.invoke(null, itemsAdderId);
            if (customStack == null) {
                plugin.getLogger().warning(
                        "ItemsAdder visual '" + itemsAdderId + "' was not found for Military Arsenal item "
                                + target.getItemId() + ".");
                return false;
            }

            Object rawStack = getItemStack.invoke(customStack);
            if (!(rawStack instanceof ItemStack visual)) {
                plugin.getLogger().warning(
                        "ItemsAdder visual '" + itemsAdderId + "' did not return an ItemStack for "
                                + target.getItemId() + ".");
                return false;
            }

            ItemMeta originalMeta = target.getItemMeta();
            ItemMeta visualMeta = visual.getItemMeta();
            if (originalMeta == null || visualMeta == null) {
                plugin.getLogger().warning(
                        "ItemsAdder visual '" + itemsAdderId + "' has unusable item metadata for "
                                + target.getItemId() + ".");
                return false;
            }

            int amount = target.getAmount();
            target.setType(visual.getType());

            ItemMeta mergedMeta = target.getItemMeta();
            if (mergedMeta == null) {
                return false;
            }

            copyMilitaryMeta(originalMeta, mergedMeta);
            copyVisualModelMeta(visualMeta, mergedMeta);

            target.setItemMeta(mergedMeta);
            target.setAmount(amount);
            return true;
        } catch (IllegalAccessException | InvocationTargetException ex) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Failed to apply ItemsAdder visual '" + itemsAdderId + "' to " + target.getItemId() + ".",
                    ex);
            return false;
        }
    }

    private static void copyMilitaryMeta(ItemMeta source, ItemMeta target) {
        if (source.hasDisplayName()) {
            target.setDisplayName(source.getDisplayName());
        }
        if (source.hasLore()) {
            target.setLore(source.getLore());
        }

        target.setUnbreakable(source.isUnbreakable());

        for (ItemFlag flag : source.getItemFlags()) {
            target.addItemFlags(flag);
        }
        for (Map.Entry<Enchantment, Integer> enchantment : source.getEnchants().entrySet()) {
            target.addEnchant(enchantment.getKey(), enchantment.getValue(), true);
        }

        // Preserve Slimefun identity, Military Arsenal damage/upgrade data and any other
        // plugin-owned persistent tags without copying ItemsAdder's own identity tags.
        source.getPersistentDataContainer().copyTo(target.getPersistentDataContainer(), true);
    }

    @SuppressWarnings("deprecation")
    private static void copyVisualModelMeta(ItemMeta source, ItemMeta target) {
        if (source.hasCustomModelData()) {
            target.setCustomModelData(source.getCustomModelData());
        }

        // Newer Minecraft versions can use data components such as item_model and the
        // expanded custom-model-data component. Copy them reflectively so this remains
        // compatible with the Java 21 bytecode target while building against Paper 26.2+.
        copyOptionalMetaValue(source, target, "getItemModel", "setItemModel");
        copyOptionalMetaValue(source, target, "getCustomModelDataComponent", "setCustomModelDataComponent");
    }

    private static void copyOptionalMetaValue(ItemMeta source, ItemMeta target, String getterName, String setterName) {
        try {
            Method getter = source.getClass().getMethod(getterName);
            Object value = getter.invoke(source);
            if (value == null) {
                return;
            }

            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(setterName) || method.getParameterCount() != 1) {
                    continue;
                }
                if (method.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                    method.invoke(target, value);
                    return;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Component does not exist on this Paper API/runtime combination; the classic
            // custom-model-data path above remains available.
        }
    }

    private static Map<String, SlimefunItemStack> supportedItems() {
        Map<String, SlimefunItemStack> items = new LinkedHashMap<>();
        items.put("MA_MACHINE_GUN", MachineGun.MACHINE_GUN);
        items.put("MA_ANTIMATTER_RIFLE", AntimatterRifle.ANTIMATTER_RIFLE);
        items.put("MA_MACHINE_GUN_AMMO", MachineGunAmmo.MACHINE_GUN_AMMO);
        return items;
    }

    private static boolean isRegistryReady(WeaponsAddon plugin) {
        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");

            for (String methodName : new String[] {"getNamespacedIds", "getNamespacedIDs"}) {
                try {
                    Method method = customStackClass.getMethod(methodName);
                    Object result = method.invoke(null);
                    if (result instanceof Collection<?> collection && !collection.isEmpty()) {
                        return true;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try the next known API spelling.
                }
            }

            Method getInstance = customStackClass.getMethod("getInstance", String.class);
            ConfigurationSection mappings = plugin.getConfig().getConfigurationSection(CONFIG_ROOT + ".mappings");
            if (mappings != null) {
                for (String militaryId : supportedItems().keySet()) {
                    String itemsAdderId = mappings.getString(militaryId, "").trim();
                    if (!itemsAdderId.isEmpty() && getInstance.invoke(null, itemsAdderId) != null) {
                        return true;
                    }
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // ItemsAdder is enabled but its registry is not ready yet, or this API version
            // does not expose one of the readiness probes. The load-data event remains primary.
        }
        return false;
    }

    private static final class ReadyGate {
        private final WeaponsAddon plugin;
        private final Runnable onReady;
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final Listener listener = new Listener() { };
        private BukkitTask pollTask;
        private BukkitTask timeoutTask;

        private ReadyGate(WeaponsAddon plugin, Runnable onReady) {
            this.plugin = plugin;
            this.onReady = onReady;
        }

        @SuppressWarnings("unchecked")
        private void start() {
            try {
                Class<?> rawEventClass = Class.forName("dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent");
                if (Event.class.isAssignableFrom(rawEventClass)) {
                    EventExecutor executor = (ignoredListener, event) -> signalReady("ItemsAdder load-data event");
                    plugin.getServer().getPluginManager().registerEvent(
                            (Class<? extends Event>) rawEventClass,
                            listener,
                            EventPriority.MONITOR,
                            executor,
                            plugin);
                }
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().warning(
                        "ItemsAdder load-data event class was not found; using registry readiness probing instead.");
            }

            if (isRegistryReady(plugin)) {
                signalReady("ItemsAdder registry probe");
                return;
            }

            pollTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (isRegistryReady(plugin)) {
                    signalReady("ItemsAdder registry probe");
                }
            }, 10L, 10L);

            timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, this::signalTimeout, READY_TIMEOUT_TICKS);
        }

        private void signalReady(String source) {
            if (!completed.compareAndSet(false, true)) {
                return;
            }

            cleanup();
            plugin.getLogger().info(source + " confirmed custom item data is ready; continuing Military Arsenal startup.");
            Bukkit.getScheduler().runTask(plugin, onReady);
        }

        private void signalTimeout() {
            if (!completed.compareAndSet(false, true)) {
                return;
            }

            cleanup();
            plugin.getLogger().warning(
                    "Timed out waiting 30 seconds for ItemsAdder custom item data. Continuing with fallback visuals.");
            onReady.run();
        }

        private void cleanup() {
            HandlerList.unregisterAll(listener);
            if (pollTask != null) {
                pollTask.cancel();
            }
            if (timeoutTask != null) {
                timeoutTask.cancel();
            }
        }
    }
}
