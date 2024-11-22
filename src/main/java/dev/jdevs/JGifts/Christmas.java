package dev.jdevs.JGifts;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.jdevs.JGifts.command.christmas;
import dev.jdevs.JGifts.events.*;
import dev.jdevs.JGifts.loots.Load;
import dev.jdevs.JGifts.made.MessageLanguage;
import dev.jdevs.JGifts.supports.PlaceholderAPI;
import dev.jdevs.JGifts.utils.ConfigManager;
import dev.jdevs.JGifts.utils.Configurations;
import dev.jdevs.JGifts.utils.Message;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static dev.jdevs.JGifts.Settings.*;
import static dev.jdevs.JGifts.events.FallGifts.gifts;
import static dev.jdevs.JGifts.events.FallGifts.removeGifts;
import static dev.jdevs.JGifts.loots.Loot.dropLoot;
import static dev.jdevs.JGifts.supports.WG.setBlock;
import static dev.jdevs.JGifts.utils.Configurations.*;

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
      String version_launch = "1.1.0";
      String version_config = "1.0.1";
      if (!(new File(this.getDataFolder(), "launch.yml")).exists()) {
         saveResource("launch.yml", true);
      }
      else {
         File fl = new File(this.getDataFolder(), "launch.yml");
         YamlConfiguration yml = YamlConfiguration.loadConfiguration(fl);
         if (yml.getString("version") == null || !yml.getString("version").contains(version_launch)) {
            try {
               yml.set("version", version_launch);
               yml.save(fl);
               ConfigUpdater.update(Christmas.getInstance(), "launch.yml", fl);
               Bukkit.getLogger().info("[UPDATE] launch.yml has been successfully updated to version " + version_launch);
            } catch (IOException e) {
               Bukkit.getLogger().warning("!!!Configuration update failed!!!\n Type: launch.yml");
               throw new RuntimeException(e);
            }
         }
      }
      FileConfiguration launch = ConfigManager.of("launch.yml").getYamlConfiguration();
      if (launch.getString("language") == null || !languages.contains(launch.getString("language").toLowerCase())) {
         disabled = true;
         Message.sendLogger("\n&cFinish configuring of the plugin ChristmasGifts before launching it in launch.yml" +
                 "\nЗавершите настройку плагина ChristmasGifts перед его запуском в файле launch.yml&f&r\n");
      }
      else {
         String[] ver = getServer().getBukkitVersion().split("\\.");
         if (!ver[0].endsWith("1")) {
            disabled = true;
            Bukkit.getLogger().warning("THIS PLUGIN DOES NOT SUPPORT MINECRAFT VERSION >=2. Contact the developer to update the plugin.\n" +
                    "ДАННЫЙ ПЛАГИН НЕ ПОДДЕРЖИВАЕТ MINECRAFT ВЕРСИЮ >=2. Обратитесь к разработчику для обновления плагина.");
            return;
         }
         if (ver[1].length() >= 2) {
            version_mode = Integer.parseInt(ver[1].substring(0, 2));
         }
         else {
            version_mode = Integer.parseInt(ver[1]);
         }
         if (version_mode <= 7) {
            Bukkit.getLogger().info("Version mode: <=1.7.10");
            Bukkit.getLogger().info("Here, I felt a strong sense of nostalgia and at the same time fear, but why...\n" +
                    "in the future, I would like to think about ending support for this version...");
            if (version_mode <= 6) {
               Bukkit.getLogger().info("Version mode: <=?6?");
               Bukkit.getLogger().info("Why...");
            }
         }
         else if (version_mode <= 12) {
            Bukkit.getLogger().info("Version mode: <=1.12.2");
            if (version_mode <= 9) {
               Bukkit.getLogger().info("Nostalgia...");
            }
         }
         language = launch.getString("language").toLowerCase();
         String hologramType = null;
         if (launch.getString("BaseSettings.supports.HologramType") != null && !launch.getString("BaseSettings.supports.HologramType").contains("null")) {
            hologramType = launch.getString("BaseSettings.supports.HologramType");
            if (!Bukkit.getPluginManager().isPluginEnabled(hologramType)) {
               disabled = true;
               Message.sendLogger("\n&cInstall plugins on the server: " + hologramType +
                       "\nУстановите на сервер плагин: " + hologramType + "&f&r\n");
               return;
            }
         }
         if (!(new File(this.getDataFolder(), "config.yml")).exists()) {
            saveResource(language + "/config.yml", true);
            try {
               Files.move(Configurations.getDataFolder(language + "/config.yml").toPath(), Configurations.getDataFolder("config.yml").toPath());
               Configurations.getDataFolder(language + "/config.yml").deleteOnExit();
               Files.delete(Configurations.getDataFolder(language).toPath());
               Bukkit.getLogger().info("The creation of config.yml was successful");
            } catch (IOException e) {
               e.printStackTrace();
               Bukkit.getLogger().warning("The creation of the config.yml file was incorrect, please contact the administrator.");
            }
         }
         else {
            File fl = new File(this.getDataFolder(), "config.yml");
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(fl);
            if (yml.getString("version") == null || !yml.getString("version").contains(version_config)) {
               try {
                  yml.set("version", version_config);
                  yml.save(fl);
                  ConfigUpdater.update(Christmas.getInstance(), language + "/config.yml", fl);
                  yml.set("version", version_config);
                  Bukkit.getLogger().info("[UPDATE] config.yml has been successfully updated to version " + version_config);
               } catch (IOException e) {
                  Bukkit.getLogger().warning("!!!Configuration update failed!!!\n Type: config.yml");
                  throw new RuntimeException(e);
               }
            }
         }
         if (!(new File(this.getDataFolder(), "storage/loot.yml")).exists()) {
            saveResource("storage/loot.yml", true);
         }
         if (!(new File(this.getDataFolder(), "storage/db.yml")).exists()) {
            saveResource("storage/db.yml", true);
         }
         FileConfiguration loot = ConfigManager.of("storage/loot.yml").getYamlConfiguration();
         FileConfiguration nicknames = ConfigManager.of("storage/db.yml").getYamlConfiguration();
         YamlConfiguration config = ConfigManager.of("config.yml").getYamlConfiguration();
         Configurations.launch = launch;
         Configurations.config = config;
         Configurations.loot = loot;
         Configurations.nicknames = nicknames;
         if (hologramType != null) {
            HologramType = hologramType.toLowerCase();
         }
         if (!Settings.autoGive) {
            getServer().getPluginManager().registerEvents(new AntiGrief(), this); // 1 action, launch protection
            if (version_mode >= 8) {
               getServer().getPluginManager().registerEvents(new AntiGriefV8(), this); // 1 action, launch protection
            }
            getServer().getPluginManager().registerEvents(new UseGifts(), this); // The player's interaction with the gift to receive it
         }
         if (launch.getInt("BaseSettings.spawn.mode.enabled") == 1) {
            getServer().getPluginManager().registerEvents(new SpawnGifts(), this); // We are starting to spawn gifts
         }
         else if (launch.getInt("BaseSettings.spawn.mode.enabled") == 2) {
            if (!launch.getString("BaseSettings.spawn.mode.2.people").contains("null")) {
               SpawnGifts.EveryTime(launch.getInt("BaseSettings.spawn.mode.2.people"), launch.getInt("BaseSettings.spawn.mode.2.every"));
            }
            else {
               Bukkit.getLogger().warning("How many people will receive gifts once in how many is not specified, the mode 2 is disabled.");
            }
         }
         getServer().getPluginManager().registerEvents(new FallGifts(), this); // We receive a gift using FallBlock and set conditions
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
                  nicknames.save(Configurations.getDataFolder("storage/db.yml"));
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
         if (HologramType != null) {
            if (HologramType.contains("decentholograms") && !FallGifts.decentHolograms.isEmpty()) {
               for (Hologram hd : FallGifts.decentHolograms.values()) {
                  hd.delete();
               }
               FallGifts.decentHolograms.clear();
            }
            else if (!FallGifts.holographicDisplays.isEmpty()) {
               for (me.filoghost.holographicdisplays.api.hologram.Hologram hd : FallGifts.holographicDisplays.values()) {
                  hd.delete();
               }
               FallGifts.holographicDisplays.clear();
            }
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
            nicknames.save(Configurations.getDataFolder("storage/db.yml"));
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
               Bukkit.getLogger().info("[ChristmasGifts] Connection to PlaceholderAPI was successful!");
               cancel();
            }
         }
      }.runTaskTimer(this, 20, 20);
   }
}