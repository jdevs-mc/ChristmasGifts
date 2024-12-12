package dev.jdevs.JGifts;

import dev.jdevs.JGifts.updater.Updater;
import dev.jdevs.JGifts.command.Gift;
import dev.jdevs.JGifts.loots.Load;
import dev.jdevs.JGifts.made.MessageLanguage;
import dev.jdevs.JGifts.supports.WG;
import dev.jdevs.JGifts.utils.Message;
import dev.jdevs.JGifts.utils.Values;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Getter
public final class Christmas extends JavaPlugin {
   private boolean disabled = false;
   public static String language;
   private int version_mode = 13;
   private YamlConfiguration launch = of("launch.yml");
   private YamlConfiguration nicknames;
   private YamlConfiguration loot;
   private Updater updater;
   private Values values;
   private Message messages;
   private WG wg;
   private Load load;
   private MessageLanguage sends;
   String version_launch = "1.1.1";
   String version_config = "1.1.0";
   @Override
   public void onEnable() {
      getCommand("christmas").setExecutor(new Gift(this));
      List<String> languages = Arrays.asList(
              "ru",
              "en"
      );
      // We have created launch.yml
      File start = new File(this.getDataFolder(), "launch.yml");
      if (!start.exists()) {
         saveResource("launch.yml", true);
      }
      updater = new Updater(this);
      createStart(start);
      //
      // We get the language
      if (launch.getString("language") == null || !languages.contains(launch.getString("language").toLowerCase())) {
         disabled = true;
         Bukkit.getLogger().warning("\nFinish configuring of the plugin ChristmasGifts before launching it in launch.yml" +
                 "\nЗавершите настройку плагина ChristmasGifts перед его запуском в файле launch.yml\n");
         return;
      }
      //
      checkVersion();
      //
      createConfigurations();
      YamlConfiguration loot = of("storage/loot.yml");
      YamlConfiguration nicknames = of("storage/db.yml");
      this.loot = loot;
      this.nicknames = nicknames;
      load = new Load(this);
      values = new Values(this);
      values.setupValues(true);
      sends = new MessageLanguage(this);
      messages = new Message(this);
      wg = new WG(this);
      sends.send("start", null, null);
   }

   @Override
   public void onDisable() {
      if (disabled) {
         return;
      }
      if (!values.getGifts().isEmpty()) {
         if (values.isTakedLoot() || values.isOnCrashes()) {
            boolean takedLoot = values.isTakedLoot();
            for (Location loc : new HashSet<>(values.getGifts().keySet())) {
               if (takedLoot) {
                  getLoad().dropLoot(loc);
               }
               removeGifts(loc);
            }
         }
      }
      if (values.getHologramType() != null) {
         if (values.getHologramType().contains("decentholograms") && !values.getDecentHolograms().isEmpty()) {
            for (Hologram hd : new HashSet<>(values.getDecentHolograms().values())) {
               hd.delete();
            }
         } else if (!values.getHolographicDisplays().isEmpty()) {
            for (me.filoghost.holographicdisplays.api.hologram.Hologram hd : new HashSet<>(values.getHolographicDisplays().values())) {
               hd.delete();
            }
         }
      }
      if (version_mode > 12) {
         if (!values.getSaveBlock().isEmpty()) {
            for (Location loc : new HashSet<>(values.getSaveBlock().keySet())) {
               if (values.getSaveBlock().get(loc) != null) {
                  wg.setBlock(loc, loc.getBlock());
               }
            }
         }
      } else {
         if (!values.getSaveBlock_12().isEmpty()) {
            for (Location loc : new HashSet<>(values.getSaveBlock_12().keySet())) {
               wg.setBlock(loc, loc.getBlock());
            }
         }
      }
      if (values.getPlaceholderAPI() != null) {
         values.getPlaceholderAPI().unregister();
      }
      try {
         nicknames.save(new File(getDataFolder(), "storage/db.yml"));
      } catch (IOException e) {
         e.printStackTrace();
      }
      sends.send("stop", null, null);
   }
   public YamlConfiguration of(String fileName) {
      if (!getDataFolder().exists()) {
         if (getDataFolder().mkdirs()) {
            disabled = true;
         }
      }

      File file = new File(getDataFolder(), fileName);
      if (!file.exists()) {
         saveResource(fileName, true);
      }

      return YamlConfiguration.loadConfiguration(file);
   }
   public void setDisabled(boolean bol) {
      disabled = bol;
   }
   public void createConfigurations() {
      File cfg = new File(this.getDataFolder(), "config.yml");
      if (!cfg.exists()) {
         String path = language + "/config.yml";
         saveResource(path, true);
         File lang = new File(getDataFolder(), language);
         File cfg_lang = new File(getDataFolder(), path);
         try {
            Files.move(cfg_lang.toPath(), cfg.toPath());
            cfg.deleteOnExit();
            Files.delete(lang.toPath());
            Bukkit.getLogger().info("The creation of config.yml was successful");
         } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("The creation of the config.yml file was incorrect, please contact the administrator.");
         }
      }
      YamlConfiguration yml = YamlConfiguration.loadConfiguration(cfg);
      if (yml.getString("version") == null || !yml.getString("version").contains(version_config)) {
         try {
            yml.set("version", version_config);
            yml.save(cfg);
            updater.update(language + "/config.yml", cfg);
            Bukkit.getLogger().info("[UPDATE] config.yml has been successfully updated to version " + version_config);
         } catch (IOException e) {
            Bukkit.getLogger().warning("!!!Configuration update failed!!!\n Type: config.yml");
            throw new RuntimeException(e);
         }
      }
      String loot = "storage/loot.yml";
      if (!(new File(getDataFolder(), loot)).exists()) {
         saveResource(loot, true);
      }
      String db = "storage/db.yml";
      if (!(new File(getDataFolder(), db)).exists()) {
         saveResource(db, true);
      }
   }
   private void createStart(File fl) {
      launch = YamlConfiguration.loadConfiguration(fl);
      if (launch.getString("version") == null || !launch.getString("version").contains(version_launch)) {
         try {
            launch.set("version", version_launch);
            launch.save(fl);
            updater.update("launch.yml", fl);
            launch = YamlConfiguration.loadConfiguration(fl);
            Bukkit.getLogger().info("[UPDATE] launch.yml has been successfully updated to version " + version_launch);
         } catch (IOException e) {
            Bukkit.getLogger().warning("!!!Configuration update failed!!!\n Type: launch.yml");
            throw new RuntimeException(e);
         }
      }
   }
   private void checkVersion() {
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
   }
   public void updateLoot() {
      this.loot = of("storage/loot.yml");
   }
   public void removeGifts(Location b_loc) {
      if (values.isOnCrashes()) {
         if (nicknames.getStringList("gifts") != null) {
            List<String> gifts2 = new ArrayList<>(nicknames.getStringList("gifts"));
            gifts2.remove(b_loc.getWorld().getName() + ":" + b_loc.getX() + ":" + b_loc.getY() + ":" + b_loc.getZ());
            nicknames.set("gifts", gifts2);
            try {
               nicknames.save(new File(getDataFolder(), "storage/db.yml"));
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
   }
}