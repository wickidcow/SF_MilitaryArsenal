package com.Chagui68.weaponsaddon;

/**
 * Slimefun Legacy entry point.
 *
 * <p>The upstream WeaponsAddon bootstrap is retained unchanged so upstream gameplay updates can
 * be synchronized with minimal conflicts. Legacy-only startup coordination lives here.</p>
 */
public final class LegacyWeaponsAddon extends WeaponsAddon {

    private boolean enableStarted;

    @Override
    public void onEnable() {
        // Load the Legacy config early so optional integrations can decide whether startup
        // should be deferred before the upstream bootstrap registers any Slimefun items.
        saveDefaultConfig();
        reloadConfig();

        ItemsAdderIntegration.runWhenReady(this, this::finishEnable);
    }

    private void finishEnable() {
        if (enableStarted || !isEnabled()) {
            return;
        }

        enableStarted = true;
        ItemsAdderIntegration.applyVisuals(this);
        super.onEnable();
    }
}
