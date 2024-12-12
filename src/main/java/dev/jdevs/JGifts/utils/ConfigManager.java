package dev.jdevs.JGifts.utils;

import dev.jdevs.JGifts.Christmas;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {
    private static JavaPlugin instance;
    private final YamlConfiguration yamlConfiguration;

    public static void setup(JavaPlugin instance) {
        ConfigManager.instance = instance;
    }

    public static ConfigManager of(String fileName) {
        if (!instance.getDataFolder().exists()) {
            if (instance.getDataFolder().mkdirs()) {
                Christmas.disabled = true;
            }
        }

        File file = new File(instance.getDataFolder(), fileName);
        if (!file.exists()) {
            instance.saveResource(fileName, true);
        }

        return new ConfigManager(YamlConfiguration.loadConfiguration(file));
    }

    public ConfigManager(YamlConfiguration yamlConfiguration) {
        this.yamlConfiguration = yamlConfiguration;
    }

    public YamlConfiguration getYamlConfiguration() {
        return this.yamlConfiguration;
    }
}

