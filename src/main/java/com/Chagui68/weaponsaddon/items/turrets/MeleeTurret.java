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
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import com.Chagui68.weaponsaddon.utils.SlimefunStorageCompat;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MeleeTurret extends CustomRecipeItem implements EnergyNetComponent, Listener {
    private static final int ENERGY_CAPACITY = 5000;
    private static final int ENERGY_PER_ATTACK = 150;
    private static final double RANGE = 4.0;
    private static final double DAMAGE = 50.0;
    private static final int ATTACK_COOLDOWN = 2;
    private static final Random RANDOM = new Random();
    private static final Set<String> ACTIVE_ATTACKS = new HashSet<>();

    public static final SlimefunItemStack MELEE_TURRET = new SlimefunItemStack(
            "MA_MELEE_TURRET",
            Material.WHITE_BANNER,
            "&e⚔ &6Guardian Melee Turret",
            "",
            "&7Advanced sentinel programmed for",
            "&7lethal close-quarters combat.",
            "",
            "&6Range: &e4.0 Blocks",
            "&6Damage: &e50.0 HP",
            "&6Animations: &e8 Distinct Styles",
            "&6Energy: &e150 J per attack",
            "&6Capacity: &e5000 J",
            "",
            "&eRight-Click to place",
            "&8(Animated Guardian Stand)"
    );

    public MeleeTurret(ItemGroup itemGroup, SlimefunItemStack item, ItemStack[] recipe) {
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
                SlimefunStorageCompat.setData(e.getBlock(), "id", "MA_MELEE_TURRET");
                spawnGuardianModel(e.getBlock().getLocation());
            }
        });

        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                removeGuardianModel(e.getBlock().getLocation());
            }

            @Override
            public void onExplode(Block b, List<ItemStack> drops) {
                removeGuardianModel(b.getLocation());
            }
        });

        addItemHandler(new BlockTicker() {
            @Override
            public void tick(Block b, SlimefunItem item, Config data) {
                MeleeTurret.this.tick(b);
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
                .getNearbyEntities(loc.clone().add(0.5, 1.0, 0.5), 1.5, 2.0, 1.5)
                .stream()
                .anyMatch(entity -> entity instanceof Interaction
                        && entity.getScoreboardTags().contains(tag)
                        && entity.getScoreboardTags().contains("MELEE_HITBOX"));
        if (!hasHitbox) {
            removeGuardianModel(loc);
            spawnGuardianModel(loc);
        }

        if (ACTIVE_ATTACKS.contains(tag)) {
            return;
        }

        int cooldown = readCooldown(loc);
        if (cooldown > 0) {
            SlimefunStorageCompat.setData(loc, "cooldown", String.valueOf(cooldown - 1));
            return;
        }

        LivingEntity target = findTarget(loc);
        if (target == null || EnergyManager.getCharge(loc) < ENERGY_PER_ATTACK) {
            return;
        }

        if (!performAttack(loc, target)) {
            return;
        }

        EnergyManager.removeCharge(loc, ENERGY_PER_ATTACK);
        SlimefunStorageCompat.setData(loc, "cooldown", String.valueOf(ATTACK_COOLDOWN));
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
        Location sightOrigin = baseLoc.clone().add(0.5, 1.1, 0.5);
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
            if (hasLineOfSight(sightOrigin, living)) {
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

    private boolean performAttack(Location loc, LivingEntity target) {
        String tag = getModelTag(loc);
        ArmorStand stand = null;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.75, 0.5), 1.5, 2.0, 1.5)) {
            if (entity instanceof ArmorStand armorStand && entity.getScoreboardTags().contains(tag)) {
                stand = armorStand;
                break;
            }
        }
        if (stand == null) {
            return false;
        }

        Vector direction = target.getLocation().toVector().subtract(stand.getLocation().toVector());
        Location standLoc = stand.getLocation();
        standLoc.setYaw((float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())));
        stand.teleport(standLoc);

        int animationIndex = RANDOM.nextInt(8);
        ArmorStand finalStand = stand;
        LivingEntity finalTarget = target;
        ACTIVE_ATTACKS.add(tag);

        new BukkitRunnable() {
            private int frame;

            @Override
            public void run() {
                if (finalStand.isDead() || !finalStand.isValid()) {
                    ACTIVE_ATTACKS.remove(tag);
                    cancel();
                    return;
                }

                switch (animationIndex) {
                    case 0 -> animatePowerOverstrike(finalStand, frame);
                    case 1 -> animateHorizontalSwipe(finalStand, frame);
                    case 2 -> animateStab(finalStand, frame);
                    case 3 -> animateUppercut(finalStand, frame);
                    case 4 -> animateDualStrike(finalStand, frame);
                    case 5 -> animateSpinStrike(finalStand, frame);
                    case 6 -> animateGroundSlam(finalStand, frame);
                    case 7 -> animateXSlash(finalStand, frame);
                    default -> {
                    }
                }

                if (frame == 5
                        && finalTarget.isValid()
                        && !finalTarget.isDead()
                        && finalTarget.getLocation().distanceSquared(finalStand.getLocation()) <= RANGE * RANGE) {
                    finalTarget.damage(DAMAGE, finalStand);
                    finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                    finalTarget.getWorld().spawnParticle(Particle.SWEEP_ATTACK, finalTarget.getLocation().add(0, 1, 0), 1);
                }

                if (frame >= 10) {
                    resetPose(finalStand);
                    ACTIVE_ATTACKS.remove(tag);
                    cancel();
                    return;
                }
                frame++;
            }
        }.runTaskTimer(WeaponsAddon.getInstance(), 0L, 1L);
        return true;
    }

    private void animatePowerOverstrike(ArmorStand stand, int frame) {
        if (frame < 5) {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-120), 0, 0));
        } else {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(30), 0, 0));
        }
    }

    private void animateHorizontalSwipe(ArmorStand stand, int frame) {
        if (frame < 5) {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-90), 0));
        } else {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(90), 0));
        }
    }

    private void animateStab(ArmorStand stand, int frame) {
        if (frame < 5) {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, Math.toRadians(-30)));
        } else {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, Math.toRadians(30)));
        }
    }

    @EventHandler
    public void onHitboxAttack(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Interaction interaction)) {
            return;
        }
        if (!interaction.getScoreboardTags().contains("MELEE_HITBOX")) {
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
        if (!interaction.getScoreboardTags().contains("MELEE_HITBOX")) {
            return;
        }
        e.setCancelled(true);
        handleDismantle(interaction, e.getPlayer());
    }

    private void handleDismantle(Interaction interaction, Entity damager) {
        if (!(damager instanceof Player)) {
            return;
        }
        if (interaction.hasMetadata("MA_DISMANTLED") || !interaction.isValid()) {
            return;
        }

        Location loc = getBaseLocation(interaction);
        if (loc == null || !TurretUtils.beginDismantle(loc)) {
            return;
        }

        String id = SlimefunStorageCompat.getData(loc, "id");
        if (!"MA_MELEE_TURRET".equals(id)) {
            removeGuardianModel(loc);
            interaction.remove();
            return;
        }

        interaction.setMetadata("MA_DISMANTLED", new FixedMetadataValue(WeaponsAddon.getInstance(), true));
        ACTIVE_ATTACKS.remove(getModelTag(loc));
        SlimefunStorageCompat.clear(loc);
        loc.getBlock().setType(Material.AIR, false);
        interaction.getWorld().playSound(interaction.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
        interaction.getWorld().dropItemNaturally(loc, MELEE_TURRET.clone());
        removeGuardianModel(loc);
    }

    private Location getBaseLocation(Interaction interaction) {
        String tag = interaction.getScoreboardTags().stream()
                .filter(value -> value.startsWith("MELEE_GUARDIAN_"))
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

    private void animateUppercut(ArmorStand stand, int frame) {
        if (frame < 5) {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(30), 0, 0));
        } else {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-150), 0, 0));
        }
    }

    private void animateDualStrike(ArmorStand stand, int frame) {
        if (frame < 5) {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-100), 0, 0));
            stand.setLeftArmPose(new EulerAngle(Math.toRadians(30), 0, 0));
        } else {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(30), 0, 0));
            stand.setLeftArmPose(new EulerAngle(Math.toRadians(-100), 0, 0));
        }
    }

    private void animateSpinStrike(ArmorStand stand, int frame) {
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, Math.toRadians(90)));
        Location loc = stand.getLocation();
        loc.setYaw(loc.getYaw() + 36);
        stand.teleport(loc);
    }

    private void animateGroundSlam(ArmorStand stand, int frame) {
        if (frame < 5) {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-150), Math.toRadians(-20), 0));
            stand.setLeftArmPose(new EulerAngle(Math.toRadians(-150), Math.toRadians(20), 0));
        } else {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(45), Math.toRadians(-20), 0));
            stand.setLeftArmPose(new EulerAngle(Math.toRadians(45), Math.toRadians(20), 0));
        }
    }

    private void animateXSlash(ArmorStand stand, int frame) {
        if (frame < 5) {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-120), Math.toRadians(-45), 0));
            stand.setLeftArmPose(new EulerAngle(Math.toRadians(-120), Math.toRadians(45), 0));
        } else {
            stand.setRightArmPose(new EulerAngle(Math.toRadians(20), Math.toRadians(45), 0));
            stand.setLeftArmPose(new EulerAngle(Math.toRadians(20), Math.toRadians(-45), 0));
        }
    }

    private void resetPose(ArmorStand stand) {
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(10)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(-10)));
    }

    private void spawnGuardianModel(Location loc) {
        Location center = loc.clone().add(0.5, 0, 0.5);
        World world = loc.getWorld();
        String tag = getModelTag(loc);

        ArmorStand stand = (ArmorStand) world.spawnEntity(center, EntityType.ARMOR_STAND);
        stand.setBasePlate(false);
        stand.setArms(true);
        stand.setSmall(false);
        stand.setVisible(true);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.addScoreboardTag(tag);
        stand.addScoreboardTag("PVZ_GUARDIAN");
        stand.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        stand.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        stand.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        stand.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        stand.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        stand.getEquipment().setItemInOffHand(new ItemStack(Material.NETHERITE_SWORD));
        resetPose(stand);

        Interaction interaction = (Interaction) world.spawnEntity(center, EntityType.INTERACTION);
        interaction.setInteractionWidth(1.2f);
        interaction.setInteractionHeight(2.0f);
        interaction.addScoreboardTag(tag);
        interaction.addScoreboardTag("MELEE_HITBOX");
    }

    public static void cleanupAllModels() {
        ACTIVE_ATTACKS.clear();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().stream().anyMatch(tag -> tag.startsWith("MELEE_GUARDIAN_"))) {
                    entity.remove();
                }
            }
        }
    }

    private void removeGuardianModel(Location loc) {
        String tag = getModelTag(loc);
        ACTIVE_ATTACKS.remove(tag);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.75, 0.5), 1.5, 2.5, 1.5)) {
            if (entity.getScoreboardTags().contains(tag)) {
                entity.remove();
            }
        }
    }

    private String getModelTag(Location loc) {
        return "MELEE_GUARDIAN_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public static void register(SlimefunAddon addon, ItemGroup category) {
        ItemStack[] recipe = new ItemStack[]{
                MilitaryComponents.FIREARM_BARREL, MilitaryComponents.ENERGY_MATRIX, new ItemStack(Material.NETHERITE_HELMET), new ItemStack(Material.NETHERITE_HELMET), MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.FIREARM_BARREL,
                MilitaryComponents.TURRET_SHELL, MilitaryComponents.QUANTUM_PROCESSOR, new ItemStack(Material.NETHERITE_CHESTPLATE), new ItemStack(Material.NETHERITE_CHESTPLATE), MilitaryComponents.QUANTUM_PROCESSOR, MilitaryComponents.TURRET_SHELL,
                MilitaryComponents.IMPACT_PISTON, MilitaryComponents.ADVANCED_MOVEMENT_CIRCUIT, new ItemStack(Material.NETHERITE_LEGGINGS), new ItemStack(Material.NETHERITE_LEGGINGS), MilitaryComponents.ADVANCED_MOVEMENT_CIRCUIT, MilitaryComponents.IMPACT_PISTON,
                MilitaryComponents.IMPACT_PISTON, MilitaryComponents.ADVANCED_MOVEMENT_CIRCUIT, new ItemStack(Material.NETHERITE_BOOTS), new ItemStack(Material.NETHERITE_BOOTS), MilitaryComponents.ADVANCED_MOVEMENT_CIRCUIT, MilitaryComponents.IMPACT_PISTON,
                MilitaryComponents.TURRET_SHELL, MilitaryComponents.QUANTUM_PROCESSOR, MilitaryComponents.TUNGSTEN_BLADE, MilitaryComponents.TUNGSTEN_BLADE, MilitaryComponents.QUANTUM_PROCESSOR, MilitaryComponents.TURRET_SHELL,
                MilitaryComponents.FIREARM_BARREL, MilitaryComponents.ENERGY_MATRIX, MilitaryVouchers.VOUCHER_COMMENDATION, MilitaryVouchers.VOUCHER_EMBLEM, MilitaryComponents.ENERGY_MATRIX, MilitaryComponents.FIREARM_BARREL
        };

        MeleeTurret turret = new MeleeTurret(category, MELEE_TURRET, recipe);
        turret.register(addon);
        if (addon instanceof Plugin plugin) {
            Bukkit.getPluginManager().registerEvents(turret, plugin);
        }
    }
}
