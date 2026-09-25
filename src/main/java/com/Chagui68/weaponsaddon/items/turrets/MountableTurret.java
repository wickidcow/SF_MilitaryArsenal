package com.Chagui68.weaponsaddon.items.turrets;

import com.Chagui68.weaponsaddon.WeaponsAddon;
import com.Chagui68.weaponsaddon.items.CustomRecipeItem;
import com.Chagui68.weaponsaddon.items.MilitaryRecipeTypes;
import com.Chagui68.weaponsaddon.items.components.MilitaryComponents;
import com.Chagui68.weaponsaddon.items.machines.energy.EnergyManager;
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
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getWorlds;

public class MountableTurret extends CustomRecipeItem implements EnergyNetComponent, Listener {
    private static final int ENERGY_CAPACITY = 50000;
    private static final int ENERGY_PER_SHOT = 150;
    private static final double DAMAGE = 10.0;
    private static final double RANGE = 35.0;
    private static final long FIRE_INTERVAL_MS = 250L;
    private static final String SHOT_READY_KEY = "mount-shot-ready";

    private static final Material PRIMARY_MATERIAL = Material.BLACK_CONCRETE;
    private static final Material SECONDARY_MATERIAL = Material.PURPLE_CONCRETE;
    private static final Material GLOW_MATERIAL = Material.SEA_LANTERN;
    private static final Material BARREL_MATERIAL = Material.NETHERITE_BLOCK;

    public static final SlimefunItemStack MOUNTABLE_TURRET = new SlimefunItemStack(
            "MA_MOUNTABLE_TURRET",
            Material.OBSERVER,
            "&4💣 &cWraith-Class War Turret",
            "",
            "&7A heavy player-controlled plasma turret.",
            "&7Advanced energy-based war machine.",
            "",
            "&6Target: &eAny (Including Players)",
            "&6Damage: &e10.0 HP",
            "&6Range: &e35 Blocks",
            "&6Energy: &e150 J per shot",
            "&6Fire Rate: &eUp to 4 shots/sec",
            "&6Capacity: &e50000 J",
            "",
            "&eRight-Click the base to mount",
            "&eLeft-Click while mounted to fire"
    );

