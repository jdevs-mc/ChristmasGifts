package dev.jdevs.JGifts;

import dev.jdevs.JGifts.utils.Configurations;

import java.util.concurrent.ThreadLocalRandom;

import static dev.jdevs.JGifts.utils.Configurations.config;
import static dev.jdevs.JGifts.utils.Configurations.launch;

public class Settings {
    public static boolean debug = launch.getBoolean("BaseSettings.debug");
    public static boolean onCrashes = launch.getBoolean("BaseSettings.onCrashes.enabled");
    public static boolean autoGive = launch.getBoolean("BaseSettings.autoGive");
    public static int wg = launch != null ? launch.getInt("BaseSettings.spawn.wg_support", 3) : 0;
    public static String key = "ChristmasGifts";
    public static boolean PlaceholderAPI = launch != null && launch.getBoolean("BaseSettings.supports.PlaceholderAPI");
    public static boolean WorldGuard = launch != null && launch.getBoolean("BaseSettings.supports.WorldGuard");
    public static int max = config.getInt("settings.gift.max");
    public static boolean limit = config.getBoolean("settings.gift.limit");
    public static String HologramType;
    public static boolean takedLoot = config.getBoolean("settings.loot.taked");
    public static String generateText(int length) {
        String symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(symbols.length());
            char ChaR = symbols.charAt(index);
            sb.append(ChaR);
        }

        return "gift_" + sb;
    }
    public static Boolean Loots = Configurations.config.getBoolean("settings.loot.enabled");
}
