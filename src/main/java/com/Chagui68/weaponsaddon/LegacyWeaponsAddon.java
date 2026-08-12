package com.Chagui68.weaponsaddon;

/**
 * Slimefun Legacy entry point.
 *
 * <p>The upstream WeaponsAddon bootstrap is retained unchanged so upstream gameplay updates can
 * be synchronized with minimal conflicts. Networks compatibility is handled through plugin load
 * ordering: Military Arsenal registers its Slimefun items before Networks snapshots supported
 * addon recipes.</p>
 */
public final class LegacyWeaponsAddon extends WeaponsAddon {
}
