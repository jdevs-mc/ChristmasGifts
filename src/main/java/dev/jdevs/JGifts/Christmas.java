package dev.jdevs.JGifts;

import dev.jdevs.JGifts.command.christmas;
import dev.jdevs.JGifts.events.AntiGrief;
import dev.jdevs.JGifts.events.FallGifts;
import dev.jdevs.JGifts.events.SpawnGifts;
import dev.jdevs.JGifts.events.UseGifts;
import dev.jdevs.JGifts.loots.Load;
import dev.jdevs.JGifts.made.MessageLanguage;
import dev.jdevs.JGifts.supports.PlaceholderAPI;
import dev.jdevs.JGifts.utils.ConfigManager;
import dev.jdevs.JGifts.utils.Configurations;
import dev.jdevs.JGifts.utils.Message;
import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import static dev.jdevs.JGifts.Settings.*;
import static dev.jdevs.JGifts.events.FallGifts.gifts;
import static dev.jdevs.JGifts.events.FallGifts.removeGifts;
import static dev.jdevs.JGifts.loots.Loot.dropLoot;
import static dev.jdevs.JGifts.supports.WG.setBlock;
import static dev.jdevs.JGifts.utils.Configurations.nicknames;

public final class Christmas extends JavaPlugin implements Listener {
   public static boolean disabled = false;
   public static String language;
   public static int version_mode = 13;
   static Christmas instance;
   public static Christmas getInstance() {
      return instance;
   }

