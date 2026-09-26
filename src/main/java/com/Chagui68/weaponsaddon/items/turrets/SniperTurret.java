package com.Chagui68.weaponsaddon.items.turrets;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import com.Chagui68.weaponsaddon.items.CustomRecipeItem;
import com.Chagui68.weaponsaddon.items.MilitaryRecipeTypes;
import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
import com.Chagui68.weaponsaddon.items.machines.energy.EnergyManager;
import com.Chagui68.weaponsaddon.items.vouchers.MilitaryVouchers;
import com.Chagui68.weaponsaddon.utils.TurretUtils;
import com.github.drakescraft_labs.slimefun4.api.SlimefunAddon;
import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.core.attributes.EnergyNetComponent;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockBreakHandler;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockPlaceHandler;
import com.github.drakescraft_labs.slimefun4.core.networks.energy.EnergyNetComponentType;
import com.github.drakescraft_labs.slimefun4.legacy.Objects.handlers.BlockTicker;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ASlimefunDataContainer;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getWorlds;

public class SniperTurret extends CustomRecipeItem implements EnergyNetComponent, Listener {
    private static final int ENERGY_CAPACITY = 6000;
    private static final int ENERGY_PER_SHOT = 250;
    private static final double RANGE = 55.0;
    private static final double DAMAGE = 100.0;
    private static final int SHOT_COOLDOWN = 3;

    private static final float STEM_SCALE_X = 0.25f;
    private static final float STEM_SCALE_Y = 1.1f;
    private static final float STEM_SCALE_Z = 0.25f;
    private static final float STEM_OFFSET_X = -0.125f;
    private static final float HEAD_OFFSET_Y = 1.1f;
    private static final float HEAD_SCALE = 0.5f;
    private static final float HEAD_INT_OFFSET = -0.25f;
    private static final float BARREL_OFFSET_Z = 0.1f;
    private static final float BARREL_SCALE_X = 0.12f;
    private static final float BARREL_SCALE_Y = 0.12f;
    private static final float BARREL_SCALE_Z = 1.4f;
    private static final float SENSOR_OFFSET_X = 0.1f;
    private static final float SENSOR_OFFSET_Y = 0.20f;
    private static final float SENSOR_OFFSET_Z = 0.20f;
    private static final float SENSOR_SCALE = 0.12f;

    private static final Material STEM_MATERIAL = Material.BLACK_CONCRETE;
    private static final Material HEAD_MATERIAL = Material.GOLD_BLOCK;
    private static final Material BARREL_MATERIAL = Material.GRAY_CONCRETE;
    private static final Material SENSOR_MATERIAL = Material.RED_STAINED_GLASS;

    public static final SlimefunItemStack SNIPER_TURRET = new SlimefunItemStack(
            "MA_SNIPER_TURRET",
            Material.RED_STAINED_GLASS,
            "&1🎯 &9Long-Range Sniper Turret",
            "",
            "&7Precision-engineered for long-range",
            "&7elimination of hostile targets.",
            "",
            "&6Maximum Range: &e55 Blocks",
            "&6Precision Damage: &e100.0 HP",
            "&6Fire Rate: &eVery Slow",
            "&6Energy: &e250 J per shot",
            "&6Capacity: &e6000 J",
            "",
            "&eRight-Click to place",
            "&8(Animated 3D Model)"
    );

