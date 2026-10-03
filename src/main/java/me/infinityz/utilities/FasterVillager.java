package me.infinityz.utilities;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

            if (isCandidate(villager) && villager.getMemory(MemoryKey.JOB_SITE) == null) {
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
            }
        }
    }

    private boolean isCandidate(Villager villager) {
        return villager.getVillagerLevel() == 1 && villager.getVillagerExperience() == 0;
    }
}