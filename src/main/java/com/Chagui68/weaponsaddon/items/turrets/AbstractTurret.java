package com.Chagui68.weaponsaddon.items.turrets;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import com.Chagui68.weaponsaddon.items.CustomRecipeItem;
import com.Chagui68.weaponsaddon.items.machines.energy.EnergyManager;
import com.Chagui68.weaponsaddon.protection.ProtectionService;
import com.Chagui68.weaponsaddon.utils.TurretUtils;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.core.attributes.EnergyNetComponent;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockBreakHandler;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockPlaceHandler;
import com.github.drakescraft_labs.slimefun4.core.networks.energy.EnergyNetComponentType;
import com.github.drakescraft_labs.slimefun4.legacy.Objects.handlers.BlockTicker;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static org.bukkit.Bukkit.getWorlds;

public abstract class AbstractTurret extends CustomRecipeItem implements EnergyNetComponent, Listener {
    protected AbstractTurret(
            com.github.drakescraft_labs.slimefun4.api.items.ItemGroup itemGroup,
            SlimefunItemStack item,
            ItemStack[] recipe
    ) {
        super(
                itemGroup,
                item,
                com.Chagui68.weaponsaddon.items.MilitaryRecipeTypes.getMilitaryMachineFabricator(),
                recipe,
                RecipeGridSize.GRID_6x6
        );
    }