    public SniperTurret(ItemGroup itemGroup, SlimefunItemStack item, ItemStack[] recipe) {
        super(itemGroup, item, MilitaryRecipeTypes.getMilitaryMachineFabricator(), recipe, RecipeGridSize.GRID_6x6);
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return ENERGY_CAPACITY;
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                e.getBlock().setType(Material.LIGHT);
                SlimefunStorageCompat.setData(e.getBlock(), "id", "MA_SNIPER_TURRET");
                spawnPvzModel(e.getBlock().getLocation());
            }
        });

        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                removePvzModel(e.getBlock().getLocation());
            }

            @Override
            public void onExplode(Block b, List<ItemStack> drops) {
                removePvzModel(b.getLocation());
            }
        });

        addItemHandler(new BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem item, ASlimefunDataContainer data) {
                SniperTurret.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });
    }

    private void tick(Block b) {
        Location loc = b.getLocation();
        String tag = getModelTag(loc);
        boolean hasHitbox = loc.getWorld()
                .getNearbyEntities(loc.clone().add(0.5, 0.8, 0.5), 1.5, 1.5, 1.5)
                .stream()
                .anyMatch(entity -> entity instanceof Interaction
                        && entity.getScoreboardTags().contains(tag)
                        && entity.getScoreboardTags().contains("SNIPER_HITBOX"));
        if (!hasHitbox) {
            removePvzModel(loc);
            spawnPvzModel(loc);
        }

        int cooldown = readCooldown(loc);
        if (cooldown > 0) {
            SlimefunStorageCompat.setData(loc, "cooldown", String.valueOf(cooldown - 1));
            LivingEntity target = findTarget(loc);
            updateModelRotation(loc, target);
            return;
        }

        LivingEntity target = findTarget(loc);
        updateModelRotation(loc, target);
        if (target == null) {
            return;
        }

        if (!EnergyManager.removeCharge(loc, ENERGY_PER_SHOT)) {
            return;
        }

        shoot(loc, target);
        SlimefunStorageCompat.setData(loc, "cooldown", String.valueOf(SHOT_COOLDOWN));
    }

    private int readCooldown(Location loc) {
        String value = SlimefunStorageCompat.getData(loc, "cooldown");
        if (value == null) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            SlimefunStorageCompat.setData(loc, "cooldown", "0");
            return 0;
        }
    }

    private LivingEntity findTarget(Location baseLoc) {
        Location center = baseLoc.clone().add(0.5, 0.5, 0.5);
        Location sensorOrigin = baseLoc.clone().add(0.5, HEAD_OFFSET_Y + SENSOR_OFFSET_Y + 0.05, 0.5);
        Collection<Entity> nearby = baseLoc.getWorld().getNearbyEntities(center, RANGE, RANGE, RANGE);
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
            if (distanceSquared >= closestDist || distanceSquared > RANGE * RANGE) {
                continue;
            }

            LivingEntity living = (LivingEntity) entity;
            if (hasLineOfSight(sensorOrigin, living)) {
                closestDist = distanceSquared;
                closest = living;
            }
        }
        return closest;
    }

    private boolean hasLineOfSight(Location start, LivingEntity target) {
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

    private void shoot(Location baseLoc, LivingEntity target) {
        Location pivot = baseLoc.clone().add(0.5, 0.5, 0.5);
        Location targetLoc = target.getEyeLocation();
        Vector direction = targetLoc.toVector().subtract(pivot.toVector()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        double radYaw = Math.toRadians(yaw);
        Location muzzle = pivot.clone().add(
                -Math.sin(radYaw) * (BARREL_SCALE_Z + 0.1),
                0.4 + (HEAD_OFFSET_Y - 0.95),
                Math.cos(radYaw) * (BARREL_SCALE_Z + 0.1)
        );

        muzzle.getWorld().playSound(muzzle, Sound.ENTITY_EGG_THROW, 1.5f, 0.8f);
        muzzle.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, muzzle, 5, 0.1, 0.1, 0.1, 0.05);

        Location bullet = muzzle.clone();
        int particleSteps = Math.max(1, (int) Math.ceil(RANGE * 2.0));
        for (int i = 0; i < particleSteps; i++) {
            bullet.add(direction.clone().multiply(0.5));
            if (bullet.distanceSquared(muzzle) > RANGE * RANGE) {
                break;
            }
            muzzle.getWorld().spawnParticle(Particle.COMPOSTER, bullet, 1, 0, 0, 0, 0);
        }

        target.setNoDamageTicks(0);
        target.damage(DAMAGE);
        target.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, targetLoc, 5, 0.2, 0.2, 0.2, 0.05);
        target.getWorld().playSound(targetLoc, Sound.ENTITY_SLIME_ATTACK, 1.0f, 1.2f);
    }

    private void spawnPvzModel(Location loc) {
        Location center = loc.clone().add(0.5, 0, 0.5);
        World world = loc.getWorld();
        String tag = getModelTag(loc);

        BlockDisplay stem = (BlockDisplay) world.spawnEntity(center, EntityType.BLOCK_DISPLAY);
        stem.setBlock(STEM_MATERIAL.createBlockData());
        stem.setTransformation(new Transformation(
                new Vector3f(STEM_OFFSET_X, 0.0f, STEM_OFFSET_X),
                new Quaternionf(),
                new Vector3f(STEM_SCALE_X, STEM_SCALE_Y, STEM_SCALE_Z),
                new Quaternionf()
        ));
        stem.addScoreboardTag(tag);

        BlockDisplay frame = (BlockDisplay) world.spawnEntity(center, EntityType.BLOCK_DISPLAY);
        frame.setBlock(Material.IRON_BARS.createBlockData());
        frame.setTransformation(new Transformation(
                new Vector3f(-0.2f, 0.0f, -0.2f),
                new Quaternionf(),
                new Vector3f(0.4f, STEM_SCALE_Y * 0.9f, 0.4f),
                new Quaternionf()
        ));
        frame.addScoreboardTag(tag);

        BlockDisplay head = (BlockDisplay) world.spawnEntity(center.clone().add(0, HEAD_OFFSET_Y, 0), EntityType.BLOCK_DISPLAY);
        head.setBlock(HEAD_MATERIAL.createBlockData());
        head.setTransformation(new Transformation(
                new Vector3f(HEAD_INT_OFFSET, -0.15f, HEAD_INT_OFFSET),
                new Quaternionf(),
                new Vector3f(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE),
                new Quaternionf()
        ));
        head.addScoreboardTag(tag);
        head.addScoreboardTag("PVZ_HEAD");

        BlockDisplay mouth = (BlockDisplay) world.spawnEntity(center.clone().add(0, HEAD_OFFSET_Y, 0), EntityType.BLOCK_DISPLAY);
        mouth.setBlock(BARREL_MATERIAL.createBlockData());
        mouth.setTransformation(new Transformation(
                new Vector3f(-BARREL_SCALE_X / 2f, 0.0f, BARREL_OFFSET_Z),
                new Quaternionf(),
                new Vector3f(BARREL_SCALE_X, BARREL_SCALE_Y, BARREL_SCALE_Z),
                new Quaternionf()
        ));
        mouth.addScoreboardTag(tag);
        mouth.addScoreboardTag("PVZ_MOUTH");

        BlockDisplay sensor = (BlockDisplay) world.spawnEntity(center.clone().add(0, HEAD_OFFSET_Y, 0), EntityType.BLOCK_DISPLAY);
        sensor.setBlock(SENSOR_MATERIAL.createBlockData());
        sensor.setTransformation(new Transformation(
                new Vector3f(SENSOR_OFFSET_X, SENSOR_OFFSET_Y, SENSOR_OFFSET_Z),
                new Quaternionf(),
                new Vector3f(SENSOR_SCALE, SENSOR_SCALE, SENSOR_SCALE),
                new Quaternionf()
        ));
        sensor.addScoreboardTag(tag);
        sensor.addScoreboardTag("PVZ_SENSOR");

        Interaction interaction = (Interaction) world.spawnEntity(center, EntityType.INTERACTION);
        interaction.setInteractionWidth(1.2f);
        interaction.setInteractionHeight(HEAD_OFFSET_Y + HEAD_SCALE);
        interaction.addScoreboardTag(tag);
        interaction.addScoreboardTag("SNIPER_HITBOX");
    }

    @EventHandler
    public void onHitboxAttack(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains("SNIPER_HITBOX")) {
            return;
        }
        e.setCancelled(true);
        handleDismantle(interaction, e.getDamager());
    }

    @EventHandler
    public void onHitboxInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains("SNIPER_HITBOX")) {
            return;
        }
        e.setCancelled(true);
        handleDismantle(interaction, e.getPlayer());
    }

    private void handleDismantle(Interaction interaction, Entity damager) {
        if (!(damager instanceof Player)) {
            return;
        }
        if (interaction.getScoreboardTags().contains("MA_DISMANTLED") || !interaction.isValid()) {
            return;
        }

        Location loc = getBaseLocation(interaction);
        if (loc == null || !TurretUtils.beginDismantle(loc)) {
            return;
        }

        String id = SlimefunStorageCompat.getData(loc, "id");
        if (!"MA_SNIPER_TURRET".equals(id)) {
            removePvzModel(loc);
            interaction.remove();
            return;
        }

        interaction.addScoreboardTag("MA_DISMANTLED");
        SlimefunStorageCompat.clear(loc);
        loc.getBlock().setType(Material.AIR, false);
        interaction.getWorld().playSound(loc, Sound.BLOCK_LANTERN_BREAK, 1f, 1f);
        interaction.getWorld().dropItemNaturally(loc, SNIPER_TURRET.clone());
        removePvzModel(loc);
    }

    private Location getBaseLocation(Interaction interaction) {
        String tag = interaction.getScoreboardTags().stream()
                .filter(value -> value.startsWith("PVZ_SNIPER_"))
                .findFirst()
                .orElse(null);
        if (tag == null) {
            return null;
        }

        String[] parts = tag.split("_");
        if (parts.length != 5) {
            return null;
        }

        try {
            int x = Integer.parseInt(parts[2]);
            int y = Integer.parseInt(parts[3]);
            int z = Integer.parseInt(parts[4]);
            return new Location(interaction.getWorld(), x, y, z);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static void cleanupAllModels() {
        for (World world : getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().stream().anyMatch(tag -> tag.startsWith("PVZ_SNIPER_"))) {
                    entity.remove();
                }
            }
        }
    }

    private void removePvzModel(Location loc) {
        String tag = getModelTag(loc);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.8, 0.5), 1.5, 2.0, 1.5)) {
            if (entity.getScoreboardTags().contains(tag)) {
                entity.remove();
            }
        }
    }

    private void updateModelRotation(Location loc, LivingEntity target) {
        String tag = getModelTag(loc);
        Location center = loc.clone().add(0.5, 0.6, 0.5);
        float yaw = 0;
        if (target != null) {
            Vector direction = target.getLocation().toVector().subtract(center.toVector());
            yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        }

        for (Entity entity : loc.getWorld().getNearbyEntities(center, 1.5, 1.5, 1.5)) {
            if (entity.getScoreboardTags().contains(tag)
                    && (entity.getScoreboardTags().contains("PVZ_HEAD")
                    || entity.getScoreboardTags().contains("PVZ_MOUTH")
                    || entity.getScoreboardTags().contains("PVZ_SENSOR"))) {
                Location entityLoc = entity.getLocation();
                entityLoc.setYaw(yaw);
                entity.teleport(entityLoc);
            }
        }
    }

    private String getModelTag(Location loc) {
        return "PVZ_SNIPER_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[]{
                MilitaryComponents.FIREARM_BARREL, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.FIREARM_BARREL,
                MilitaryComponents.KINETIC_STABILIZER, MilitaryComponents.TARGETING_SYSTEM, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TARGETING_SYSTEM, MilitaryComponents.KINETIC_STABILIZER,
                MilitaryComponents.MOVEMENT_CIRCUIT, MilitaryComponents.TUNGSTEN_INGOT, MilitaryVouchers.VOUCHER_DOCUMENT, MilitaryComponents.TURRET_SHELL, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.MOVEMENT_CIRCUIT,
                MilitaryComponents.MOVEMENT_CIRCUIT, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TURRET_SHELL, MilitaryVouchers.VOUCHER_WEAPON_UPGRADE, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.MOVEMENT_CIRCUIT,
                MilitaryComponents.KINETIC_STABILIZER, MilitaryComponents.TARGETING_SYSTEM, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TARGETING_SYSTEM, MilitaryComponents.KINETIC_STABILIZER,
                MilitaryComponents.FIREARM_BARREL, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.FIREARM_BARREL
        };

        SniperTurret turret = new SniperTurret(category, SNIPER_TURRET, recipe);
        turret.register(addon);
        if (addon instanceof Plugin plugin) {
            getPluginManager().registerEvents(turret, plugin);
        }
    }
}
