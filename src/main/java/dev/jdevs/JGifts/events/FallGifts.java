package dev.jdevs.JGifts.events;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.Settings;
import dev.jdevs.JGifts.made.MessageLanguage;
import dev.jdevs.JGifts.utils.Configurations;
import dev.jdevs.JGifts.utils.Message;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static dev.jdevs.JGifts.Christmas.version_mode;
import static dev.jdevs.JGifts.Settings.*;
import static dev.jdevs.JGifts.loots.Loot.dropLoot;
import static dev.jdevs.JGifts.made.CustomMessages.*;
import static dev.jdevs.JGifts.supports.WG.*;
import static dev.jdevs.JGifts.utils.Configurations.config;
import static dev.jdevs.JGifts.utils.Configurations.nicknames;
import static dev.jdevs.JGifts.utils.Message.hex;

public class FallGifts implements Listener {
    public static Map<Location, Player> gifts = new HashMap<>();
    public static HashMap<Location, BlockData> saveBlock = new HashMap<>();
    public static HashMap<Location, Map.Entry<Material, Byte>> saveBlock_12 = new HashMap<>();
    public static HashMap<Location, Hologram> decentHolograms = new HashMap<>();
    public static HashMap<Location, me.filoghost.holographicdisplays.api.hologram.Hologram> holographicDisplays = new HashMap<>();
    @EventHandler
    void FallBlock(EntityChangeBlockEvent event) {
        Entity ent = event.getEntity();
        if (ent instanceof FallingBlock) {
            if (!ent.hasMetadata(Settings.key)) {
                return;
            }
            event.setCancelled(true);
            ((FallingBlock) ent).setDropItem(false);
            Player p = Bukkit.getPlayer(String.valueOf(ent.getMetadata(Settings.key).get(0).value()));
            Block b = ent.getLocation().getBlock();
            Location b_loc = b.getLocation();
            // Checking the WorldGuard
            if (Settings.WorldGuard) {
                RegionManager regionManager = getRegionManager(b_loc);
                if (Settings.wg == 2 || Settings.wg == 3) {
                    ApplicableRegionSet regionSet;
                    if (version_mode > 12) {
                        BlockVector3 vec = BlockVector3.at(b_loc.getX(), b_loc.getY(), b_loc.getZ());
                        regionSet = regionManager.getApplicableRegions(vec);
                    } else {
                        try {
                            Class<?>[] parameters_regions = new Class<?>[]{Location.class};
                            // Ignore warnings
                            @SuppressWarnings("all")
                            Method methods = regionManager.getClass().getMethod("getApplicableRegions", parameters_regions);
                            //
                            regionSet = (ApplicableRegionSet) methods.invoke(regionManager, b_loc);
                        } catch (NoSuchMethodException | IllegalAccessException |
                                 InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (Settings.wg == 2) {
                        LocalPlayer p2 = WorldGuardPlugin.inst().wrapPlayer(p);
                        if (!regionSet.isMemberOfAll(p2)) {
                            if (!regionSet.isOwnerOfAll(p2)) {
                                return;
                            }
                        }
                    } else if (Settings.wg == 3) {
                        if (!regionSet.getRegions().isEmpty()) {
                            return;
                        }
                    }
                }
            }
            // We save the previous data of the block and replace it with our gift
            if (Configurations.config.getBoolean("settings.gift.spawn.firework")) {
                spawnFirework(b_loc);
            }
            // Add limit gift
            if (limit) {
                nicknames.set("players." + p.getName(), nicknames.getInt("players." + p.getName()) + 1);
                if (onCrashes) {
                    try {
                        nicknames.save(Configurations.getDataFolder("storage/db.yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (String lore : start_gift) {
                Message.sendMessage(p, lore);
            }
            UUID uuid = p.getUniqueId();
            SpawnGifts.time.remove(uuid);
            if (autoGive) {
                dropLoot(b_loc);
                return;
            }
            if (onCrashes) {
                List<String> gifts2 = new ArrayList<>();
                if (nicknames.getStringList("gifts") != null) {
                    gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
                }
                gifts2.add(b_loc.getWorld().getName() + ":" + b_loc.getX() + ":" + b_loc.getY() + ":" + b_loc.getZ());
                nicknames.set("gifts", gifts2);
                try {
                    nicknames.save(Configurations.getDataFolder("storage/db.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Skull skin;
            if (version_mode > 12) {
                saveBlock.put(b_loc, b.getBlockData());
                b.setType(Material.PLAYER_HEAD);
                skin = (Skull) b.getState();
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", Configurations.config.getString("settings.gift.texture")));
                skin.setPlayerProfile(profile);
            } else {
                try {
                    Method method = ((Object) b).getClass().getMethod("getData");
                    saveBlock_12.put(b_loc, new AbstractMap.SimpleEntry<>(b.getType(), (Byte) method.invoke(b)));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                b.setType(Material.valueOf("SKULL"));
                skin = (Skull) b.getState();
                //noinspection deprecation
                skin.setSkullType(SkullType.PLAYER);
                //noinspection deprecation
                skin.setRawData((byte) 1);
                if (version_mode >= 8) {
                    try {
                        GameProfile profile = new GameProfile(UUID.randomUUID(), "gift");
                        profile.getProperties().put("textures", new Property("textures", Configurations.config.getString("settings.gift.texture")));
                        Field profileField = skin.getClass().getDeclaredField("profile");
                        profileField.setAccessible(true);
                        profileField.set(skin, profile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    skin.setOwner("defib");
                }
            }
            skin.update();
            // We save our gift and notify you about the spawn with the help of fireworks
            gifts.put(b_loc, p);
            ((FallingBlock) ent).setDropItem(false);
            // Creating a hologram
            if (HologramType != null) {
                String gen_hol = Settings.generateText(8);
                double Y = b_loc.getY() + Configurations.config.getDouble("settings.holograms.height");
                Location hdloc = new Location(b.getWorld(), b_loc.getX() + 0.5, Y, b_loc.getZ() + 0.5);
                if (HologramType.contains("decentholograms")) {
                    if (DHAPI.getHologram(gen_hol) != null) {
                        return;
                    }
                    Hologram hd = DHAPI.createHologram(gen_hol, hdloc);
                    try {
                        for (String lore : hdstring) {
                            DHAPI.addHologramLine(hd, lore);
                        }
                    } catch (IllegalArgumentException e) {
                        MessageLanguage.send("error", null, "settings.holograms.lines");
                    }
                    decentHolograms.put(b_loc, hd);
                }
                else {
                    HolographicDisplaysAPI api = HolographicDisplaysAPI.get(Christmas.getInstance());
                    me.filoghost.holographicdisplays.api.hologram.Hologram hd = api.createHologram(hdloc);
                    int line = 0;
                    try {
                        for (String lore : hdstring) {
                            hd.getLines().insertText(line, hex(lore));
                            line++;
                        }
                    } catch (IllegalArgumentException e) {
                        MessageLanguage.send("error", null, "settings.holograms.lines");
                    }
                    holographicDisplays.put(b_loc, hd);
                }

            }
            // We delete the gift in case of inactivity
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (gifts.containsKey(b_loc)) {
                        if (takedLoot) {
                            dropLoot(b_loc);
                        }
                        setBlock(b_loc, b);
                        gifts.remove(b_loc);
                        if (HologramType != null) {
                            if (HologramType.contains("decentholograms") && decentHolograms.get(b_loc) != null) {
                                decentHolograms.get(b_loc).delete();
                                decentHolograms.remove(b_loc);
                            }
                            else if (holographicDisplays.get(b_loc) != null) {
                                holographicDisplays.get(b_loc).delete();
                                holographicDisplays.remove(b_loc);
                            }
                        }
                        removeGifts(b_loc);
                        for (String lore : stop_gift) {
                            Message.sendMessage(p, lore);
                        }
                        cancel();
                    } else {
                        cancel();
                    }
                }
            }.runTaskLater(Christmas.getInstance(), config.getInt("settings.gift.remove") * 20L);
        }
    }
    void spawnFirework(Location location2) {
        Location location = location2.clone().add(0.5, 0.5, 0.5);
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(false)
                .trail(true)
                .with(FireworkEffect.Type.STAR)
                .withColor(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PURPLE)
                .build();

        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
    }
    public static void removeGifts(Location b_loc) {
        if (onCrashes) {
            if (nicknames.getStringList("gifts") != null) {
                List<String> gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
                gifts2.remove(b_loc.getWorld().getName() + ":" + b_loc.getX() + ":" + b_loc.getY() + ":" + b_loc.getZ());
                nicknames.set("gifts", gifts2);
                try {
                    nicknames.save(Configurations.getDataFolder("storage/db.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