    protected abstract String getTurretId();
    protected abstract String getHitboxTag();
    protected abstract String getStructurePrefix();
    protected abstract double getBaseRange();
    protected abstract double getBaseDamage();
    protected abstract int getBaseEnergyCapacity();
    protected abstract int getEnergyPerShot();
    protected abstract SlimefunItemStack getTurretItem();
    protected abstract int getShotCooldown();
    protected abstract void onShootEffects(Location baseLoc, Location muzzle, LivingEntity target, double range);
    protected abstract void onStructurePlaced(Location loc);

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        int maxLevel = TurretUpgradeManager.getMaxLevel(getTurretId());
        return TurretUpgradeManager.getCapacityForLevel(getBaseEnergyCapacity(), maxLevel);
    }

    public double getCurrentRange(Location loc) {
        return TurretUpgradeManager.getRangeForLevel(getBaseRange(), TurretUpgradeManager.getCurrentLevel(loc));
    }

    public double getCurrentDamage(Location loc) {
        return TurretUpgradeManager.getDamageForLevel(getBaseDamage(), TurretUpgradeManager.getCurrentLevel(loc));
    }

    public int getCurrentEnergyCost(Location loc) {
        return TurretUpgradeManager.getEnergyCostForLevel(getEnergyPerShot(), TurretUpgradeManager.getCurrentLevel(loc));
    }

    public int getCurrentCapacity(Location loc) {
        return TurretUpgradeManager.getCapacityForLevel(getBaseEnergyCapacity(), TurretUpgradeManager.getCurrentLevel(loc));
    }

    public int getCurrentShotCooldown(Location loc) {
        return TurretUpgradeManager.getShotCooldownForLevel(getShotCooldown(), TurretUpgradeManager.getCurrentLevel(loc));
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                if (e.isCancelled()) {
                    return;
                }

                Location loc = e.getBlock().getLocation();
                String structure = TurretStructureManager.getStructureName(getStructurePrefix(), 1);

                // Turrets perform direct multiblock world writes. On Towny servers we
                // only permit them where the entire conservative footprint is buildable.
                if (!isStructureSafeForPlayer(e.getPlayer(), loc, structure)) {
                    e.setCancelled(true);
                    ProtectionService.deny(e.getPlayer(), "turret placement");
                    return;
                }

                if (!TurretStructureManager.canPlaceStructure(loc, structure, null)) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cThere is not enough clear space to place this turret.");
                    return;
                }

                e.getBlock().setType(Material.LIGHT);
                BlockStorage.addBlockInfo(e.getBlock(), "id", getTurretId());
                TurretUpgradeManager.setLevel(loc, 1);

                if (!TurretStructureManager.placeStructure(loc, structure)) {
                    TurretStructureManager.removeStructure(loc, structure);
                    BlockStorage.clearBlockInfo(loc);
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cThe turret structure could not be loaded.");
                    return;
                }

                ensureHitbox(loc);
                onStructurePlaced(loc);
            }
        });

        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                if (e.isCancelled() || !ProtectionService.canDestroy(e.getPlayer(), e.getBlock().getLocation())) {
                    e.setCancelled(true);
                    return;
                }
                dismantle(e.getBlock().getLocation());
            }

            @Override
            public void onExplode(Block b, List<ItemStack> drops) {
                // Never let an automated/direct teardown mutate a claimed Towny area.
                if (ProtectionService.canAutomateWorldChange(b.getLocation())) {
                    dismantle(b.getLocation());
                }
            }
        });

        addItemHandler(new BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem item, Config data) {
                AbstractTurret.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });
    }

    protected void tick(Block b) {
        Location loc = b.getLocation();

        // A ticker has no player actor. Fail closed in Towny claims: no auto-repair,
        // no direct structure writes and no autonomous turret firing there.
        if (!ProtectionService.canAutomateWorldChange(loc)) {
            return;
        }

        int level = TurretUpgradeManager.getCurrentLevel(loc);
        String structure = TurretStructureManager.getStructureName(getStructurePrefix(), level);

        if (!TurretStructureManager.isStructureIntact(loc, structure)
                && !TurretStructureManager.repairStructure(loc, structure)) {
            return;
        }

        ensureHitbox(loc);

        int structureHeight = TurretStructureManager.getStructureHeight(structure);
        Location muzzle = loc.clone().add(0.5, structureHeight + 1.05, 0.5);

        int cooldown = readCooldown(loc);
        if (cooldown > 0) {
            BlockStorage.addBlockInfo(loc, "cooldown", String.valueOf(cooldown - 1));
            LivingEntity target = findTarget(loc, muzzle);
            updateModelRotation(loc, target);
            return;
        }

        int currentCapacity = getCurrentCapacity(loc);
        int charge = EnergyManager.getCharge(loc);
        if (charge > currentCapacity) {
            EnergyManager.setCharge(loc, currentCapacity);
            charge = currentCapacity;
        }

        LivingEntity target = findTarget(loc, muzzle);
        updateModelRotation(loc, target);
        if (target == null) {
            return;
        }

        int energyCost = getCurrentEnergyCost(loc);
        if (charge < energyCost) {
            return;
        }

        double range = getCurrentRange(loc);
        onShootEffects(loc, muzzle, target, range);
        EnergyManager.removeCharge(loc, energyCost);

        int shotCooldown = getCurrentShotCooldown(loc);
        if (shotCooldown > 0) {
            BlockStorage.addBlockInfo(loc, "cooldown", String.valueOf(shotCooldown));
        }
    }

    private int readCooldown(Location loc) {
        String value = BlockStorage.getLocationInfo(loc, "cooldown");
        if (value == null) {
            return 0;
        }

        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            BlockStorage.addBlockInfo(loc, "cooldown", "0");
            return 0;
        }
    }

    protected LivingEntity findTarget(Location baseLoc, Location muzzle) {
        double range = getCurrentRange(baseLoc);
        Location center = baseLoc.clone().add(0.5, 0.5, 0.5);
        Collection<Entity> nearby = baseLoc.getWorld().getNearbyEntities(center, range, range, range);
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : nearby) {
            boolean hostile = entity instanceof Monster
                    || entity instanceof Slime
                    || entity instanceof Ghast
                    || entity instanceof Phantom
                    || entity instanceof Shulker
                    || entity instanceof Hoglin;

            if (!hostile
                    || entity.isDead()
                    || entity.hasMetadata("no_target")
                    || entity.getScoreboardTags().contains("PVZ_HEAD")
                    || entity.getScoreboardTags().contains("PVZ_GUARDIAN")) {
                continue;
            }

            double distanceSquared = entity.getLocation().distanceSquared(center);
            if (distanceSquared >= closestDist || distanceSquared > range * range) {
                continue;
            }

            LivingEntity living = (LivingEntity) entity;
            if (hasLineOfSight(muzzle, living)) {
                closestDist = distanceSquared;
                closest = living;
            }
        }

        return closest;
    }

    protected boolean hasLineOfSight(Location start, LivingEntity target) {
        Location end = target.getEyeLocation();
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        if (distance <= 0.0001) {
            return true;
        }

        RayTraceResult result = start.getWorld().rayTraceBlocks(
                start,
                direction.normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true
        );
        return result == null || result.getHitBlock() == null;
    }

    protected void damageTarget(LivingEntity target, double damage) {
        target.setNoDamageTicks(0);
        target.damage(damage);
        target.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, target.getEyeLocation(), 5, 0.2, 0.2, 0.2, 0.05);
        target.getWorld().playSound(target.getEyeLocation(), Sound.ENTITY_SLIME_ATTACK, 1.0f, 1.2f);
    }

    protected void updateModelRotation(Location loc, LivingEntity target) {
        String tag = getLocationTag(loc);
        Location center = loc.clone().add(0.5, 0.6, 0.5);
        float yaw = 0;
        if (target != null) {
            Vector direction = target.getLocation().toVector().subtract(center.toVector());
            yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        }

        for (Entity entity : loc.getWorld().getNearbyEntities(center, 1.5, getHitboxHeight(loc), 1.5)) {
            if (entity.getScoreboardTags().contains(tag)
                    && (entity.getScoreboardTags().contains("TURRET_HEAD")
                    || entity.getScoreboardTags().contains("TURRET_MOUTH")
                    || entity.getScoreboardTags().contains("TURRET_SENSOR"))) {
                Location entityLoc = entity.getLocation();
                entityLoc.setYaw(yaw);
                entity.teleport(entityLoc);
            }
        }
    }

    private void ensureHitbox(Location loc) {
        String locationTag = getLocationTag(loc);
        Location anchor = loc.clone().add(0.5, 0.0, 0.5);
        float height = getHitboxHeight(loc);

        for (Entity entity : loc.getWorld().getNearbyEntities(anchor.clone().add(0, height / 2.0, 0), 2.0, height, 2.0)) {
            if (entity instanceof Interaction interaction
                    && entity.getScoreboardTags().contains(locationTag)
                    && entity.getScoreboardTags().contains(getHitboxTag())) {
                interaction.setInteractionWidth(1.25f);
                interaction.setInteractionHeight(height);
                return;
            }
        }

        Interaction interaction = (Interaction) loc.getWorld().spawnEntity(anchor, EntityType.INTERACTION);
        interaction.setInteractionWidth(1.25f);
        interaction.setInteractionHeight(height);
        interaction.addScoreboardTag(locationTag);
        interaction.addScoreboardTag(getHitboxTag());
        interaction.addScoreboardTag("TURRET_HITBOX");
    }

    private float getHitboxHeight(Location loc) {
        int level = TurretUpgradeManager.getCurrentLevel(loc);
        String structure = TurretStructureManager.getStructureName(getStructurePrefix(), level);
        return Math.max(1.5f, TurretStructureManager.getStructureHeight(structure) + 1.5f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitboxAttack(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains(getHitboxTag())) {
            return;
        }
        if (!(e.getDamager() instanceof Player player)) {
            e.setCancelled(true);
            return;
        }

        Location loc = getBaseLocation(interaction);
        if (loc == null || !ProtectionService.canDestroy(player, loc)) {
            e.setCancelled(true);
            ProtectionService.deny(player, "turret dismantling");
            return;
        }

        e.setCancelled(true);
        handleDismantle(interaction, player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitboxInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains(getHitboxTag())) {
            return;
        }

        Player player = e.getPlayer();
        e.setCancelled(true);

        Location loc = getBaseLocation(interaction);
        if (loc == null) {
            interaction.remove();
            return;
        }

        if (!ProtectionService.canModify(player, loc)) {
            ProtectionService.deny(player, "turret interaction");
            return;
        }

        if (player.isSneaking()) {
            String id = BlockStorage.getLocationInfo(loc, "id");
            if (getTurretId().equals(id)) {
                TurretUpgradeGUI.open(
                        player,
                        getTurretId(),
                        getTurretItem().getDisplayName(),
                        loc,
                        getBaseRange(),
                        getBaseDamage(),
                        getBaseEnergyCapacity(),
                        getEnergyPerShot()
                );
            }
            return;
        }

        handleDismantle(interaction, player);
    }

    protected void handleDismantle(Interaction interaction, Entity damager) {
        if (!(damager instanceof Player player)) {
            return;
        }
        if (interaction.hasMetadata("MA_DISMANTLED") || !interaction.isValid()) {
            return;
        }

        Location loc = getBaseLocation(interaction);
        if (loc == null || !ProtectionService.canDestroy(player, loc)) {
            ProtectionService.deny(player, "turret dismantling");
            return;
        }
        if (!TurretUtils.beginDismantle(loc)) {
            return;
        }

        String id = BlockStorage.getLocationInfo(loc, "id");
        if (getTurretId().equals(id)) {
            interaction.setMetadata("MA_DISMANTLED", new FixedMetadataValue(WeaponsAddon.getInstance(), true));
            dismantle(loc);
            interaction.getWorld().playSound(interaction.getLocation(), Sound.BLOCK_LANTERN_BREAK, 1f, 1f);
            interaction.getWorld().dropItemNaturally(loc, getTurretItem().clone());
        } else {
            interaction.remove();
        }
    }

    private boolean isStructureSafeForPlayer(Player player, Location baseLoc, String structureName) {
        if (!ProtectionService.canModify(player, baseLoc)) {
            return false;
        }
        if (!ProtectionService.isTownyPresent()) {
            return true;
        }

        // Conservative footprint guard. Turret structures are narrow towers; this
        // deliberately checks beyond their current footprint so a direct block write
        // can never spill across a Towny plot/chunk boundary unnoticed.
        int maxHeight = Math.max(1, TurretStructureManager.getMaxHeight(getStructurePrefix()) + 1);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = 0; y <= maxHeight; y++) {
                    Location check = baseLoc.clone().add(x, y, z);
                    if (!ProtectionService.canBuild(player, check)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Location getBaseLocation(Interaction interaction) {
        for (String tag : interaction.getScoreboardTags()) {
            if (!tag.startsWith(getTagPrefix())) {
                continue;
            }

            String[] parts = tag.split("_");
            if (parts.length < 4) {
                continue;
            }

            try {
                int x = Integer.parseInt(parts[parts.length - 3]);
                int y = Integer.parseInt(parts[parts.length - 2]);
                int z = Integer.parseInt(parts[parts.length - 1]);
                return new Location(interaction.getWorld(), x, y, z);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    protected void dismantle(Location loc) {
        int level = TurretUpgradeManager.getCurrentLevel(loc);
        String structure = TurretStructureManager.getStructureName(getStructurePrefix(), level);
        TurretStructureManager.removeStructure(loc, structure);
        BlockStorage.clearBlockInfo(loc);

        String tag = getLocationTag(loc);
        double height = getHitboxHeightForLevel(level);
        Location center = loc.clone().add(0.5, height / 2.0, 0.5);
        for (Entity entity : loc.getWorld().getNearbyEntities(center, 2.0, height + 1.0, 2.0)) {
            if (entity.getScoreboardTags().contains(tag)) {
                entity.remove();
            }
        }
    }

    private float getHitboxHeightForLevel(int level) {
        String structure = TurretStructureManager.getStructureName(getStructurePrefix(), level);
        return Math.max(1.5f, TurretStructureManager.getStructureHeight(structure) + 1.5f);
    }

    protected String getTagPrefix() {
        return getStructurePrefix().toUpperCase().replace("_", "") + "_";
    }

    private String getLocationTag(Location loc) {
        return getTagPrefix() + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public static void cleanupAllModels() {
        for (org.bukkit.World world : getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains("TURRET_HITBOX")
                        || entity.getScoreboardTags().stream().anyMatch(tag -> tag.startsWith("TURRET_"))) {
                    entity.remove();
                }
            }
        }
    }
}