   @Override
   public void onEnable() {
      instance = this;
      ConfigManager.setup(this);
      getCommand("christmas").setExecutor(new christmas());
      List<String> languages = Arrays.asList(
              "ru",
              "en"
      );
      if (!(new File(this.getDataFolder(), "launch.yml")).exists()) {
         saveResource("launch.yml", true);
      }
      YamlConfiguration launch = ConfigManager.of("launch.yml").getYamlConfiguration();
      if (launch.getString("language") == null || !languages.contains(launch.getString("language").toLowerCase())) {
         disabled = true;
         Message.sendLogger("\n&cFinish configuring of the plugin ChristmasGifts before launching it in launch.yml" +
                 "\nЗавершите настройку плагина ChristmasGifts перед его запуском в файле launch.yml&f&r\n");
      }
      else if (launch.getBoolean("BaseSettings.supports.DecentHolograms") && !Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
         disabled = true;
         Message.sendLogger("\n&cInstall plugins on the server: DecentHolograms" +
                 "\nУстановите на сервер плагин: DecentHolograms&f&r\n");
      }
      else {
         language = launch.getString("language").toLowerCase();
         if (!(new File(this.getDataFolder(), "config.yml")).exists()) {
            saveResource(language + "\\config.yml", true);
            try {
               Files.move(Configurations.getDataFolder(language + "\\config.yml").toPath(), Configurations.getDataFolder("config.yml").toPath());
               Configurations.getDataFolder(language + "\\config.yml").deleteOnExit();
               Files.delete(Configurations.getDataFolder(language).toPath());
               Bukkit.getLogger().info("The creation of config.yml was successful");
            } catch (IOException e) {
               e.printStackTrace();
               Bukkit.getLogger().warning("The creation of the config.yml file was incorrect, please contact the administrator.");
            }
         }
         if (!(new File(this.getDataFolder(), "storage\\loot.yml")).exists()) {
            saveResource("storage\\loot.yml", true);
         }
         if (!(new File(this.getDataFolder(), "storage\\db.yml")).exists()) {
            saveResource("storage\\db.yml", true);
         }
         FileConfiguration loot = ConfigManager.of("storage\\loot.yml").getYamlConfiguration();
         FileConfiguration nicknames = ConfigManager.of("storage\\db.yml").getYamlConfiguration();
         YamlConfiguration config = ConfigManager.of("config.yml").getYamlConfiguration();
         Configurations.launch = launch;
         Configurations.config = config;
         Configurations.loot = loot;
         Configurations.nicknames = nicknames;
         String[] ver = getServer().getVersion().split("\\.");
         if (!ver[0].endsWith("1")) {
            disabled = true;
            Bukkit.getLogger().warning("THIS PLUGIN DOES NOT SUPPORT MINECRAFT VERSION >=2. Contact the developer to update the plugin.\n" +
                    "ДАННЫЙ ПЛАГИН НЕ ПОДДЕРЖИВАЕТ MINECRAFT ВЕРСИЮ >=2. Обратитесь к разработчику для обновления плагина.");
            return;
         }
         int version = Integer.parseInt(ver[1]);
         Object type = config.get("settings.gift.spawn.type");
         version_mode = version;
         if (version > 13) {
            SpawnGifts.mt = Material.getMaterial("BARREL");
            if (type != null) {
               SpawnGifts.mt = Material.getMaterial(config.getString("settings.gift.spawn.type", "BARREL"));
            }
         }
         else if (version == 13) {
            SpawnGifts.mt = Material.getMaterial("OAK_PLANKS");
            if (type != null) {
               SpawnGifts.mt = Material.getMaterial(config.getString("settings.gift.spawn.type", "OAK_PLANKS"));
            }
         }
         else {
            try {
               // Ignore warnings
               @SuppressWarnings("all")
               Method mtd = Material.class.getMethod("getMaterial", int.class);
               //
               SpawnGifts.mt = (Material) mtd.invoke(Material.class, 5);
               if (config.get("settings.gift.spawn.id") != null) {
                  SpawnGifts.mt = (Material) mtd.invoke(Material.class, config.getInt("settings.gift.spawn.id", 5));
               }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
               throw new RuntimeException(e);
            }
            Bukkit.getLogger().info("Version mode: <=1.12.2");
         }
         if (!Settings.autoGive) {
            getServer().getPluginManager().registerEvents(new AntiGrief(), this); // 1 action, launch protection
         }
         if (launch.getInt("BaseSettings.spawn.mode.enabled") == 1) {
            getServer().getPluginManager().registerEvents(new SpawnGifts(), this); // We are starting to spawn gifts
         }
         else if (launch.getInt("BaseSettings.spawn.mode.enabled") == 2) {
            SpawnGifts.EveryTime(launch.getInt("BaseSettings.spawn.mode.2.people"), launch.getInt("BaseSettings.spawn.mode.2.every"));
         }
         getServer().getPluginManager().registerEvents(new FallGifts(), this); // We receive a gift using FallBlock and set conditions
         if (!Settings.autoGive) {
            getServer().getPluginManager().registerEvents(new UseGifts(), this); // The player's interaction with the gift to receive it
         }
         if (Settings.Loots) {
            Load.LoadLoots();
         }
         if (Settings.onCrashes) {
            if (nicknames.getStringList("gifts") != null) {
               List<String> gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
               for (String locs : nicknames.getStringList("gifts")) {
                  String[] split = locs.split(":");
                  Location loc = new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
                  dropLoot(loc);
                  gifts2.remove(locs);
               }
               nicknames.set("gifts", gifts2);
               try {
                  nicknames.save(Configurations.getDataFolder("storage\\db.yml"));
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }
         if (Settings.PlaceholderAPI) {
            placeholderAPI = new PlaceholderAPI();
            connectPlaceholderAPI();
         }
         MessageLanguage.send("start", null, null);
      }
   }

   @Override
   public void onDisable() {
      if (!disabled) {
         if (takedLoot || onCrashes) {
            if (!gifts.isEmpty()) {
               for (Location loc : gifts.keySet()) {
                  if (takedLoot) {
                     dropLoot(loc);
                  }
                  removeGifts(loc);
               }
               gifts.clear();
            }
         }
         else {
            if (!gifts.isEmpty()) {
               gifts.clear();
            }
         }
         if (DSupport && !FallGifts.hds.isEmpty()) {
            for (String loc : FallGifts.hds.values()) {
               DHAPI.removeHologram(loc);
            }
            FallGifts.hds.clear();
         }
         if (version_mode > 12) {
            if (!FallGifts.saveBlock.isEmpty()) {
               for (Location loc : FallGifts.saveBlock.keySet()) {
                  if (FallGifts.saveBlock.get(loc) != null) {
                     setBlock(loc, loc.getBlock());
                  }
               }
               FallGifts.saveBlock.clear();
            }
         }
         else {
            if (!FallGifts.saveBlock_12.isEmpty()) {
               for (Location loc : FallGifts.saveBlock_12.keySet()) {
                  if (FallGifts.saveBlock_12.get(loc) != null) {
                     setBlock(loc, loc.getBlock());
                  }
               }
               FallGifts.saveBlock_12.clear();
            }
         }
         if (placeholderAPI != null) {
            placeholderAPI.unregister();
         }
         try {
            nicknames.save(Configurations.getDataFolder("storage\\db.yml"));
         } catch (IOException e) {
            e.printStackTrace();
         }
         MessageLanguage.send("stop", null, null);
      }
   }
   PlaceholderAPI placeholderAPI = null;
   void connectPlaceholderAPI() {
      new BukkitRunnable() {
         @Override
         public void run() {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
               placeholderAPI.register();
               Bukkit.getLogger().info("Connection to PlaceholderAPI was successful!");
               cancel();
            }
         }
      }.runTaskTimer(this, 20, 20);
   }
}