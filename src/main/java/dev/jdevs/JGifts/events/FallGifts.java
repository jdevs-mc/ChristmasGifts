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
import dev.jdevs.JGifts.utils.Values;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class FallGifts implements Listener {
    Christmas plugin;
    String key;
    private final Values values;
    public FallGifts(Christmas plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        key = values.getKey();
    }
    private void sendMessage(Player sender, String text) {
        plugin.getMessages().sendMessage(sender, text);
    }
    @EventHandler
    void FallBlock(EntityChangeBlockEvent event) {
        Entity ent = event.getEntity();
        if (ent instanceof FallingBlock) {
            if (!ent.hasMetadata(key)) {
                return;
            }
            event.setCancelled(true);
            ((FallingBlock) ent).setDropItem(false);
            Player p = Bukkit.getPlayer(String.valueOf(ent.getMetadata(key).get(0).value()));
            Block b = ent.getLocation().getBlock();
            Location b_loc = b.getLocation();
            // Checking the WorldGuard
            if (values.isWorldGuard()) {
                RegionManager regionManager = plugin.getWg().getRegionManager(b_loc);
                int wg = values.getWg();
                if (wg == 2 || wg == 3) {
                    ApplicableRegionSet regionSet;
                    int version_mode = plugin.getVersion_mode();
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
                    if (wg == 2) {
                        LocalPlayer p2 = WorldGuardPlugin.inst().wrapPlayer(p);
                        if (!regionSet.isMemberOfAll(p2)) {
                            if (!regionSet.isOwnerOfAll(p2)) {
                                return;
                            }
                        }
                    } else {
                        if (!regionSet.getRegions().isEmpty()) {
                            return;
                        }
                    }
                }
            }
            // We save the previous data of the block and replace it with our gift
            if (values.isFirework()) {
                spawnFirework(b_loc);
            }
            // Add limit gift
            if (values.isLimit()) {
                YamlConfiguration nicknames = plugin.getNicknames();
                nicknames.set("players." + p.getName(), nicknames.getInt("players." + p.getName()) + 1);
                if (values.isOnCrashes()) {
                    try {
                        nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (String lore : values.getStart_gift()) {
                sendMessage(p, lore);
            }
            int version_mode = plugin.getVersion_mode();
            UUID uuid = p.getUniqueId();
            values.getTime().remove(uuid);
            if (values.isAutoGive()) {
                plugin.getLoad().dropLoot(b_loc);
                return;
            }
            if (values.isOnCrashes()) {
                YamlConfiguration nicknames = plugin.getNicknames();
                List<String> gifts2 = new ArrayList<>();
                if (nicknames.getStringList("gifts") != null) {
                    gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
                }
                gifts2.add(b_loc.getWorld().getName() + ":" + b_loc.getX() + ":" + b_loc.getY() + ":" + b_loc.getZ());
                nicknames.set("gifts", gifts2);
                try {
                    nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Skull skin;
            if (version_mode > 12) {
                values.getSaveBlock().put(b_loc, b.getBlockData());
                b.setType(Material.PLAYER_HEAD);
                skin = (Skull) b.getState();
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", values.getTexture()));
                skin.setPlayerProfile(profile);
            } else {
                try {
                    Method method = ((Object) b).getClass().getMethod("getData");
                    values.getSaveBlock_12().put(b_loc, new AbstractMap.SimpleEntry<>(b.getType(), (Byte) method.invoke(b)));
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
                        profile.getProperties().put("textures", new Property("textures", values.getTexture()));
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
            values.getGifts().put(b_loc, p);
            ((FallingBlock) ent).setDropItem(false);
            String hologramType = values.getHologramType();
            // Creating a hologram
            if (hologramType != null && !hologramType.contains("null")) {
                String gen_hol = values.generateText(8);
                double Y = b_loc.getY() + values.getHeight();
                Location hdloc = new Location(b.getWorld(), b_loc.getX() + 0.5, Y, b_loc.getZ() + 0.5);
                if (hologramType.contains("decentholograms")) {
                    if (DHAPI.getHologram(gen_hol) != null) {
                        return;
                    }
                    Hologram hd = DHAPI.createHologram(gen_hol, hdloc);
                    try {
                        for (String lore : values.getHdstring()) {
                            DHAPI.addHologramLine(hd, lore);
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getSends().send("error", null, "settings.holograms.lines");
                    }
                    values.getDecentHolograms().put(b_loc, hd);
                }
                else {
                    HolographicDisplaysAPI api = HolographicDisplaysAPI.get(plugin);
                    me.filoghost.holographicdisplays.api.hologram.Hologram hd = api.createHologram(hdloc);
                    int line = 0;
                    try {
                        for (String lore : values.getHdstring()) {
                            hd.getLines().insertText(line, plugin.getMessages().hex(lore));
                            line++;
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getSends().send("error", null, "settings.holograms.lines");
                    }
                    values.getHolographicDisplays().put(b_loc, hd);
                }

            }
            // We delete the gift in case of inactivity
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (values.getGifts().containsKey(b_loc)) {
                        if (!values.isTakedLoot()) {
                            plugin.getLoad().dropLoot(b_loc);
                        }
                        plugin.getWg().setBlock(b_loc, b);
                        values.getGifts().remove(b_loc);
                        if (hologramType != null) {
                            if (hologramType.contains("decentholograms") && values.getDecentHolograms().get(b_loc) != null) {
                                values.getDecentHolograms().get(b_loc).delete();
                                values.getDecentHolograms().remove(b_loc);
                            }
                            else if (values.getHolographicDisplays().get(b_loc) != null) {
                                values.getHolographicDisplays().get(b_loc).delete();
                                values.getHolographicDisplays().remove(b_loc);
                            }
                        }
                        removeGifts(b_loc);
                        for (String lore : values.getStop_gift()) {
                            sendMessage(p, lore);
                        }
                        cancel();
                    } else {
                        cancel();
                    }
                }
            }.runTaskLater(plugin, values.getRemove() * 20L);
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
    public void removeGifts(Location b_loc) {
        if (values.isOnCrashes()) {
            YamlConfiguration nicknames = plugin.getNicknames();
            if (nicknames.getStringList("gifts") != null) {
                List<String> gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
                gifts2.remove(b_loc.getWorld().getName() + ":" + b_loc.getX() + ":" + b_loc.getY() + ":" + b_loc.getZ());
                nicknames.set("gifts", gifts2);
                try {
                    nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
