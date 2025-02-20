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
import dev.jdevs.JGifts.loots.Load;
import dev.jdevs.JGifts.made.MessageLanguage;
import dev.jdevs.JGifts.supports.WG;
import dev.jdevs.JGifts.utils.Message;
import dev.jdevs.JGifts.utils.Values;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.clip.placeholderapi.PlaceholderAPI;
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
    private final Christmas plugin;
    private final String key;
    private final Values values;
    private final Load load;
    private final WG wg;
    private final Message messages;
    private final MessageLanguage sends;
    private final int version;
    public FallGifts(Christmas plugin) {
        this.plugin = plugin;
        wg = plugin.getWg();
        load = plugin.getLoad();
        values = plugin.getValues();
        messages = plugin.getMessages();
        sends = plugin.getSends();
        version = plugin.getVersion_mode();
        key = values.getKey();
    }
    private void sendMessage(Player sender, String text, Location loc) {
        messages.sendMessage(sender, text, loc);
    }
    @EventHandler
    void FallBlock(EntityChangeBlockEvent event) {
        Entity ent = event.getEntity();
        if (ent instanceof FallingBlock) {
            if (!ent.hasMetadata(key) && !ent.hasMetadata(key + "_force")) {
                return;
            }
            event.setCancelled(true);
            ((FallingBlock) ent).setDropItem(false);
            Player p;
            if (ent.hasMetadata(key)) {
                p = Bukkit.getPlayer(String.valueOf(ent.getMetadata(key).get(0).value()));
            }
            else {
                p = Bukkit.getPlayer(String.valueOf(ent.getMetadata(key + "_force").get(0).value()));
            }
            Block b = ent.getLocation().getBlock();
            Location b_loc = b.getLocation();
            if (values.isWorldGuard()) {
                if (!checkWorldGuard(b_loc, p)) {
                    return;
                }
            }
            if (!values.getSpawnGifts().checkLimitGifts(p)) {
                if (!ent.hasMetadata(key + "_force")) {
                    return;
                }
            }
            if (values.isFirework()) {
                spawnFirework(b_loc);
            }
            UUID uuid = p.getUniqueId();
            values.getTime().remove(uuid);
            values.getGifts().put(b_loc, p.getUniqueId());
            String locale = "";
            if (values.getMessageMode() == 2) {
                if (version <= 12) {
                    //noinspection deprecation
                    locale = p.spigot().getLocale().toLowerCase();
                } else {
                    locale = p.getLocale().toLowerCase();
                }
            }
            startValues(b_loc, p, locale);
            if (values.isAutoGive()) {
                return;
            }
            setSkinGift(b, b_loc);
            ((FallingBlock) ent).setDropItem(false);
            String hologramType = values.getHologramType();
            if (hologramType != null && !hologramType.contains("null")) {
                createHolograms(hologramType, b_loc, p, locale);
            }
            // We delete the gift in case of inactivity
            deleteTaskGift(b, b_loc, p, locale);
        }
    }
    private void setSkinGift(Block b, Location b_loc) {
        Skull skin;
        if (version > 12) {
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
            if (version >= 8) {
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
                //noinspection deprecation
                skin.setOwner("defib");
            }
        }
        skin.update();
    }
    private void createHolograms(String hologramType, Location b_loc, Player player, String locale) {
        String gen_hol = values.generateText(8);
        double Y = b_loc.getY() + values.getHeight();
        Location hdloc = new Location(b_loc.getWorld(), b_loc.getX() + 0.5, Y, b_loc.getZ() + 0.5);
        if (hologramType.contains("decentholograms")) {
            if (DHAPI.getHologram(gen_hol) != null) {
                return;
            }
            Hologram hd = DHAPI.createHologram(gen_hol, hdloc);
            try {
                if (values.getMessageMode() == 2 && values.getHdstrings().containsKey(locale)) {
                    for (String lore : values.getHdstrings().get(locale)) {
                        lore = lore.
                                replace("%player%", player.getName());
                        if (values.isPlaceholderAPI()) {
                            lore = PlaceholderAPI.setPlaceholders(player, lore);
                        }
                        DHAPI.addHologramLine(hd, lore);
                    }
                }
                else {
                    for (String lore : values.getHdstrings().get("default")) {
                        lore = lore.
                                replace("%player%", player.getName());
                        if (values.isPlaceholderAPI()) {
                            lore = PlaceholderAPI.setPlaceholders(player, lore);
                        }
                        DHAPI.addHologramLine(hd, lore);
                    }
                }
            } catch (IllegalArgumentException e) {
                sends.send("error", null, "settings.holograms.lines");
            }
            values.getDecentHolograms().put(b_loc, hd);
        } else {
            HolographicDisplaysAPI api = HolographicDisplaysAPI.get(plugin);
            me.filoghost.holographicdisplays.api.hologram.Hologram hd = api.createHologram(hdloc);
            int line = 0;
            try {
                if (values.getMessageMode() == 2 && values.getHdstrings().containsKey(locale)) {
                    for (String lore : values.getHdstrings().get(locale)) {
                        lore = lore.
                                replace("%player%", player.getName());
                        if (values.isPlaceholderAPI()) {
                            lore = PlaceholderAPI.setPlaceholders(player, lore);
                        }
                        hd.getLines().insertText(line, messages.hex(lore));
                        line++;
                    }
                }
                else {
                    for (String lore : values.getHdstrings().get("default")) {
                        lore = lore.
                                replace("%player%", player.getName());
                        if (values.isPlaceholderAPI()) {
                            lore = PlaceholderAPI.setPlaceholders(player, lore);
                        }
                        hd.getLines().insertText(line, messages.hex(lore));
                        line++;
                    }
                }
            } catch (IllegalArgumentException e) {
                sends.send("error", null, "settings.holograms.lines");
            }
            values.getHolographicDisplays().put(b_loc, hd);
        }
    }
    private boolean checkWorldGuard(Location b_loc, Player p) {
        RegionManager regionManager = wg.getRegionManager(b_loc);
        int wg = values.getWg();
        if (wg == 2 || wg == 3) {
            ApplicableRegionSet regionSet;
            if (version > 12) {
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
                    return regionSet.isOwnerOfAll(p2);
                }
            } else {
                return regionSet.getRegions().isEmpty();
            }
        }
        return true;
    }
    private void startValues(Location b_loc, Player p, String locale) {
        // Add limit gift
        if (values.isLimit()) {
            YamlConfiguration nicknames = plugin.getNicknames();
            String key = "players." + p.getName();
            nicknames.set(key, nicknames.getInt(key) + 1);
            if (values.isOnCrashes()) {
                try {
                    nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (values.getMessageMode() == 2 && values.getStart_gifts().containsKey(locale)) {
            for (String lore : values.getStart_gifts().get(locale)) {
                sendMessage(p, lore, b_loc);
            }
        }
        else {
            for (String lore : values.getStart_gifts().get("default")) {
                sendMessage(p, lore, b_loc);
            }
        }
        if (values.isAutoGive()) {
            load.dropLoot(b_loc);
            values.getGifts().remove(b_loc);
            return;
        }
        if (values.isOnCrashes()) {
            YamlConfiguration nicknames = plugin.getNicknames();
            List<String> gifts2 = new ArrayList<>();
            if (nicknames.getStringList("gifts") != null) {
                gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
            }
            gifts2.add(b_loc.getWorld().getName() + ":" + b_loc.getX() + ":" + b_loc.getY() + ":" + b_loc.getZ() + ":" + p.getName());
            nicknames.set("gifts", gifts2);
            try {
                nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void deleteTaskGift(Block b, Location b_loc, Player p, String locale) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (values.getGifts().containsKey(b_loc)) {
                    if (values.isTakedLoot()) {
                        load.dropLoot(b_loc);
                    }
                    else {
                        YamlConfiguration nicknames = plugin.getNicknames();
                        nicknames.set("players." + p.getName(), nicknames.getInt("players." + p.getName()) - 1);
                    }
                    wg.setBlock(b_loc, b);
                    values.getGifts().remove(b_loc);
                    if (!values.getDecentHolograms().isEmpty() || !values.getHolographicDisplays().isEmpty()) {
                        if (values.getDecentHolograms().get(b_loc) != null) {
                            values.getDecentHolograms().get(b_loc).delete();
                            values.getDecentHolograms().remove(b_loc);
                        }
                        else if (values.getHolographicDisplays().get(b_loc) != null) {
                            values.getHolographicDisplays().get(b_loc).delete();
                            values.getHolographicDisplays().remove(b_loc);
                        }
                    }
                    plugin.removeGifts(b_loc, p.getName());
                    if (values.getMessageMode() == 2 && values.getStop_gifts().containsKey(locale)) {
                        for (String lore : values.getStop_gifts().get(locale)) {
                            sendMessage(p, lore, b_loc);
                        }
                    }
                    else {
                        for (String lore : values.getStop_gifts().get("default")) {
                            sendMessage(p, lore, b_loc);
                        }
                    }
                    cancel();
                } else {
                    cancel();
                }
            }
        }.runTaskLater(plugin, values.getRemove() * 20L);
    }
    private void spawnFirework(Location location2) {
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
}
