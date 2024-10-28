package dev.jdevs.JGifts.utils;

import dev.jdevs.JGifts.Christmas;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class Configurations {
    public static FileConfiguration launch;
    public static FileConfiguration config;
    public static FileConfiguration loot;
    public static FileConfiguration nicknames;
    public static File getDataFolder(String yml) {
        return new File(Christmas.getInstance().getDataFolder(), yml);
    }
}
