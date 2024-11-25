package dev.jdevs.JGifts.supports;

import dev.jdevs.JGifts.Christmas;
import dev.jdevs.JGifts.utils.Values;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderAPI extends PlaceholderExpansion {
    Christmas plugin;
    Values values;
    public PlaceholderAPI(Christmas plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
    }
    @Override
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        String[] args = ind.split("_");
        if (args.length >= 1) {
            if (args[0].contains("active")) {
                if (args.length != 2) {
                    if (values.getGifts().containsValue(p)) {
                        return "true";
                    }
                } else {
                    Player uuid = Bukkit.getPlayer(args[1]);
                    if (values.getGifts().containsValue(uuid)) {
                        return "true";
                    }
                }
                return "false";
            } else if (args[0].contains("limit")) {
                ConfigurationSection players = plugin.getNicknames().getConfigurationSection("players");
                if (args.length != 2) {
                    if (values.getMax() > players.getInt(p.getName(), 0)) {
                        return "false";
                    }
                } else {
                    String name = Bukkit.getPlayer(args[1]).getName();
                    if (values.getMax() > players.getInt(name, 0)) {
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
