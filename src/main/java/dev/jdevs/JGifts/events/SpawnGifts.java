package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Values;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class SpawnGifts implements Listener {
    private final Christmas plugin;
    private final Values values;
    public SpawnGifts(Christmas plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
    }

    @EventHandler
    public void onMoveRandom(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        if (values.getTime().contains(uuid)) {
            return;
        }
        if (values.getWg() != 1 && values.getProcent() >= ThreadLocalRandom.current().nextInt(values.getAll_procent())) {
            // We check the actions blocked for the player
            if (values.isFly()) {
                if (p.isFlying()) {
                    return;
                }
            }
            if (values.isShift()) {
                if (p.isSneaking()) {
                    return;
                }
            }
            if (checkLimitGifts(p)) {
                Spawn(p);
            }
        } else {
            values.getTime().add(uuid);
            new BukkitRunnable() {
                @Override
                public void run() {
                    values.getTime().remove(uuid);
                }
            }.runTaskLater(plugin, values.getEvery());
        }
    }

    public void EveryTime(int people, int seconds) {
        if (people == -1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().size() != 0) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (checkLimitGifts(p)) {
                                Spawn(p);
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, seconds * 20L, seconds * 20L);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().size() >= people) {
                        task(people);
                    }
                }
            }.runTaskTimer(plugin, seconds * 20L, seconds * 20L);
        }
    }
    void task(int people) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        new BukkitRunnable() {
            int ok = 0;

            @Override
            public void run() {
                ok++;
                Player p = players.get(ThreadLocalRandom.current().nextInt(players.size()));
                if (checkLimitGifts(p)) {
                    Spawn(p);
                }
                if (ok >= people) {
                    players.clear();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    void Spawn(Player p) {
        Location loc = p.getLocation();
        // Checking the world and the biomes
        String world_name = loc.getWorld().getName().toLowerCase();
        if (values.getType_world().contains("allowed")) {
            if (!values.getWorlds().contains(world_name)) {
                return;
            }
        } else {
            if (values.getWorlds().contains(world_name)) {
                return;
            }
        }
        String biome_name = loc.getBlock().getBiome().name().toLowerCase();
        if (values.getType_biome().contains("allowed")) {
            if (!values.getBiomes().contains(biome_name)) {
                return;
            }
        } else {
            if (values.getBiomes().contains(biome_name)) {
                return;
            }
        }
        // Spawn a gifts
        fallBlockSpawn(p, searchLocation(p, 3, loc.getYaw()), "");
    }
    public void fallBlockSpawn(Player p, Location loc, String force) {
        // Continue...
        Material mt;
        String type = values.getType();
        int version_mode = plugin.getVersion_mode();
        if (version_mode > 13) {
            mt = Material.getMaterial("BARREL");
            if (type != null && !type.contains("null")) {
                mt = Material.getMaterial(type);
            }
        }
        else if (version_mode == 13) {
            mt = Material.getMaterial("OAK_PLANKS");
            if (type != null && !type.contains("null")) {
                mt = Material.getMaterial(type);
            }
        }
        else {
            try {
                // Ignore warnings
                @SuppressWarnings("all")
                Method mtd = Material.class.getMethod("getMaterial", int.class);
                //
                mt = (Material) mtd.invoke(Material.class, 5);
                if (values.getId() != 0) {
                    mt = (Material) mtd.invoke(Material.class, values.getId());
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        FallingBlock fallingBlock;
        if (version_mode <= 12) {
            try {
                Class<?>[] parameters = new Class<?>[]{Location.class, Material.class, byte.class};
                Method methods = p.getWorld().getClass().getMethod("spawnFallingBlock", parameters);
                Object[] args = new Object[]{loc, mt, (byte) 12};
                fallingBlock = (FallingBlock) methods.invoke(p.getWorld(), args);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            fallingBlock = p.getWorld().spawnFallingBlock(loc, mt.createBlockData());
        }
        if (fallingBlock != null) {
            fallingBlock.setMetadata(values.getKey() + force, new FixedMetadataValue(plugin, String.valueOf(p.getName())));
            fallingBlock.setDropItem(false);
            if (version_mode >= 8) {
                fallingBlock.setHurtEntities(false);
            }
        }
        UUID uuid = p.getUniqueId();
        values.getTime().add(uuid);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fallingBlock != null) {
                    if (values.getTime().contains(uuid)) {
                        values.getTime().remove(uuid);
                        fallingBlock.remove();
                    }
                }
            }
        }.runTaskLater(plugin, values.getTimeLived() * 20L);
    }
    public Location searchLocation(Player p, int height, float yw) {
        Location loc = p.getLocation();
        yw = (yw % 360 + 360) % 360;
        // Spawn a gifts
        double X = loc.getX();
        double Y = loc.getY() + height;
        double Z = loc.getZ();
        if (yw >= 316 || yw <= 45) {
            Z = Z + 1;
        } else if (226 <= yw && yw <= 315) {
            X = X + 1;
        } else if (46 <= yw && 135 >= yw) {
            X = X - 1;
        } else if (136 <= yw && yw <= 225) {
            Z = Z - 1;
        }
        return new Location(loc.getWorld(), X, Y, Z);
    }
    public Boolean checkLimitGifts(Player p) {
        // Checking the limit gifts
        if (values.isLimit()) {
            YamlConfiguration nicknames = plugin.getNicknames();
            if (values.getMax() <= nicknames.getInt("players." + p.getName(), 0)) {
                return false;
            }
            else {
                if (!nicknames.contains("players." + p.getName())) {
                    nicknames.set("players." + p.getName(), 0);
                }
                if (values.isOnCrashes()) {
                    try {
                        nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
}
