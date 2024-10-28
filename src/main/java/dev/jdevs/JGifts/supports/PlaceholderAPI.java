package dev.jdevs.JGifts.supports;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static dev.jdevs.JGifts.Settings.max;
import static dev.jdevs.JGifts.events.FallGifts.gifts;
import static dev.jdevs.JGifts.utils.Configurations.nicknames;

public class PlaceholderAPI extends PlaceholderExpansion {
    @Override
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        String[] args = ind.split("_");
        if (args.length >= 1) {
            if (args[0].contains("active")) {
                if (args.length != 2) {
                    if (gifts.containsValue(p)) {
                        return "true";
                    }
                } else {
                    Player uuid = Bukkit.getPlayer(args[1]);
                    if (gifts.containsValue(uuid)) {
                        return "true";
                    }
                }
                return "false";
            } else if (args[0].contains("limit")) {
                ConfigurationSection players = nicknames.getConfigurationSection("players");
                if (args.length != 2) {
                    if (max > players.getInt(p.getName(), 0)) {
                        return "false";
                    }
                } else {
                    String name = Bukkit.getPlayer(args[1]).getName();
                    if (max > players.getInt(name, 0)) {
                        return "false";
                    }
                }
                return "true";
            }
        }
        return null;
    }
    @Override
    public @NotNull String getIdentifier() {
        return "ChristmasGifts";
    }

    @Override
    public @NotNull String getAuthor() {
        return "JDevs";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
}
