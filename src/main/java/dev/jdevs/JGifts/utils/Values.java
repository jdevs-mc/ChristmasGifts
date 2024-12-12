package dev.jdevs.JGifts.utils;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.events.*;
import dev.jdevs.JGifts.supports.PlaceholderAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Values {
    private final Christmas plugin;
    private String hologramType;
    private List<String> hdstring;
    private List<String> start_gift;
    private List<String> stop_gift;
    private List<String> success;
    private List<String> limit_message;
    private List<String> no_perm;
    private final Map<Location, UUID> gifts = new HashMap<>();
    private final Map<Location, BlockData> saveBlock = new HashMap<>();
    private final Map<Location, Map.Entry<Material, Byte>> saveBlock_12 = new HashMap<>();
    private final Map<Location, Hologram> decentHolograms = new HashMap<>();
    private final Map<Location, me.filoghost.holographicdisplays.api.hologram.Hologram> holographicDisplays = new HashMap<>();
    private final List<UUID> time = new ArrayList<>();
    private int all_procent;
    private int procent;
    private List<String> biomes;
    private List<String> worlds;
    private String type_world;
    private String type_biome;
    private String texture;
    private String type;
    private Integer every;
    private boolean debug;
    private boolean onCrashes;
    private boolean autoGive;
    private int wg;
    private final String key = "ChristmasGifts";
    private int id;
    private int timeLived;
    private boolean PlaceholderAPI;
    private boolean WorldGuard;
    private int max;
    private boolean limit;
    private AntiGrief antiGrief;
    private AntiGriefV8 antiGriefV8;
    private FallGifts fallGifts;
    private SpawnGifts spawnGifts;
    private UseGifts useGifts;
    private boolean takedLoot;
    private boolean grinch;
    private int chance;
    private boolean fly;
    private boolean shift;
    private boolean firework;
    private boolean Loots;
    private double height;
    private int remove;
    public Values(Christmas plugin) {
        this.plugin = plugin;
    }
    public void setupValues(boolean first) {
        YamlConfiguration launch = plugin.of("launch.yml");
        YamlConfiguration config = plugin.of("config.yml");
        setupSettings(launch, config);
        setupActions(config);
        if (first) {
            setupFirst(launch);
        }
    }
    private void setupFirst(YamlConfiguration launch) {
        YamlConfiguration nicknames = plugin.getNicknames();
        Server server = plugin.getServer();
        autoGive = launch.getBoolean("BaseSettings.autoGive");
        if (!autoGive) {
            antiGrief = new AntiGrief(plugin);
            server.getPluginManager().registerEvents(antiGrief, plugin); // 1 action, launch protection
            if (plugin.getVersion_mode() >= 8) {
                antiGriefV8 = new AntiGriefV8(plugin);
                server.getPluginManager().registerEvents(antiGriefV8, plugin); // 1 action, launch protection
            }
            useGifts = new UseGifts(plugin);
            server.getPluginManager().registerEvents(useGifts, plugin); // The player's interaction with the gift to receive it
        }
        ConfigurationSection mode = launch.getConfigurationSection("BaseSettings.spawn.mode");
        spawnGifts = new SpawnGifts(plugin);
        if (mode.getInt("enabled") == 1) {
            server.getPluginManager().registerEvents(spawnGifts, plugin); // We are starting to spawn gifts
        } else if (mode.getInt("enabled") == 2) {
            if (!mode.getString("2.people").contains("null")) {
                spawnGifts.EveryTime(mode.getInt("2.people"), mode.getInt("2.every"));
            } else {
                Bukkit.getLogger().warning("How many people will receive gifts once in how many is not specified, the mode 2 is disabled.");
            }
        }
        fallGifts = new FallGifts(plugin);
        server.getPluginManager().registerEvents(fallGifts, plugin); // We receive a gift using FallBlock and set conditions
        if (Loots) {
            plugin.getLoad().loadLoots();
        }
        onCrashes = launch.getBoolean("BaseSettings.onCrashes.enabled");
        if (onCrashes) {
            if (nicknames.getStringList("gifts") != null) {
                List<String> gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
                for (String locs : nicknames.getStringList("gifts")) {
                    String[] split = locs.split(":");
                    Location loc = new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
                    plugin.getLoad().dropLoot(loc);
                    gifts2.remove(locs);
                }
                nicknames.set("gifts", gifts2);
                try {
                    nicknames.save(new File(plugin.getDataFolder(), "storage/db.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (PlaceholderAPI) {
            placeholderAPI = new PlaceholderAPI(plugin);
            connectPlaceholderAPI();
        }
    }
    private void setupSettings(YamlConfiguration launch, YamlConfiguration config) {
        ConfigurationSection section = launch.getConfigurationSection("BaseSettings");
        ConfigurationSection spawn = section.getConfigurationSection("spawn");
        ConfigurationSection supports = section.getConfigurationSection("supports");
        ConfigurationSection settings = config.getConfigurationSection("settings");
        hologramType = supports.getString("HologramType");
        if (supports.getString("HologramType") != null && !supports.getString("HologramType").contains("null")) {
            if (!Bukkit.getPluginManager().isPluginEnabled(hologramType)) {
                plugin.setDisabled(true);
                plugin.getLogger().warning("\nInstall plugins on the server: " + hologramType +
                        "\nУстановите на сервер плагин: " + hologramType + "\n");
                return;
            }
            hologramType = hologramType.toLowerCase();
        }
        PlaceholderAPI = supports.getBoolean("PlaceholderAPI");
        WorldGuard = supports.getBoolean("WorldGuard");
        debug = section.getBoolean("debug");
        all_procent = spawn.getInt("mode.1.FullChance");
        procent = spawn.getInt("mode.1.Chance");
        biomes = spawn.getStringList("biomes");
        worlds = spawn.getStringList("worlds");
        type_world = spawn.getString("type-worlds");
        type_biome = spawn.getString("type-biomes");
        every = spawn.getInt("mode.1.every");
        wg = spawn.getInt("wg_support", 3);
        fly = spawn.getBoolean("blocked.fly");
        shift = spawn.getBoolean("blocked.shift");
        max = settings.getInt("gift.max");
        limit = settings.getBoolean("gift.limit");
        takedLoot = settings.getBoolean("loot.taked");
        Loots = settings.getBoolean("loot.enabled");
        grinch = settings.getBoolean("grinch.enabled");
        chance = settings.getInt("grinch.chance");
        firework = settings.getBoolean("gift.spawn.firework");
        texture = settings.getString("gift.texture");
        height = settings.getDouble("holograms.height");
        remove = settings.getInt("gift.remove");
        type = settings.getString("gift.spawn.type");
        id = settings.getInt("gift.spawn.id", 5);
        timeLived = settings.getInt("gift.spawn.timeLived");
    }
    private void setupActions(YamlConfiguration config) {
        hdstring = config.getStringList("settings.holograms.lines");
        start_gift = config.getStringList("actions.gift.spawn");
        stop_gift = config.getStringList("actions.gift.loss");
        success = config.getStringList("actions.gift.success");
        limit_message = config.getStringList("actions.gift.limit");
        no_perm = config.getStringList("actions.no_perm");
    }
    PlaceholderAPI placeholderAPI = null;
    private void connectPlaceholderAPI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    placeholderAPI.register();
                    Bukkit.getLogger().info("[ChristmasGifts] Connection to PlaceholderAPI was successful!");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }
    public String generateText(int length) {
        String symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(symbols.length());
            char ChaR = symbols.charAt(index);
            sb.append(ChaR);
        }

        return "gift_" + sb;
    }
}