    public MountableTurret(ItemGroup itemGroup, SlimefunItemStack item, ItemStack[] recipe) {
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
                SlimefunStorageCompat.setData(e.getBlock(), "id", "MA_MOUNTABLE_TURRET");
                spawnModel(e.getBlock().getLocation());
            }
        });

        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                removeModel(e.getBlock().getLocation());
            }

            @Override
            public void onExplode(Block b, List<ItemStack> drops) {
                removeModel(b.getLocation());
            }
        });

        addItemHandler(new BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem item, Config data) {
                MountableTurret.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        });
    }

    private void tick(Block b) {
        Location loc = b.getLocation();
        String tag = getTurretTag(loc);
        boolean hasHitbox = loc.getWorld()
                .getNearbyEntities(loc.clone().add(0.5, 1.0, 0.5), 1.5, 1.5, 1.5)
                .stream()
                .anyMatch(entity -> entity instanceof Interaction
                        && entity.getScoreboardTags().contains(tag)
                        && entity.getScoreboardTags().contains("MOUNT_HITBOX"));

        if (!hasHitbox) {
            removeModel(loc);
            spawnModel(loc);
        }
        updateModelRotation(loc);
    }

    private void spawnModel(Location loc) {
        Location center = loc.clone().add(0.5, 0, 0.5);
        World world = loc.getWorld();
        String tag = getTurretTag(loc);

        BlockDisplay base = (BlockDisplay) world.spawnEntity(center, EntityType.BLOCK_DISPLAY);
        base.setBlock(PRIMARY_MATERIAL.createBlockData());
        base.setTransformation(new Transformation(
                new Vector3f(-0.5f, 0.15f, -0.5f),
                new Quaternionf(),
                new Vector3f(1.0f, 0.25f, 1.0f),
                new Quaternionf()
        ));
        base.addScoreboardTag(tag);

        float[][] legOffsets = {{0.45f, 0.45f}, {0.45f, -0.45f}, {-0.45f, 0.45f}, {-0.45f, -0.45f}};
        for (float[] offset : legOffsets) {
            BlockDisplay leg = (BlockDisplay) world.spawnEntity(center, EntityType.BLOCK_DISPLAY);
            leg.setBlock(SECONDARY_MATERIAL.createBlockData());
            leg.setTransformation(new Transformation(
                    new Vector3f(offset[0] - 0.2f, 0.0f, offset[1] - 0.2f),
                    new Quaternionf(),
                    new Vector3f(0.4f, 0.3f, 0.4f),
                    new Quaternionf()
            ));
            leg.addScoreboardTag(tag);
        }

        BlockDisplay glow = (BlockDisplay) world.spawnEntity(center, EntityType.BLOCK_DISPLAY);
        glow.setBlock(GLOW_MATERIAL.createBlockData());
        glow.setTransformation(new Transformation(
                new Vector3f(-0.3f, 0.18f, -0.3f),
                new Quaternionf(),
                new Vector3f(0.6f, 0.05f, 0.6f),
                new Quaternionf()
        ));
        glow.addScoreboardTag(tag);

        ArmorStand seat = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.6, 0), EntityType.ARMOR_STAND);
        seat.setVisible(false);
        seat.setGravity(false);
        seat.setMarker(true);
        seat.addScoreboardTag(tag);
        seat.addScoreboardTag("MOUNT_SEAT");

        BlockDisplay head = (BlockDisplay) world.spawnEntity(center.clone().add(0, 0.9, 0), EntityType.BLOCK_DISPLAY);
        head.setBlock(PRIMARY_MATERIAL.createBlockData());
        head.setTransformation(new Transformation(
                new Vector3f(-0.4f, -0.3f, -0.4f),
                new Quaternionf(),
                new Vector3f(0.8f, 0.7f, 0.8f),
                new Quaternionf()
        ));
        head.addScoreboardTag(tag);
        head.addScoreboardTag("MOUNT_HEAD");

        float[] sideOffsets = {-0.65f, 0.45f};
        for (float x : sideOffsets) {
            BlockDisplay barrel = (BlockDisplay) world.spawnEntity(center.clone().add(0, 1.1, 0), EntityType.BLOCK_DISPLAY);
            barrel.setBlock(BARREL_MATERIAL.createBlockData());
            barrel.setTransformation(new Transformation(
                    new Vector3f(x + 0.05f, 0.0f, 0.4f),
                    new Quaternionf(),
                    new Vector3f(0.15f, 0.15f, 1.4f),
                    new Quaternionf()
            ));
            barrel.addScoreboardTag(tag);
            barrel.addScoreboardTag("MOUNT_BARREL");

            BlockDisplay wing = (BlockDisplay) world.spawnEntity(center.clone().add(0, 1.1, 0), EntityType.BLOCK_DISPLAY);
            wing.setBlock(SECONDARY_MATERIAL.createBlockData());
            wing.setTransformation(new Transformation(
                    new Vector3f(x - 0.1f, -0.3f, 0.0f),
                    new Quaternionf(),
                    new Vector3f(0.4f, 1.0f, 0.6f),
                    new Quaternionf()
            ));
            wing.addScoreboardTag(tag);
            wing.addScoreboardTag("MOUNT_BARREL");

            BlockDisplay wingGlow = (BlockDisplay) world.spawnEntity(center.clone().add(0, 1.1, 0), EntityType.BLOCK_DISPLAY);
            wingGlow.setBlock(GLOW_MATERIAL.createBlockData());
            wingGlow.setTransformation(new Transformation(
                    new Vector3f(x - 0.11f, 0.1f, 0.2f),
                    new Quaternionf(),
                    new Vector3f(0.42f, 0.1f, 0.2f),
                    new Quaternionf()
            ));
            wingGlow.addScoreboardTag(tag);
            wingGlow.addScoreboardTag("MOUNT_BARREL");
        }

        BlockDisplay ring = (BlockDisplay) world.spawnEntity(center.clone().add(0, 1.3, 0), EntityType.BLOCK_DISPLAY);
        ring.setBlock(SECONDARY_MATERIAL.createBlockData());
        ring.setTransformation(new Transformation(
                new Vector3f(-0.55f, -0.7f, -0.65f),
                new Quaternionf(),
                new Vector3f(1.1f, 1.5f, 0.2f),
                new Quaternionf()
        ));
        ring.addScoreboardTag(tag);
        ring.addScoreboardTag("MOUNT_HEAD");

        Interaction interaction = (Interaction) world.spawnEntity(center, EntityType.INTERACTION);
        interaction.setInteractionWidth(1.6f);
        interaction.setInteractionHeight(2.0f);
        interaction.addScoreboardTag(tag);
        interaction.addScoreboardTag("MOUNT_HITBOX");
    }

    @EventHandler
    public void onHitboxInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains("MOUNT_HITBOX")) {
            return;
        }
        if (interaction.hasMetadata("MA_DISMANTLED") || !interaction.isValid()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains("MOUNT_HITBOX")) {
            return;
        }

        e.setCancelled(true);
        if (interaction.hasMetadata("MA_DISMANTLED") || !interaction.isValid()) {
            return;
        }

        String tag = interaction.getScoreboardTags().stream()
                .filter(t -> t.startsWith("MOUNT_TURRET_"))
                .findFirst()
                .orElse(null);
        if (tag == null || e.getPlayer().getVehicle() != null) {
            return;
        }

        Entity seatEntity = interaction.getWorld()
                .getNearbyEntities(interaction.getLocation(), 1.5, 1.5, 1.5)
                .stream()
                .filter(entity -> entity instanceof ArmorStand
                        && entity.getScoreboardTags().contains(tag)
                        && entity.getScoreboardTags().contains("MOUNT_SEAT"))
                .findFirst()
                .orElse(null);

        if (seatEntity instanceof ArmorStand seat && seat.getPassengers().isEmpty()) {
            seat.addPassenger(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerShoot(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains("MOUNT_HITBOX")) {
            return;
        }
        if (!(e.getDamager() instanceof Player player)) {
            return;
        }

        e.setCancelled(true);
        if (isMounting(player, interaction)) {
            shoot(player, interaction);
            return;
        }
        handleDismantle(interaction, player);
    }

    private void handleDismantle(Interaction interaction, Player player) {
        if (interaction.hasMetadata("MA_DISMANTLED") || !interaction.isValid()) {
            return;
        }

        Location loc = getBaseLocation(interaction);
        if (loc == null || !TurretUtils.beginDismantle(loc)) {
            return;
        }

        String id = SlimefunStorageCompat.getData(loc, "id");
        if (!"MA_MOUNTABLE_TURRET".equals(id)) {
            removeModel(loc);
            interaction.remove();
            return;
        }

        interaction.setMetadata("MA_DISMANTLED", new FixedMetadataValue(WeaponsAddon.getInstance(), true));
        SlimefunStorageCompat.clear(loc);
        loc.getBlock().setType(Material.AIR, false);
        interaction.getWorld().playSound(loc, Sound.BLOCK_LANTERN_BREAK, 1f, 1f);
        interaction.getWorld().dropItemNaturally(loc, MOUNTABLE_TURRET.clone());
        removeModel(loc);
    }

    private boolean isMounting(Player player, Interaction interaction) {
        if (!(player.getVehicle() instanceof ArmorStand seat)
                || !seat.getScoreboardTags().contains("MOUNT_SEAT")) {
            return false;
        }

        String turretTag = interaction.getScoreboardTags().stream()
                .filter(t -> t.startsWith("MOUNT_TURRET_"))
                .findFirst()
                .orElse(null);
        return turretTag != null && seat.getScoreboardTags().contains(turretTag);
    }

    private void shoot(Player shooter, Interaction interaction) {
        Location loc = getBaseLocation(interaction);
        if (loc == null || !"MA_MOUNTABLE_TURRET".equals(SlimefunStorageCompat.getData(loc, "id"))) {
            return;
        }

        long now = System.currentTimeMillis();
        long readyAt = readReadyAt(loc);
        if (now < readyAt) {
            return;
        }

        // removeCharge performs the availability check and the deduction together.
        if (!EnergyManager.removeCharge(loc, ENERGY_PER_SHOT)) {
            shooter.sendMessage("§cNot enough energy!");
            shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }
        SlimefunStorageCompat.setData(loc, SHOT_READY_KEY, String.valueOf(now + FIRE_INTERVAL_MS));

        Location start = interaction.getLocation().clone().add(0, 1.1, 0);
        Vector direction = shooter.getEyeLocation().getDirection().normalize();
        World world = start.getWorld();

        world.playSound(start, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.5f, 0.5f);
        world.spawnParticle(
                Particle.SOUL_FIRE_FLAME,
                start.clone().add(direction.clone().multiply(1.5)),
                20,
                0.2,
                0.2,
                0.2,
                0.1
        );
        world.spawnParticle(Particle.FLASH, start.clone().add(direction.clone()), 2);

        RayTraceResult blockResult = world.rayTraceBlocks(
                start,
                direction,
                RANGE,
                FluidCollisionMode.NEVER,
                true
        );
        double maxDist = blockResult != null && blockResult.getHitPosition() != null
                ? blockResult.getHitPosition().distance(start.toVector())
                : RANGE;

        RayTraceResult entityResult = world.rayTraceEntities(
                start,
                direction,
                maxDist,
                0.5,
                entity -> entity instanceof LivingEntity
                        && entity != shooter
                        && !(entity instanceof ArmorStand)
                        && !(entity instanceof Interaction)
        );
        if (entityResult != null && entityResult.getHitEntity() instanceof LivingEntity target) {
            target.damage(DAMAGE, shooter);
            target.getWorld().spawnParticle(Particle.SONIC_BOOM, target.getLocation().add(0, 1, 0), 1);
        }

        Location bullet = start.clone();
        int steps = Math.max(1, (int) Math.ceil(maxDist / 1.4));
        for (int i = 0; i < steps; i++) {
            bullet.add(direction.clone().multiply(1.4));
            if (bullet.distanceSquared(start) > maxDist * maxDist) {
                break;
            }
            world.spawnParticle(Particle.SOUL, bullet, 1, 0, 0, 0, 0);
        }
    }

    private long readReadyAt(Location loc) {
        String value = SlimefunStorageCompat.getData(loc, SHOT_READY_KEY);
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            SlimefunStorageCompat.setData(loc, SHOT_READY_KEY, "0");
            return 0L;
        }
    }

    private Location getBaseLocation(Interaction interaction) {
        String tag = interaction.getScoreboardTags().stream()
                .filter(t -> t.startsWith("MOUNT_TURRET_"))
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

    private void updateModelRotation(Location loc) {
        String tag = getTurretTag(loc);
        Entity seatEntity = loc.getWorld()
                .getNearbyEntities(loc.clone().add(0.5, 0.5, 0.5), 1.5, 1.5, 1.5)
                .stream()
                .filter(entity -> entity instanceof ArmorStand
                        && entity.getScoreboardTags().contains(tag)
                        && entity.getScoreboardTags().contains("MOUNT_SEAT"))
                .findFirst()
                .orElse(null);

        if (!(seatEntity instanceof ArmorStand seat) || seat.getPassengers().isEmpty()) {
            return;
        }
        if (!(seat.getPassengers().getFirst() instanceof Player player)) {
            return;
        }

        float yaw = player.getLocation().getYaw();
        for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.5, 0.5), 1.5, 1.5, 1.5)) {
            if (entity.getScoreboardTags().contains(tag)
                    && (entity.getScoreboardTags().contains("MOUNT_HEAD")
                    || entity.getScoreboardTags().contains("MOUNT_BARREL"))) {
                Location entityLoc = entity.getLocation();
                entityLoc.setYaw(yaw);
                entity.teleport(entityLoc);
            }
        }
    }

    private void removeModel(Location loc) {
        String tag = getTurretTag(loc);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.75, 0.5), 2.0, 2.5, 2.0)) {
            if (entity.getScoreboardTags().contains(tag)) {
                entity.remove();
            }
        }
    }

    private String getTurretTag(Location loc) {
        return "MOUNT_TURRET_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public static void cleanupAllModels() {
        for (World world : getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().stream().anyMatch(tag -> tag.startsWith("MOUNT_"))) {
                    entity.remove();
                }
            }
        }
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[]{
                MilitaryComponents.FIREARM_BARREL, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.FIREARM_BARREL,
                MilitaryComponents.KINETIC_STABILIZER, MilitaryComponents.ADVANCED_MOVEMENT_CIRCUIT, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TARGETING_SYSTEM, MilitaryComponents.KINETIC_STABILIZER,
                MilitaryComponents.MOVEMENT_CIRCUIT, MilitaryComponents.TUNGSTEN_INGOT, new ItemStack(Material.SADDLE), new ItemStack(Material.SADDLE), MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.MOVEMENT_CIRCUIT,
                MilitaryComponents.MOVEMENT_CIRCUIT, MilitaryComponents.TUNGSTEN_INGOT, new ItemStack(Material.SADDLE), new ItemStack(Material.SADDLE), MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.MOVEMENT_CIRCUIT,
                MilitaryComponents.KINETIC_STABILIZER, MilitaryComponents.TARGETING_SYSTEM, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.TUNGSTEN_INGOT, MilitaryComponents.ADVANCED_MOVEMENT_CIRCUIT, MilitaryComponents.KINETIC_STABILIZER,
                MilitaryComponents.FIREARM_BARREL, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.FIREARM_BARREL
        };

        MountableTurret turret = new MountableTurret(category, MOUNTABLE_TURRET, recipe);
        turret.register(addon);
        if (addon instanceof Plugin plugin) {
            getPluginManager().registerEvents(turret, plugin);
        }
    }
}
