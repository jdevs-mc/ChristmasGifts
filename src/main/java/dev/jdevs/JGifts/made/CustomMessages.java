package dev.jdevs.JGifts.made;

import dev.jdevs.JGifts.utils.Configurations;

import java.util.List;

public class CustomMessages {
    public static List<String> hdstring = Configurations.config.getStringList("settings.holograms.lines");
    public static List<String> start_gift = Configurations.config.getStringList("actions.gift.spawn");
    public static List<String> stop_gift = Configurations.config.getStringList("actions.gift.loss");
    public static List<String> success = Configurations.config.getStringList("actions.gift.success");
    public static void Update() {
        hdstring = Configurations.config.getStringList("settings.holograms.lines");
        start_gift = Configurations.config.getStringList("actions.gift.spawn");
        stop_gift = Configurations.config.getStringList("actions.gift.loss");
        success = Configurations.config.getStringList("actions.gift.success");
    }
}
