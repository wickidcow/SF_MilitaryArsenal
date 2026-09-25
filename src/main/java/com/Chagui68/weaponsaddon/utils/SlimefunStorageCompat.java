package com.Chagui68.weaponsaddon.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Runtime-neutral Slimefun block-data access.
 *
 * <p>Current Slimefun Legacy uses its block-data controller. Older Gugu-compatible
 * runtimes fall back to the historical BlockStorage facade reflectively so this addon
 * does not compile against deprecated storage APIs.</p>
 */
public final class SlimefunStorageCompat {

    private static final String SLIMEFUN_CLASS =
            "io.github.thebusybiscuit.slimefun4.implementation.Slimefun";
    private static final String LEGACY_STORAGE_CLASS =
            "me.mrCookieSlime.Slimefun.api.BlockStorage";

    private SlimefunStorageCompat() {
    }

    public static String getData(Location location, String key) {
        if (location == null || key == null) {
            return null;
        }

        String modern = modernGet(location, key);
        return modern != null ? modern : legacyGet(location, key);
    }

    public static void setData(Location location, String key, String value) {
        if (location == null || key == null) {
            return;
        }

        if (!modernSet(location, key, value)) {
            legacySet(location, key, value);
        }
    }

    public static void setData(Block block, String key, String value) {
        if (block != null) {
            setData(block.getLocation(), key, value);
        }
    }

    public static boolean is(Location location, String slimefunId) {
        return slimefunId != null && slimefunId.equals(getData(location, "id"));
    }

    public static SlimefunItem getItem(Location location) {
        String id = getData(location, "id");
        return id == null ? null : SlimefunItem.getById(id);
    }

    public static SlimefunItem getItem(Block block) {
        return block == null ? null : getItem(block.getLocation());
    }

    public static void clear(Location location) {
        if (location == null) {
            return;
        }

        if (!modernClear(location)) {
            legacyClear(location);
        }
    }

    public static void clear(Block block) {
        if (block != null) {
            clear(block.getLocation());
        }
    }

    private static Object modernController()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> slimefun = Class.forName(SLIMEFUN_CLASS, false, SlimefunStorageCompat.class.getClassLoader());
        Method getDatabaseManager = slimefun.getMethod("getDatabaseManager");
        Object database = getDatabaseManager.invoke(null);
        if (database == null) {
            return null;
        }
        return database.getClass().getMethod("getBlockDataController").invoke(database);
    }

    private static Object modernData(Location location)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object controller = modernController();
        if (controller == null) {
            return null;
        }

        Object data = controller.getClass().getMethod("getBlockData", Location.class).invoke(controller, location);
        if (data != null) {
            try {
                Method loaded = data.getClass().getMethod("isDataLoaded");
                if (Boolean.FALSE.equals(loaded.invoke(data))) {
                    for (Method method : controller.getClass().getMethods()) {
                        if (method.getName().equals("loadBlockData")
                                && method.getParameterCount() == 1
                                && method.getParameterTypes()[0].isAssignableFrom(data.getClass())) {
                            method.invoke(controller, data);
                            break;
                        }
                    }
                }
            } catch (NoSuchMethodException ignored) {
                // Runtime already exposes loaded data directly.
            }
        }
        return data;
    }

    private static String modernGet(Location location, String key) {
        try {
            Object data = modernData(location);
            if (data == null) {
                return null;
            }
            if ("id".equals(key)) {
                Object value = data.getClass().getMethod("getSfId").invoke(data);
                return value instanceof String string ? string : null;
            }
            Object value = data.getClass().getMethod("getData", String.class).invoke(data, key);
            return value instanceof String string ? string : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean modernSet(Location location, String key, String value) {
        try {
            Object controller = modernController();
            if (controller == null) {
                return false;
            }

            if ("id".equals(key)) {
                controller.getClass().getMethod("createBlock", Location.class, String.class)
                        .invoke(controller, location, value);
                return true;
            }

            Object data = modernData(location);
            if (data == null) {
                return false;
            }

            if (value == null) {
                data.getClass().getMethod("removeData", String.class).invoke(data, key);
            } else {
                data.getClass().getMethod("setData", String.class, String.class).invoke(data, key, value);
            }
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean modernClear(Location location) {
        try {
            Object controller = modernController();
            if (controller == null) {
                return false;
            }
            controller.getClass().getMethod("removeBlock", Location.class).invoke(controller, location);
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static void legacyClear(Location location) {
        try {
            Class<?> storage = Class.forName(
                    LEGACY_STORAGE_CLASS,
                    false,
                    SlimefunStorageCompat.class.getClassLoader());

            try {
                storage.getMethod("clearBlockInfo", Location.class).invoke(null, location);
                return;
            } catch (NoSuchMethodException ignored) {
                // Older facades may expose the Block overload.
            }

            storage.getMethod("clearBlockInfo", Block.class).invoke(null, location.getBlock());
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // No compatible storage facade is available.
        }
    }

    private static String legacyGet(Location location, String key) {
        try {
            Class<?> storage = Class.forName(
                    LEGACY_STORAGE_CLASS,
                    false,
                    SlimefunStorageCompat.class.getClassLoader());
            Object value = storage.getMethod("getLocationInfo", Location.class, String.class)
                    .invoke(null, location, key);
            return value instanceof String string ? string : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static void legacySet(Location location, String key, String value) {
        try {
            Class<?> storage = Class.forName(
                    LEGACY_STORAGE_CLASS,
                    false,
                    SlimefunStorageCompat.class.getClassLoader());

            try {
                storage.getMethod("addBlockInfo", Location.class, String.class, String.class)
                        .invoke(null, location, key, value == null ? "" : value);
                return;
            } catch (NoSuchMethodException ignored) {
                // Older facades expose the Block overload.
            }

            storage.getMethod("addBlockInfo", Block.class, String.class, String.class)
                    .invoke(null, location.getBlock(), key, value == null ? "" : value);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // No compatible storage facade is available.
        }
    }
}
