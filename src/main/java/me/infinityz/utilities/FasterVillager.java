package me.infinityz.utilities;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FasterVillager extends JavaPlugin implements Listener {

    private static final double JOB_SITE_SEARCH_RADIUS = 10.0;
    private static final Set<Material> JOB_SITE_BLOCKS = EnumSet.of(
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.CARTOGRAPHY_TABLE,
            Material.BREWING_STAND,
            Material.COMPOSTER,
            Material.BARREL,
            Material.FLETCHING_TABLE,
            Material.CAULDRON,
            Material.STONECUTTER,
            Material.LOOM,
            Material.SMITHING_TABLE,
            Material.GRINDSTONE,
            Material.LECTERN
    );
            private static final Map<Material, Villager.Profession> PROFESSIONS = Map.ofEntries(
                Map.entry(Material.BLAST_FURNACE, Villager.Profession.ARMORER),
                Map.entry(Material.SMOKER, Villager.Profession.BUTCHER),
                Map.entry(Material.CARTOGRAPHY_TABLE, Villager.Profession.CARTOGRAPHER),
                Map.entry(Material.BREWING_STAND, Villager.Profession.CLERIC),
                Map.entry(Material.COMPOSTER, Villager.Profession.FARMER),
                Map.entry(Material.BARREL, Villager.Profession.FISHERMAN),
                Map.entry(Material.FLETCHING_TABLE, Villager.Profession.FLETCHER),
                Map.entry(Material.CAULDRON, Villager.Profession.LEATHERWORKER),
                Map.entry(Material.LECTERN, Villager.Profession.LIBRARIAN),
                Map.entry(Material.STONECUTTER, Villager.Profession.MASON),
                Map.entry(Material.LO﻿OM, Villager.Profession.SHEPHERD),
                Map.entry(Material.SMITHING_TABLE, Villager.Profession.TOOLSMITH),
                Map.entry(Material.GRINDSTONE, Villager.Profession.WEAPONSMITH)
            );

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Material placed = event.getBlockPlaced().getType();
        if (!JOB_SITE_BLOCKS.contains(placed)) {
            return;
        }

        Location blockLocation = event.getBlockPlaced().getLocation();
        for (LivingEntity entity : blockLocation.getNearbyLivingEntities(JOB_SITE_SEARCH_RADIUS, JOB_SITE_SEARCH_RADIUS, JOB_SITE_SEARCH_RADIUS)) {
            if (!(entity instanceof Villager villager)) {
                continue;
            }

            if (isCandidate(villager) && canSeeWorkstation(villager, blockLocation)) {
                villager.setProfession(PROFESSIONS.get(placed));
                villager.setMemory(MemoryKey.JOB_SITE, blockLocation);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Material broken = event.getBlock().getType();
        if (!JOB_SITE_BLOCKS.contains(broken)) {
            return;
        }

        Location blockLocation = event.getBlock().getLocation();
        for (LivingEntity entity : blockLocation.getNearbyLivingEntities(JOB_SITE_SEARCH_RADIUS, JOB_SITE_SEARCH_RADIUS, JOB_SITE_SEARCH_RADIUS)) {
            if (!(entity instanceof Villager villager)) {
                continue;
            }

            if (Objects.equals(villager.getMemory(MemoryKey.JOB_SITE), blockLocation)) {
                villager.setMemory(MemoryKey.JOB_SITE, null);
                if (villager.getProfession() == PROFESSIONS.get(broken)
                        && villager.getVillagerLevel() == 1
                        && villager.getVillagerExperience() == 0) {
                    villager.setProfession(Villager.Profession.NONE);
                }
            }
        }
    }

    private boolean isCandidate(Villager villager) {
        return villager.isAdult()
                && villager.getProfession() == Villager.Profession.NONE
                && villager.getVillagerLevel() == 1
                && villager.getVillagerExperience() == 0;
    }

    private boolean canSeeWorkstation(Villager villager, Location workstation) {
        Location target = workstation.clone().add(0.5, 0.5, 0.5);
        Location eyeLocation = villager.getEyeLocation();
        Vector direction = target.toVector().subtract(eyeLocation.toVector());
        double distance = direction.length();
        if (distance == 0.0) {
            return true;
        }

        RayTraceResult hit = villager.getWorld().rayTraceBlocks(
                eyeLocation,
                direction.normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true
        );
        return hit == null || hit.getHitBlock() == workstation.getBlock();
    }
}