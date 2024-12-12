package dev.jdevs.JGifts.supports;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Values;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public final class WG {
    private final Christmas plugin;
    private final Values values;
    public WG(Christmas plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
    }
    public RegionManager getRegionManager(Location w) {
        if (plugin.getVersion_mode() > 12) {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w.getWorld()));
        } else {
            try {
                Method method = WorldGuardPlugin.inst().getClass().getMethod("getRegionManager", World.class);
                return (RegionManager)method.invoke(WorldGuardPlugin.inst(), w.getWorld());
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException var4) {
                throw new RuntimeException(var4);
            }
        }
    }

    public void setBlock(Location block, org.bukkit.block.Block b) {
        if (plugin.getVersion_mode() > 12) {
            if (values.getSaveBlock().get(block) != null) {
                BlockData blockData = values.getSaveBlock().get(block);
                String material = blockData.getMaterial().name().toLowerCase();
                String check = block.getBlock().getType().name().toLowerCase();
                if (check.contains("nether") || check.contains("end")) {
                    return;
                }
                if (material.contains("lava") || material.contains("fire") || material.contains("nether") || material.contains("end")) {
                    values.getSaveBlock().remove(b.getLocation());
                    b.setBlockData(Material.AIR.createBlockData());
                }
                else {
                    b.setBlockData(blockData);
                    values.getSaveBlock().remove(b.getLocation());
                }
            }
        }
        else {
            if (values.getSaveBlock_12().get(block) != null) {
                Map.Entry<Material, Byte> entry = values.getSaveBlock_12().get(block);
                Material mt = entry.getKey();
                String material = mt.name().toLowerCase();
                String check = block.getBlock().getType().name().toLowerCase();
                if (check.contains("nether") || check.contains("end")) {
                    return;
                }
                if (material.contains("lava") || material.contains("fire")) {
                    b.setType(Material.AIR);
                    values.getSaveBlock_12().remove(block);
                    block.getBlock().getState().update();
                }
                else {
                    block.getBlock().setType(mt);
                    try {
                        Class<?>[] parameters = new Class<?>[]{byte.class};
                        Object[] args = new Object[]{entry.getValue()};
                        b.getClass().getMethod("setData", parameters).invoke(b, args);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    block.getBlock().getState().update();
                    values.getSaveBlock_12().remove(block);
                }
            }
        }
    }
}
