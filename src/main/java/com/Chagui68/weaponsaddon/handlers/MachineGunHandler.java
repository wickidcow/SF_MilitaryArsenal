package com.Chagui68.weaponsaddon.handlers;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import com.Chagui68.weaponsaddon.protection.ProtectionService;
import com.Chagui68.weaponsaddon.utils.WeaponUtils;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MachineGunHandler implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int BURST_SIZE = 5;
    private static final long COOLDOWN_TICKS = 40;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta())
            return;

        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem == null || !sfItem.getId().equals("MA_MACHINE_GUN"))
            return;

        if (event.getClickedBlock() != null && !ProtectionService.canUse(player, event.getClickedBlock().getLocation())) {
            ProtectionService.deny(player, "weapon use");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long lastUse = cooldowns.get(playerId);
            if (currentTime - lastUse < (COOLDOWN_TICKS * 50)) {
                return;
            }
        }

        if (!hasAmmo(player)) {
            player.sendMessage(ChatColor.RED + "✕ Out of ammo! Craft Machine Gun Bullets");
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 1.0f);
            return;
        }

        consumeAmmo(player);
        cooldowns.put(playerId, currentTime);
        player.sendMessage(ChatColor.GREEN + "✓ Magazine loaded (" + BURST_SIZE + " rounds)");
        fireBurst(player, item);
    }

    private void fireBurst(Player player, ItemStack item) {
        new BukkitRunnable() {
            int shotsFired = 0;

            @Override
            public void run() {
                if (shotsFired >= BURST_SIZE) {
                    player.sendMessage(ChatColor.GREEN + "✓ Burst complete - Magazine empty");
                    cancel();
                    return;
                }

                shotsFired++;
                player.sendMessage(ChatColor.YELLOW + "🔫 Firing burst... [" + shotsFired + "/" + BURST_SIZE + "]");

                Location eyeLoc = player.getEyeLocation();
                Vector direction = eyeLoc.getDirection();

                player.getWorld().playSound(eyeLoc, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.5f);
                eyeLoc.getWorld().spawnParticle(Particle.FLAME, eyeLoc, 5, 0.1, 0.1, 0.1, 0.02);
                eyeLoc.getWorld().spawnParticle(Particle.SMOKE, eyeLoc, 10, 0.1, 0.1, 0.1, 0.05);

                Location currentLoc = eyeLoc.clone();
                for (int i = 0; i < 50; i++) {
                    currentLoc.add(direction.clone().multiply(0.5));

                    if (i % 5 == 0) {
                        currentLoc.getWorld().spawnParticle(Particle.CRIT, currentLoc, 1);
                    }

                    for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 0.5, 0.5, 0.5)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;
                            if (!ProtectionService.canDamage(player, target)) {
                                ProtectionService.deny(player, "weapon damage");
                                return;
                            }

                            double finalDamage = WeaponUtils.calculateDamage(item, 5.0, target);
                            target.setNoDamageTicks(0);
                            target.damage(finalDamage, player);
                            target.getWorld().spawnParticle(Particle.ENCHANTED_HIT,
                                    target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3);
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT, 1.0f, 1.0f);
                            player.sendMessage(ChatColor.RED + "✕ HIT! -" + String.format("%.1f", finalDamage) + " HP");
                            return;
                        }
                    }

                    if (currentLoc.getBlock().getType().isSolid()) {
                        currentLoc.getWorld().spawnParticle(Particle.BLOCK, currentLoc, 10, 0.2, 0.2, 0.2,
                                0.1, currentLoc.getBlock().getBlockData());
                        currentLoc.getWorld().playSound(currentLoc, Sound.BLOCK_STONE_HIT, 1.0f, 1.0f);
                        return;
                    }
                }
            }
        }.runTaskTimer(WeaponsAddon.getInstance(), 0L, WeaponUtils.calculateFireInterval(item, 2L));
    }

    private boolean hasAmmo(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                SlimefunItem sfItem = SlimefunItem.getByItem(item);
                if (sfItem != null && sfItem.getId().equals("MA_MACHINE_GUN_AMMO")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void consumeAmmo(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                SlimefunItem sfItem = SlimefunItem.getByItem(item);
                if (sfItem != null && sfItem.getId().equals("MA_MACHINE_GUN_AMMO")) {
                    item.setAmount(item.getAmount() - 1);
                    return;
                }
            }
        }
    }
}
