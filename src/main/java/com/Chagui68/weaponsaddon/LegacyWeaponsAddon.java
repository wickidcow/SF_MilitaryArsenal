package com.Chagui68.weaponsaddon;

import com.Chagui68.weaponsaddon.integrations.LegacyNetworksIntegration;

/**
 * Slimefun Legacy entry point.
 *
 * <p>The upstream WeaponsAddon bootstrap is retained so upstream gameplay updates can be
 * synchronized with minimal conflicts. Legacy-only compatibility bridges are started here
 * after upstream registration has completed.</p>
 */
public final class LegacyWeaponsAddon extends WeaponsAddon {

    @Override
    public void onEnable() {
        super.onEnable();
        LegacyNetworksIntegration.register();
    }
}
