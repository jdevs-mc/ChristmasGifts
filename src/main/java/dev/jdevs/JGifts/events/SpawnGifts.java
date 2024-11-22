package dev.jdevs.JGifts.events;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static dev.jdevs.JGifts.Christmas.version_mode;
import static dev.jdevs.JGifts.Settings.*;
import static dev.jdevs.JGifts.utils.Configurations.*;

public class SpawnGifts implements Listener {
    public static List<UUID> time = new ArrayList<>();
    static int all_procent = launch.getInt("BaseSettings.spawn.mode.1.FullChance");
    static int procent = launch.getInt("BaseSettings.spawn.mode.1.Chance");
    static List<String> biomes = launch.getStringList("BaseSettings.spawn.biomes");
    public static List<String> worlds = launch.getStringList("BaseSettings.spawn.worlds");
    static String type_world = launch.getString("BaseSettings.spawn.type-worlds");
    static String type_biome = launch.getString("BaseSettings.spawn.type-biomes");
    static Integer every = launch.getInt("BaseSettings.spawn.mode.1.every");

    @EventHandler
    public void onMoveRandom(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        if (time.contains(uuid)) {
            return;
        }
        if (wg != 1 && procent >= ThreadLocalRandom.current().nextInt(all_procent)) {
            // We check the actions blocked for the player
            if (launch.getBoolean("BaseSettings.spawn.blocked.fly")) {
                if (p.isFlying()) {
                    return;
                }
            }
            if (launch.getBoolean("BaseSettings.spawn.blocked.shift")) {
                if (p.isSneaking()) {
                    return;
                }
            }
            if (CheckLimitGifts(p)) {
                Spawn(p);
            }
        } else {
            time.add(uuid);
            new BukkitRunnable() {
                @Override
                public void run() {
                    time.remove(uuid);
                }
            }.runTaskLater(Christmas.getInstance(), every);
        }
    }

    public static void EveryTime(int people, int seconds) {
        if (people == -1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!Bukkit.getOnlinePlayers().isEmpty()) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (CheckLimitGifts(p)) {
                                Spawn(p);
                            }
                        }
                    }
                }
            }.runTaskTimer(Christmas.getInstance(), seconds * 20L, seconds * 20L);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().size() == people) {
                        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                        new BukkitRunnable() {
                            int ok;

                            @Override
                            public void run() {
                                ok++;
                                Player p = players.get(ThreadLocalRandom.current().nextInt(players.size()));
                                if (CheckLimitGifts(p)) {
                                    Spawn(p);
                                }
                                if (ok == people) {
                                    cancel();
                                }
                            }
                        }.runTaskTimer(Christmas.getInstance(), 1, 1);
                    }
                }
            }.runTaskTimer(Christmas.getInstance(), seconds * 20L, seconds * 20L);
        }
    }

    static void Spawn(Player p) {
        Location loc = p.getLocation();
        // Checking the world and the biomes
        String world_name = loc.getWorld().getName().toLowerCase();
        if (type_world.contains("allowed")) {
            if (!worlds.contains(world_name)) {
                return;
            }
        } else {
            if (worlds.contains(world_name)) {
                return;
            }
        }
        String biome_name = loc.getBlock().getBiome().name().toLowerCase();
        if (type_biome.contains("allowed")) {
            if (!biomes.contains(biome_name)) {
                return;
            }
        } else {
            if (biomes.contains(biome_name)) {
                return;
            }
        }
        // Spawn a gifts
        fallBlockSpawn(p, SearchLocation(p, 3, loc.getYaw()));
    }
    public static void fallBlockSpawn(Player p, Location loc) {
        // Continue...
        Material mt;
        String type = config.getString("settings.gift.spawn.type");
        if (version_mode > 13) {
            mt = Material.getMaterial("BARREL");
            if (type != null && !type.contains("null")) {
                mt = Material.getMaterial(config.getString("settings.gift.spawn.type", "BARREL"));
            }
        }
        else if (version_mode == 13) {
            mt = Material.getMaterial("OAK_PLANKS");
            if (type != null && !type.contains("null")) {
                mt = Material.getMaterial(config.getString("settings.gift.spawn.type", "OAK_PLANKS"));
            }
        }
        else {
            try {
                // Ignore warnings
                @SuppressWarnings("all")
                Method mtd = Material.class.getMethod("getMaterial", int.class);
                //
                mt = (Material) mtd.invoke(Material.class, 5);
                if (config.get("settings.gift.spawn.id") != null) {
                    mt = (Material) mtd.invoke(Material.class, config.getInt("settings.gift.spawn.id", 5));
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
            fallingBlock.setMetadata(key, new FixedMetadataValue(Christmas.getInstance(), String.valueOf(p.getName())));
            fallingBlock.setDropItem(false);
            if (version_mode >= 8) {
                fallingBlock.setHurtEntities(false);
            }
        }
        UUID uuid = p.getUniqueId();
        time.add(uuid);
        new BukkitRunnable() {
            @Override
            public void run () {
                if (time.contains(uuid)) {
                    if (fallingBlock != null) {
                        time.remove(uuid);
                        fallingBlock.remove();
                    }
                }
            }
        }.runTaskLater(Christmas.getInstance(), config.getInt("settings.gift.spawn.timeLived") * 20L);
    }
    public static Location SearchLocation(Player p, int height, float yw) {
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
    public static Boolean CheckLimitGifts(Player p) {
        // Checking the limit gifts
        if (limit) {
            if (max <= nicknames.getInt("players." + p.getName(), 0)) {
                return false;
            }
            else {
                if (!nicknames.contains("players." + p.getName())) {
                    nicknames.set("players." + p.getName(), 0);
                }
                if (onCrashes) {
                    try {
                        nicknames.save(Configurations.getDataFolder("storage/db.yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
    public static void Update() {
        all_procent = launch.getInt("BaseSettings.spawn.mode.1.FullChance");
        every = launch.getInt("BaseSettings.spawn.mode.1.every");
        procent = launch.getInt("BaseSettings.spawn.mode.1.Chance");
        biomes = launch.getStringList("BaseSettings.spawn.biomes");
        worlds = launch.getStringList("BaseSettings.spawn.worlds");
        type_world = launch.getString("BaseSettings.spawn.type-worlds");
        type_biome = launch.getString("BaseSettings.spawn.type-biomes");
    }
}
