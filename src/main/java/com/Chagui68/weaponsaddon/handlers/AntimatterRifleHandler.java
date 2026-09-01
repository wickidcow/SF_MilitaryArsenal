package com.Chagui68.weaponsaddon.handlers;

import com.Chagui68.weaponsaddon.protection.ProtectionService;
import com.Chagui68.weaponsaddon.utils.WeaponUtils;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class AntimatterRifleHandler implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Identify the rifle by its Slimefun ID rather than its vanilla material. ItemsAdder visuals
        // intentionally replace the base material, so a Material.NETHERITE_SWORD check makes a correctly
        // textured rifle impossible to fire.
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem == null || !sfItem.getId().equals("MA_ANTIMATTER_RIFLE")) {
            return;
        }

        Player p = event.getPlayer();
        if (event.getClickedBlock() != null && !ProtectionService.canUse(p, event.getClickedBlock().getLocation())) {
            ProtectionService.deny(p, "weapon use");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        UUID playerId = p.getUniqueId();
        long now = System.currentTimeMillis();
        long cooldownUntil = cooldowns.getOrDefault(playerId, 0L);
        if (cooldownUntil > now) {
            p.sendMessage(ChatColor.RED + "⚠ Rifle cooling down...");
            return;
        }

        Vector direction = p.getEyeLocation().getDirection();
        RayTraceResult result = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                direction,
                50.0,
                0.5,
                entity -> entity instanceof LivingEntity && entity != p);

        if (result != null && result.getHitEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) result.getHitEntity();
            if (!ProtectionService.canDamage(p, target)) {
                ProtectionService.deny(p, "weapon damage");
                return;
            }

            for (double i = 0; i < result.getHitPosition().distance(p.getEyeLocation().toVector()); i += 0.5) {
                Vector point = p.getEyeLocation().toVector().add(direction.clone().multiply(i));
                p.getWorld().spawnParticle(Particle.CRIT,
                        point.getX(), point.getY(), point.getZ(),
                        1, 0, 0, 0, 0);
                p.getWorld().spawnParticle(Particle.SMOKE,
                        point.getX(), point.getY(), point.getZ(),
                        1, 0, 0, 0, 0.02);
                if (i % 2 == 0) {
                    p.getWorld().spawnParticle(Particle.FLASH,
                            point.getX(), point.getY(), point.getZ(),
                            1, null);
                }
            }

            target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation().add(0, 1, 0), 50, 1, 1, 1,
                    0.3);
            target.getWorld().spawnParticle(Particle.FLASH, target.getLocation().add(0, 1, 0), 10);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);

            double finalDamage = WeaponUtils.calculateDamage(item, 8.0, target);
            target.damage(finalDamage, p);

            p.sendMessage(ChatColor.DARK_RED + "☢ " + ChatColor.RED + "ANTIMATTER ANNIHILATION!");
            p.sendMessage(ChatColor.GRAY + "Target eliminated at " +
                    String.format("%.1f", result.getHitPosition().distance(p.getEyeLocation().toVector()))
                    + " blocks");

            long cooldownTicks = WeaponUtils.calculateFireInterval(item, 320L);
            cooldowns.put(playerId, now + Math.max(1L, cooldownTicks) * 50L);
        } else {
            p.sendMessage(ChatColor.RED + "✗ No target in range");
            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 1.0f);
        }
    }
}
